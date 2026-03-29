package app.rednote_m25.data.local.dao

import androidx.room.*
import app.rednote_m25.data.local.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE noteId = :noteId ORDER BY createdAt DESC")
    fun getCommentsByNoteId(noteId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments ORDER BY createdAt DESC")
    suspend fun getAllComments(): List<CommentEntity>

    @Query("SELECT COUNT(*) FROM comments WHERE noteId = :noteId")
    suspend fun getCommentCount(noteId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteCommentById(id: Long)

    @Query("DELETE FROM comments WHERE noteId = :noteId")
    suspend fun deleteCommentsByNoteId(noteId: Long)
}