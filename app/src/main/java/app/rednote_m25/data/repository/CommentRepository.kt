package app.rednote_m25.data.repository

import app.rednote_m25.data.local.dao.CommentDao
import app.rednote_m25.data.local.entity.CommentEntity
import app.rednote_m25.domain.model.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val commentDao: CommentDao
) {
    fun getCommentsByNoteId(noteId: Long): Flow<List<Comment>> {
        return commentDao.getCommentsByNoteId(noteId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getCommentCount(noteId: Long): Int {
        return commentDao.getCommentCount(noteId)
    }

    suspend fun addComment(comment: Comment): Long {
        return commentDao.insertComment(comment.toEntity())
    }

    suspend fun deleteComment(id: Long) {
        commentDao.deleteCommentById(id)
    }

    suspend fun deleteCommentsByNoteId(noteId: Long) {
        commentDao.deleteCommentsByNoteId(noteId)
    }

    private fun CommentEntity.toDomain(): Comment {
        return Comment(
            id = id,
            noteId = noteId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            content = content,
            createdAt = createdAt
        )
    }

    private fun Comment.toEntity(): CommentEntity {
        return CommentEntity(
            id = id,
            noteId = noteId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            content = content,
            createdAt = createdAt
        )
    }
}