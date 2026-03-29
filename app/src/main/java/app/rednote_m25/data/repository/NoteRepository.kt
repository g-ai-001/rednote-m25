package app.rednote_m25.data.repository

import app.rednote_m25.data.local.dao.NoteDao
import app.rednote_m25.data.local.entity.NoteEntity
import app.rednote_m25.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getNoteById(id: Long): Flow<Note?> {
        return noteDao.getNoteByIdFlow(id).map { it?.toDomain() }
    }

    suspend fun getNoteByIdOnce(id: Long): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }

    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity())
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    suspend fun deleteNote(id: Long) {
        noteDao.deleteNoteById(id)
    }

    suspend fun toggleLike(id: Long, isLiked: Boolean) {
        val note = noteDao.getNoteById(id) ?: return
        val newLikeCount = if (isLiked) note.likeCount + 1 else maxOf(0, note.likeCount - 1)
        noteDao.updateLikeStatus(id, isLiked, newLikeCount)
    }

    suspend fun toggleCollect(id: Long, isCollected: Boolean) {
        val note = noteDao.getNoteById(id) ?: return
        val newCollectCount = if (isCollected) note.collectCount + 1 else maxOf(0, note.collectCount - 1)
        noteDao.updateCollectStatus(id, isCollected, newCollectCount)
    }

    suspend fun incrementShareCount(id: Long) {
        noteDao.incrementShareCount(id)
    }

    fun getCollectedNotes(): Flow<List<Note>> {
        return noteDao.getCollectedNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchNotes(keyword: String): Flow<List<Note>> {
        return noteDao.searchNotes(keyword).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllTags(): Flow<List<String>> {
        return noteDao.getAllTags().map { tagStrings ->
            tagStrings.flatMap { it.split(",") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }
    }

    fun getNotesByTag(tag: String): Flow<List<Note>> {
        return noteDao.getNotesByTag(tag).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getNotesCountByTag(tag: String): Flow<Int> {
        return noteDao.getNotesByTag(tag).map { entities -> entities.size }
    }

    fun getNotesByAuthor(authorName: String): Flow<List<Note>> {
        return noteDao.getNotesByAuthor(authorName).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getDraftNotes(): Flow<List<Note>> {
        return noteDao.getDraftNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            coverImageUrl = coverImageUrl,
            imageUrls = if (imageUrls.isBlank()) emptyList() else imageUrls.split(","),
            videoUrls = if (videoUrls.isBlank()) emptyList() else videoUrls.split(","),
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            likeCount = likeCount,
            collectCount = collectCount,
            commentCount = commentCount,
            shareCount = shareCount,
            isLiked = isLiked,
            isCollected = isCollected,
            isDraft = isDraft,
            tags = if (tags.isBlank()) emptyList() else tags.split(","),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            content = content,
            coverImageUrl = coverImageUrl,
            imageUrls = imageUrls.joinToString(","),
            videoUrls = videoUrls.joinToString(","),
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            likeCount = likeCount,
            collectCount = collectCount,
            commentCount = commentCount,
            shareCount = shareCount,
            isLiked = isLiked,
            isCollected = isCollected,
            isDraft = isDraft,
            tags = tags.joinToString(","),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
