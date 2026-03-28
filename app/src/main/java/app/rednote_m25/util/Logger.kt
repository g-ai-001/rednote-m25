package app.rednote_m25.util

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

object Logger {
    private const val LOG_DIR = "logs"
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024L
    private val logQueue = ConcurrentLinkedQueue<String>()
    private val executor = Executors.newSingleThreadExecutor()
    private var logFile: File? = null

    fun init(context: Context) {
        val logDir = File(context.getExternalFilesDir(null), LOG_DIR)
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        logFile = File(logDir, "app_${dateFormat.format(Date())}.log")
    }

    fun d(tag: String, message: String) {
        log("DEBUG", tag, message)
    }

    fun i(tag: String, message: String) {
        log("INFO", tag, message)
    }

    fun w(tag: String, message: String) {
        log("WARN", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log("ERROR", tag, message)
        throwable?.let {
            log("ERROR", tag, it.stackTraceToString())
        }
    }

    private fun log(level: String, tag: String, message: String) {
        val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        val timestamp = timeFormat.format(Date())
        val logEntry = "[$timestamp][$level][$tag] $message"
        logQueue.offer(logEntry)
        executor.execute {
            writeToFile()
        }
    }

    private fun writeToFile() {
        val file = logFile ?: return
        try {
            if (file.length() > MAX_LOG_SIZE) {
                val newFile = File(file.parent, "app_${SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())}.log")
                file.renameTo(newFile)
            }
            FileWriter(file, true).use { writer ->
                while (true) {
                    val entry = logQueue.poll() ?: break
                    writer.write("$entry\n")
                }
                writer.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
