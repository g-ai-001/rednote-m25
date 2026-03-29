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

data class DraftUiState(
    val drafts: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DraftViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DraftUiState(isLoading = true))
    val uiState: StateFlow<DraftUiState> = _uiState.asStateFlow()

    init {
        loadDrafts()
    }

    private fun loadDrafts() {
        viewModelScope.launch {
            noteRepository.getDraftNotes()
                .catch { e ->
                    Logger.e("DraftViewModel", "Failed to load drafts", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { drafts ->
                    _uiState.update { it.copy(drafts = drafts, isLoading = false, error = null) }
                }
        }
    }

    fun deleteDraft(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note.id)
                Logger.i("DraftViewModel", "Draft deleted: ${note.id}")
            } catch (e: Exception) {
                Logger.e("DraftViewModel", "Failed to delete draft", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun publishDraft(note: Note) {
        viewModelScope.launch {
            try {
                val publishedNote = note.copy(
                    isDraft = false,
                    updatedAt = System.currentTimeMillis()
                )
                noteRepository.updateNote(publishedNote)
                Logger.i("DraftViewModel", "Draft published: ${note.id}")
            } catch (e: Exception) {
                Logger.e("DraftViewModel", "Failed to publish draft", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}