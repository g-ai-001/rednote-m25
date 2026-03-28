package app.rednote_m25.domain.model

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val coverImageUrl: String?,
    val imageUrls: List<String>,
    val authorName: String,
    val authorAvatarUrl: String?,
    val likeCount: Int = 0,
    val collectCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
