package app.rednote_m25.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rednote_m25.data.repository.NoteRepository
import app.rednote_m25.domain.model.Note
import app.rednote_m25.util.FormatUtils
import app.rednote_m25.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditNoteUiState(
    val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val coverImageUrl: String = "",
    val imageUrls: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: 0L

    private val _uiState = MutableStateFlow(EditNoteUiState(noteId = noteId, isLoading = true))
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    init {
        Logger.i("EditNoteViewModel", "Loading note for edit: id=$noteId")
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            try {
                val note = noteRepository.getNoteById(noteId).first()
                if (note != null) {
                    _uiState.update {
                        it.copy(
                            noteId = note.id,
                            title = note.title,
                            content = note.content,
                            tags = note.tags.joinToString(","),
                            coverImageUrl = note.coverImageUrl ?: "",
                            imageUrls = note.imageUrls.joinToString(","),
                            isLoading = false,
                            error = null
                        )
                    }
                    Logger.i("EditNoteViewModel", "Note loaded: ${note.title}")
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "笔记不存在") }
                }
            } catch (e: Exception) {
                Logger.e("EditNoteViewModel", "Failed to load note", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun updateTags(tags: String) {
        _uiState.update { it.copy(tags = tags) }
    }

    fun updateCoverImageUrl(url: String) {
        _uiState.update { it.copy(coverImageUrl = url) }
    }

    fun updateImageUrls(urls: String) {
        _uiState.update { it.copy(imageUrls = urls) }
    }

    fun saveNote() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "标题不能为空") }
            return
        }
        if (state.content.isBlank()) {
            _uiState.update { it.copy(error = "内容不能为空") }
            return
        }

        Logger.i("EditNoteViewModel", "Saving note: ${state.title}")

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val note = Note(
                    id = state.noteId,
                    title = state.title,
                    content = state.content,
                    coverImageUrl = state.coverImageUrl.ifBlank { null },
                    imageUrls = if (state.imageUrls.isBlank()) emptyList() else state.imageUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    authorName = FormatUtils.CURRENT_USER_NAME,
                    authorAvatarUrl = null,
                    tags = state.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    updatedAt = System.currentTimeMillis()
                )

                noteRepository.updateNote(note)
                Logger.i("EditNoteViewModel", "Note saved successfully")
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                Logger.e("EditNoteViewModel", "Failed to save note", e)
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "保存失败") }
            }
        }
    }
}