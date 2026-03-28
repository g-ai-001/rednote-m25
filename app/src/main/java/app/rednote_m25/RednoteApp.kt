package app.rednote_m25

import android.app.Application
import app.rednote_m25.data.local.DataSeeder
import app.rednote_m25.util.Logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@HiltAndroidApp
class RednoteApp : Application() {

    @Inject
    lateinit var dataSeeder: DataSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        Logger.i("RednoteApp", "Application started")
        dataSeeder.seedIfEmpty(applicationScope)
    }
}
