package app.rednote_m25.domain.model

data class Comment(
    val id: Long = 0,
    val noteId: Long,
    val authorName: String,
    val authorAvatarUrl: String?,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)