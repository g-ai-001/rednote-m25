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

data class CategoryUiState(
    val tags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        Logger.i("CategoryViewModel", "Loading tags")
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            noteRepository.getAllTags()
                .catch { e ->
                    Logger.e("CategoryViewModel", "Failed to load tags", e)
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { tags ->
                    Logger.i("CategoryViewModel", "Loaded ${tags.size} tags")
                    _uiState.update { it.copy(tags = tags) }
                }
        }
    }

    fun selectTag(tag: String) {
        Logger.i("CategoryViewModel", "Selected tag: $tag")
        _uiState.update { it.copy(selectedTag = tag, isLoading = true) }
        loadNotesByTag(tag)
    }

    fun clearTagSelection() {
        _uiState.update { it.copy(selectedTag = null, notes = emptyList()) }
    }

    private fun loadNotesByTag(tag: String) {
        viewModelScope.launch {
            noteRepository.getNotesByTag(tag)
                .catch { e ->
                    Logger.e("CategoryViewModel", "Failed to load notes for tag: $tag", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { notes ->
                    Logger.i("CategoryViewModel", "Loaded ${notes.size} notes for tag: $tag")
                    _uiState.update { it.copy(notes = notes, isLoading = false) }
                }
        }
    }

    fun toggleLike(noteId: Long, isLiked: Boolean) {
        Logger.i("CategoryViewModel", "toggleLike: noteId=$noteId, isLiked=$isLiked")
        viewModelScope.launch {
            noteRepository.toggleLike(noteId, isLiked)
        }
    }

    fun toggleCollect(noteId: Long, isCollected: Boolean) {
        Logger.i("CategoryViewModel", "toggleCollect: noteId=$noteId, isCollected=$isCollected")
        viewModelScope.launch {
            noteRepository.toggleCollect(noteId, isCollected)
        }
    }
}
