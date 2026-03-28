package app.rednote_m25.data.local.dao

import androidx.room.*
import app.rednote_m25.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE notes SET isLiked = :isLiked, likeCount = :newLikeCount WHERE id = :id")
    suspend fun updateLikeStatus(id: Long, isLiked: Boolean, newLikeCount: Int)

    @Query("UPDATE notes SET isCollected = :isCollected, collectCount = :newCollectCount WHERE id = :id")
    suspend fun updateCollectStatus(id: Long, isCollected: Boolean, newCollectCount: Int)

    @Query("SELECT * FROM notes WHERE isCollected = 1 ORDER BY updatedAt DESC")
    fun getCollectedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY createdAt DESC")
    fun searchNotes(keyword: String): Flow<List<NoteEntity>>

    @Query("SELECT DISTINCT tags FROM notes WHERE tags != ''")
    fun getAllTags(): Flow<List<String>>

    @Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun getNotesByTag(tag: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE authorName = :authorName ORDER BY createdAt DESC")
    fun getNotesByAuthor(authorName: String): Flow<List<NoteEntity>>
}
