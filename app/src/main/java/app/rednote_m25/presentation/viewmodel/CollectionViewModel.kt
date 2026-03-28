package app.rednote_m25.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rednote_m25.data.repository.NoteRepository
import app.rednote_m25.domain.model.Note
import app.rednote_m25.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionUiState(
    val collectedNotes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState(isLoading = true))
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    init {
        Logger.i("CollectionViewModel", "Loading collected notes")
        loadCollectedNotes()
    }

    private fun loadCollectedNotes() {
        viewModelScope.launch {
            noteRepository.getCollectedNotes()
                .catch { e ->
                    Logger.e("CollectionViewModel", "Failed to load collected notes", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { notes ->
                    Logger.i("CollectionViewModel", "Loaded ${notes.size} collected notes")
                    _uiState.update { it.copy(collectedNotes = notes, isLoading = false, error = null) }
                }
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