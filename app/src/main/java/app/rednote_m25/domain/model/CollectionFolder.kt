package app.rednote_m25.domain.model

data class CollectionFolder(
    val id: Long = 0,
    val name: String,
    val noteCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
