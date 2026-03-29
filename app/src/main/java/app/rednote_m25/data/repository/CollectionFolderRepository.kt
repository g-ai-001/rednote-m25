package app.rednote_m25.data.repository

import app.rednote_m25.data.local.dao.CollectionFolderDao
import app.rednote_m25.data.local.entity.CollectionFolderEntity
import app.rednote_m25.data.local.entity.NoteCollectionFolderEntity
import app.rednote_m25.domain.model.CollectionFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionFolderRepository @Inject constructor(
    private val collectionFolderDao: CollectionFolderDao
) {
    fun getAllFolders(): Flow<List<CollectionFolder>> {
        return collectionFolderDao.getAllFolders().map { folders ->
            folders.map { it.toDomain() }
        }
    }

    suspend fun createFolder(name: String): Long {
        val folder = CollectionFolderEntity(name = name)
        return collectionFolderDao.insertFolder(folder)
    }

    suspend fun updateFolder(folder: CollectionFolder) {
        collectionFolderDao.updateFolder(folder.toEntity())
    }

    suspend fun deleteFolder(folderId: Long) {
        collectionFolderDao.deleteFolderById(folderId)
    }

    suspend fun addNoteToFolder(noteId: Long, folderId: Long) {
        collectionFolderDao.addNoteToFolder(NoteCollectionFolderEntity(noteId = noteId, folderId = folderId))
    }

    suspend fun removeNoteFromFolder(noteId: Long, folderId: Long) {
        collectionFolderDao.removeNoteFromFolder(noteId, folderId)
    }

    suspend fun removeNoteFromAllFolders(noteId: Long) {
        collectionFolderDao.removeNoteFromAllFolders(noteId)
    }

    fun getFoldersForNote(noteId: Long): Flow<List<CollectionFolder>> {
        return collectionFolderDao.getFolderIdsForNote(noteId).map { folderIds ->
            folderIds.mapNotNull { folderId ->
                collectionFolderDao.getFolderById(folderId)?.toDomain()
            }
        }
    }

    fun getNotesInFolder(folderId: Long): Flow<List<Long>> {
        return collectionFolderDao.getNoteIdsInFolder(folderId)
    }

    fun getNoteCountInFolder(folderId: Long): Flow<Int> {
        return collectionFolderDao.getNoteCountInFolder(folderId)
    }

    fun getFoldersWithNoteCounts(): Flow<List<CollectionFolder>> {
        return collectionFolderDao.getAllFolders().map { folders ->
            folders.map { folder ->
                val count = collectionFolderDao.getNoteCountInFolder(folder.id).first()
                folder.toDomain(count)
            }
        }
    }

    private fun CollectionFolderEntity.toDomain(noteCount: Int = 0): CollectionFolder {
        return CollectionFolder(
            id = id,
            name = name,
            noteCount = noteCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun CollectionFolder.toEntity(): CollectionFolderEntity {
        return CollectionFolderEntity(
            id = id,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
