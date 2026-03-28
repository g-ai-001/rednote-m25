package app.rednote_m25.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val authorName: String,
    val authorAvatarUrl: String?,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)