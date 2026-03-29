package app.rednote_m25.data.repository

import app.rednote_m25.data.local.dao.CommentDao
import app.rednote_m25.data.local.dao.NoteDao
import app.rednote_m25.data.local.entity.CommentEntity
import app.rednote_m25.data.local.entity.NoteEntity
import app.rednote_m25.util.Logger
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportImportRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val commentDao: CommentDao
) {
    suspend fun exportAllData(): String {
        Logger.i("ExportImportRepository", "Starting data export")
        val notes = noteDao.getAllNotes().first()
        val allComments = commentDao.getAllComments().first()
        val noteIds = notes.map { it.id }.toSet()
        val comments = allComments.filter { it.noteId in noteIds }

        val jsonObject = JSONObject().apply {
            put("version", 1)
            put("exportTime", System.currentTimeMillis())
            put("notes", JSONArray().apply {
                notes.forEach { noteEntity ->
                    put(JSONObject().apply {
                        put("id", noteEntity.id)
                        put("title", noteEntity.title)
                        put("content", noteEntity.content)
                        put("coverImageUrl", noteEntity.coverImageUrl ?: "")
                        put("imageUrls", noteEntity.imageUrls)
                        put("authorName", noteEntity.authorName)
                        put("authorAvatarUrl", noteEntity.authorAvatarUrl ?: "")
                        put("likeCount", noteEntity.likeCount)
                        put("collectCount", noteEntity.collectCount)
                        put("commentCount", noteEntity.commentCount)
                        put("shareCount", noteEntity.shareCount)
                        put("isLiked", noteEntity.isLiked)
                        put("isCollected", noteEntity.isCollected)
                        put("tags", noteEntity.tags)
                        put("createdAt", noteEntity.createdAt)
                        put("updatedAt", noteEntity.updatedAt)
                    })
                }
            })
            put("comments", JSONArray().apply {
                comments.forEach { commentEntity ->
                    put(JSONObject().apply {
                        put("id", commentEntity.id)
                        put("noteId", commentEntity.noteId)
                        put("authorName", commentEntity.authorName)
                        put("authorAvatarUrl", commentEntity.authorAvatarUrl ?: "")
                        put("content", commentEntity.content)
                        put("createdAt", commentEntity.createdAt)
                    })
                }
            })
        }

        Logger.i("ExportImportRepository", "Exported ${notes.size} notes and ${comments.size} comments")
        return jsonObject.toString()
    }

    suspend fun importData(jsonString: String): ImportResult {
        Logger.i("ExportImportRepository", "Starting data import")
        return try {
            val jsonObject = JSONObject(jsonString)
            val version = jsonObject.optInt("version", 1)

            val notesArray = jsonObject.getJSONArray("notes")
            val notesToInsert = mutableListOf<NoteEntity>()

            for (i in 0 until notesArray.length()) {
                val noteJson = notesArray.getJSONObject(i)
                notesToInsert.add(
                    NoteEntity(
                        id = 0,
                        title = noteJson.getString("title"),
                        content = noteJson.getString("content"),
                        coverImageUrl = noteJson.optString("coverImageUrl").ifEmpty { null },
                        imageUrls = noteJson.getString("imageUrls"),
                        authorName = noteJson.getString("authorName"),
                        authorAvatarUrl = noteJson.optString("authorAvatarUrl").ifEmpty { null },
                        likeCount = noteJson.optInt("likeCount", 0),
                        collectCount = noteJson.optInt("collectCount", 0),
                        commentCount = noteJson.optInt("commentCount", 0),
                        shareCount = noteJson.optInt("shareCount", 0),
                        isLiked = noteJson.optBoolean("isLiked", false),
                        isCollected = noteJson.optBoolean("isCollected", false),
                        tags = noteJson.getString("tags"),
                        createdAt = noteJson.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = noteJson.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            noteDao.insertNotes(notesToInsert)

            val commentsArray = jsonObject.getJSONArray("comments")
            val commentsToInsert = mutableListOf<CommentEntity>()

            for (i in 0 until commentsArray.length()) {
                val commentJson = commentsArray.getJSONObject(i)
                commentsToInsert.add(
                    CommentEntity(
                        id = 0,
                        noteId = commentJson.getLong("noteId"),
                        authorName = commentJson.getString("authorName"),
                        authorAvatarUrl = commentJson.optString("authorAvatarUrl").ifEmpty { null },
                        content = commentJson.getString("content"),
                        createdAt = commentJson.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            for (comment in commentsToInsert) {
                commentDao.insertComment(comment)
            }

            val notesCount = notesToInsert.size
            val commentsCount = commentsToInsert.size
            Logger.i("ExportImportRepository", "Imported $notesCount notes and $commentsCount comments")
            ImportResult.Success(notesCount, commentsCount)
        } catch (e: Exception) {
            Logger.e("ExportImportRepository", "Import failed", e)
            ImportResult.Error(e.message ?: "Unknown error")
        }
    }

    sealed class ImportResult {
        data class Success(val notesCount: Int, val commentsCount: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}