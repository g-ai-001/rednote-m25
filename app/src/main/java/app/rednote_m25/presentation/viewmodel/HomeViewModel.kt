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

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortType: SortType = SortType.LATEST
)

enum class SortType {
    LATEST,
    POPULAR
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Logger.i("HomeViewModel", "Loading notes")
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes()
                .catch { e ->
                    Logger.e("HomeViewModel", "Failed to load notes", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { notes ->
                    Logger.i("HomeViewModel", "Loaded ${notes.size} notes")
                    applySorting(notes)
                }
        }
    }

    private fun applySorting(notes: List<Note>) {
        val sortedNotes = when (_uiState.value.sortType) {
            SortType.LATEST -> notes.sortedByDescending { it.createdAt }
            SortType.POPULAR -> notes.sortedByDescending { it.likeCount + it.collectCount }
        }
        _uiState.update { it.copy(notes = sortedNotes, isLoading = false, error = null) }
    }

    fun setSortType(sortType: SortType) {
        Logger.i("HomeViewModel", "setSortType: $sortType")
        val currentNotes = _uiState.value.notes
        val sortedNotes = when (sortType) {
            SortType.LATEST -> currentNotes.sortedByDescending { it.createdAt }
            SortType.POPULAR -> currentNotes.sortedByDescending { it.likeCount + it.collectCount }
        }
        _uiState.update { it.copy(notes = sortedNotes, sortType = sortType) }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadNotes()
    }

    fun toggleLike(noteId: Long, isLiked: Boolean) {
        Logger.i("HomeViewModel", "toggleLike: noteId=$noteId, isLiked=$isLiked")
        viewModelScope.launch {
            noteRepository.toggleLike(noteId, isLiked)
        }
    }

    fun toggleCollect(noteId: Long, isCollected: Boolean) {
        Logger.i("HomeViewModel", "toggleCollect: noteId=$noteId, isCollected=$isCollected")
        viewModelScope.launch {
            noteRepository.toggleCollect(noteId, isCollected)
        }
    }
}
