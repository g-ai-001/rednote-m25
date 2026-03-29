package app.rednote_m25.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rednote_m25.data.repository.CollectionFolderRepository
import app.rednote_m25.data.repository.NoteRepository
import app.rednote_m25.domain.model.CollectionFolder
import app.rednote_m25.domain.model.Note
import app.rednote_m25.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionUiState(
    val collectedNotes: List<Note> = emptyList(),
    val folders: List<CollectionFolder> = emptyList(),
    val selectedFolderId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: CollectionFolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState(isLoading = true))
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<Long?>(null)

    init {
        Logger.i("CollectionViewModel", "Loading collected notes and folders")
        loadFolders()
        observeSelectedFolder()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            folderRepository.getFoldersWithNoteCounts()
                .catch { e ->
                    Logger.e("CollectionViewModel", "Failed to load folders", e)
                }
                .collect { folders ->
                    _uiState.update { it.copy(folders = folders) }
                }
        }
    }

    private fun observeSelectedFolder() {
        viewModelScope.launch {
            _selectedFolderId.collect { folderId ->
                loadCollectedNotes(folderId)
            }
        }
    }

    private fun loadCollectedNotes(folderId: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (folderId == null) {
                noteRepository.getCollectedNotes()
                    .catch { e ->
                        Logger.e("CollectionViewModel", "Failed to load collected notes", e)
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                    .collect { notes ->
                        Logger.i("CollectionViewModel", "Loaded ${notes.size} collected notes")
                        _uiState.update { it.copy(collectedNotes = notes, isLoading = false, error = null, selectedFolderId = folderId) }
                    }
            } else {
                folderRepository.getNotesInFolder(folderId)
                    .combine(noteRepository.getCollectedNotes()) { noteIds, collectedNotes ->
                        noteIds.mapNotNull { id -> collectedNotes.find { it.id == id } }
                    }
                    .catch { e ->
                        Logger.e("CollectionViewModel", "Failed to load notes in folder", e)
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                    .collect { notes ->
                        Logger.i("CollectionViewModel", "Loaded ${notes.size} notes in folder $folderId")
                        _uiState.update { it.copy(collectedNotes = notes, isLoading = false, error = null, selectedFolderId = folderId) }
                    }
            }
        }
    }

    fun selectFolder(folderId: Long?) {
        Logger.i("CollectionViewModel", "selectFolder: folderId=$folderId")
        _selectedFolderId.value = folderId
    }

    fun createFolder(name: String) {
        Logger.i("CollectionViewModel", "createFolder: name=$name")
        viewModelScope.launch {
            folderRepository.createFolder(name)
        }
    }

    fun deleteFolder(folderId: Long) {
        Logger.i("CollectionViewModel", "deleteFolder: folderId=$folderId")
        viewModelScope.launch {
            folderRepository.deleteFolder(folderId)
            if (_selectedFolderId.value == folderId) {
                _selectedFolderId.value = null
            }
        }
    }

    fun addNoteToFolder(noteId: Long, folderId: Long) {
        Logger.i("CollectionViewModel", "addNoteToFolder: noteId=$noteId, folderId=$folderId")
        viewModelScope.launch {
            folderRepository.addNoteToFolder(noteId, folderId)
        }
    }

    fun removeNoteFromFolder(noteId: Long, folderId: Long) {
        Logger.i("CollectionViewModel", "removeNoteFromFolder: noteId=$noteId, folderId=$folderId")
        viewModelScope.launch {
            folderRepository.removeNoteFromFolder(noteId, folderId)
        }
    }

    fun removeNoteFromAllFolders(noteId: Long) {
        Logger.i("CollectionViewModel", "removeNoteFromAllFolders: noteId=$noteId")
        viewModelScope.launch {
            folderRepository.removeNoteFromAllFolders(noteId)
        }
    }

    fun toggleLike(noteId: Long, isLiked: Boolean) {
        Logger.i("CollectionViewModel", "toggleLike: noteId=$noteId, isLiked=$isLiked")
        viewModelScope.launch {
            noteRepository.toggleLike(noteId, isLiked)
        }
    }

    fun toggleCollect(noteId: Long, isCollected: Boolean) {
        Logger.i("CollectionViewModel", "toggleCollect: noteId=$noteId, isCollected=$isCollected")
        viewModelScope.launch {
            noteRepository.toggleCollect(noteId, isCollected)
        }
    }
}