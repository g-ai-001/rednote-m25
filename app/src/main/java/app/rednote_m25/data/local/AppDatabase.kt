package app.rednote_m25.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.rednote_m25.data.local.dao.CollectionFolderDao
import app.rednote_m25.data.local.dao.CommentDao
import app.rednote_m25.data.local.dao.NoteDao
import app.rednote_m25.data.local.entity.CollectionFolderEntity
import app.rednote_m25.data.local.entity.CommentEntity
import app.rednote_m25.data.local.entity.NoteCollectionFolderEntity
import app.rednote_m25.data.local.entity.NoteEntity

@Database(
    entities = [
        NoteEntity::class,
        CommentEntity::class,
        CollectionFolderEntity::class,
        NoteCollectionFolderEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun commentDao(): CommentDao
    abstract fun collectionFolderDao(): CollectionFolderDao
}
