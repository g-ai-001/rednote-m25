package app.rednote_m25.data.local.dao

import androidx.room.*
import app.rednote_m25.data.local.entity.CollectionFolderEntity
import app.rednote_m25.data.local.entity.NoteCollectionFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionFolderDao {
    @Query("SELECT * FROM collection_folders ORDER BY updatedAt DESC")
    fun getAllFolders(): Flow<List<CollectionFolderEntity>>

    @Query("SELECT * FROM collection_folders WHERE id = :id")
    suspend fun getFolderById(id: Long): CollectionFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: CollectionFolderEntity): Long

    @Update
    suspend fun updateFolder(folder: CollectionFolderEntity)

    @Delete
    suspend fun deleteFolder(folder: CollectionFolderEntity)

    @Query("DELETE FROM collection_folders WHERE id = :id")
    suspend fun deleteFolderById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNoteToFolder(noteCollectionFolder: NoteCollectionFolderEntity)

    @Query("DELETE FROM note_collection_folders WHERE noteId = :noteId AND folderId = :folderId")
    suspend fun removeNoteFromFolder(noteId: Long, folderId: Long)

    @Query("DELETE FROM note_collection_folders WHERE noteId = :noteId")
    suspend fun removeNoteFromAllFolders(noteId: Long)

    @Query("SELECT folderId FROM note_collection_folders WHERE noteId = :noteId")
    fun getFolderIdsForNote(noteId: Long): Flow<List<Long>>

    @Query("SELECT noteId FROM note_collection_folders WHERE folderId = :folderId")
    fun getNoteIdsInFolder(folderId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM note_collection_folders WHERE folderId = :folderId")
    fun getNoteCountInFolder(folderId: Long): Flow<Int>

    @Query("""
        SELECT cf.*, COUNT(ncf.noteId) as noteCount
        FROM collection_folders cf
        LEFT JOIN note_collection_folders ncf ON cf.id = ncf.folderId
        GROUP BY cf.id
        ORDER BY cf.updatedAt DESC
    """)
    fun getAllFoldersWithNoteCounts(): Flow<List<CollectionFolderWithCount>>
}

data class CollectionFolderWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val noteCount: Int
)
