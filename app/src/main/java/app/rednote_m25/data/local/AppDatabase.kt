package app.rednote_m25.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.rednote_m25.data.local.dao.NoteDao
import app.rednote_m25.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
