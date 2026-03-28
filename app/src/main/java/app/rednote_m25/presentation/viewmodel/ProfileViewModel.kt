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

data class ProfileUiState(
    val myNotes: List<Note> = emptyList(),
    val myCollections: List<Note> = emptyList(),
    val selectedTab: ProfileTab = ProfileTab.MY_NOTES,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ProfileTab {
    MY_NOTES,
    MY_COLLECTIONS
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val currentUserName = "当前用户"

    init {
        Logger.i("ProfileViewModel", "Loading profile data")
        loadMyNotes()
        loadMyCollections()
    }

    private fun loadMyNotes() {
        viewModelScope.launch {
            noteRepository.getNotesByAuthor(currentUserName)
                .catch { e ->
                    Logger.e("ProfileViewModel", "Failed to load my notes", e)
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { notes ->
                    Logger.i("ProfileViewModel", "Loaded ${notes.size} my notes")
                    _uiState.update { it.copy(myNotes = notes) }
                }
        }
    }

    private fun loadMyCollections() {
        viewModelScope.launch {
            noteRepository.getCollectedNotes()
                .catch { e ->
                    Logger.e("ProfileViewModel", "Failed to load my collections", e)
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { notes ->
                    Logger.i("ProfileViewModel", "Loaded ${notes.size} my collections")
                    _uiState.update { it.copy(myCollections = notes) }
                }
        }
    }

    fun selectTab(tab: ProfileTab) {
        Logger.i("ProfileViewModel", "Selected tab: $tab")
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleLike(noteId: Long, isLiked: Boolean) {
        Logger.i("ProfileViewModel", "toggleLike: noteId=$noteId, isLiked=$isLiked")
        viewModelScope.launch {
            noteRepository.toggleLike(noteId, isLiked)
        }
    }

    fun toggleCollect(noteId: Long, isCollected: Boolean) {
        Logger.i("ProfileViewModel", "toggleCollect: noteId=$noteId, isCollected=$isCollected")
        viewModelScope.launch {
            noteRepository.toggleCollect(noteId, isCollected)
        }
    }
}
