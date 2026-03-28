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

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<Note> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isNotBlank()) {
            search(query)
        } else {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        }
    }

    private fun search(query: String) {
        Logger.i("SearchViewModel", "Searching for: $query")
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            noteRepository.searchNotes(query)
                .catch { e ->
                    Logger.e("SearchViewModel", "Search failed", e)
                    _uiState.update { it.copy(isSearching = false, error = e.message) }
                }
                .collect { notes ->
                    Logger.i("SearchViewModel", "Found ${notes.size} notes")
                    _uiState.update { it.copy(searchResults = notes, isSearching = false, error = null) }
                }
        }
    }

    fun toggleLike(noteId: Long, isLiked: Boolean) {
        Logger.i("SearchViewModel", "toggleLike: noteId=$noteId, isLiked=$isLiked")
        viewModelScope.launch {
            noteRepository.toggleLike(noteId, isLiked)
        }
    }

    fun toggleCollect(noteId: Long, isCollected: Boolean) {
        Logger.i("SearchViewModel", "toggleCollect: noteId=$noteId, isCollected=$isCollected")
        viewModelScope.launch {
            noteRepository.toggleCollect(noteId, isCollected)
        }
    }
}