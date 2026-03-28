package app.rednote_m25.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rednote_m25.data.repository.NoteRepository
import app.rednote_m25.domain.model.Note
import app.rednote_m25.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteDetailUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: 0L

    private val _uiState = MutableStateFlow(NoteDetailUiState(isLoading = true))
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        Logger.i("NoteDetailViewModel", "Loading note: id=$noteId")
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            noteRepository.getNoteById(noteId)
                .catch { e ->
                    Logger.e("NoteDetailViewModel", "Failed to load note: id=$noteId", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { note ->
                    Logger.i("NoteDetailViewModel", "Loaded note: id=$noteId, found=${note != null}")
                    _uiState.update { it.copy(note = note, isLoading = false, error = null) }
                }
        }
    }

    fun toggleLike() {
        val note = _uiState.value.note ?: return
        Logger.i("NoteDetailViewModel", "toggleLike: noteId=${note.id}, isLiked=${!note.isLiked}")
        viewModelScope.launch {
            noteRepository.toggleLike(note.id, !note.isLiked)
        }
    }

    fun toggleCollect() {
        val note = _uiState.value.note ?: return
        Logger.i("NoteDetailViewModel", "toggleCollect: noteId=${note.id}, isCollected=${!note.isCollected}")
        viewModelScope.launch {
            noteRepository.toggleCollect(note.id, !note.isCollected)
        }
    }
}
