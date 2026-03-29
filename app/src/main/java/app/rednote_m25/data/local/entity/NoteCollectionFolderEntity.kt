package app.rednote_m25.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "note_collection_folders",
    primaryKeys = ["noteId", "folderId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CollectionFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("noteId"),
        Index("folderId")
    ]
)
data class NoteCollectionFolderEntity(
    val noteId: Long,
    val folderId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
