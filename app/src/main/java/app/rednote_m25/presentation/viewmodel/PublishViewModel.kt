package app.rednote_m25.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rednote_m25.data.repository.NoteRepository
import app.rednote_m25.domain.model.Note
import app.rednote_m25.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublishUiState(
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val coverImageUrl: String = "",
    val imageUrls: String = "",
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

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

    fun publish() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "标题不能为空") }
            return
        }
        if (state.content.isBlank()) {
            _uiState.update { it.copy(error = "内容不能为空") }
            return
        }

        Logger.i("PublishViewModel", "Publishing note: ${state.title}")

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, error = null) }
            try {
                val coverUrl = state.coverImageUrl.ifBlank {
                    "https://picsum.photos/seed/${System.currentTimeMillis()}/400/300"
                }
                val imageList = if (state.imageUrls.isBlank()) {
                    listOf(
                        "https://picsum.photos/seed/${System.currentTimeMillis()}/400/400",
                        "https://picsum.photos/seed/${System.currentTimeMillis() + 1}/400/400"
                    )
                } else {
                    state.imageUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                }

                val note = Note(
                    title = state.title,
                    content = state.content,
                    coverImageUrl = coverUrl,
                    imageUrls = imageList,
                    authorName = "当前用户",
                    authorAvatarUrl = null,
                    tags = state.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                )

                noteRepository.insertNote(note)
                Logger.i("PublishViewModel", "Note published successfully")
                _uiState.update { it.copy(isPublishing = false, publishSuccess = true) }
            } catch (e: Exception) {
                Logger.e("PublishViewModel", "Failed to publish note", e)
                _uiState.update { it.copy(isPublishing = false, error = e.message ?: "发布失败") }
            }
        }
    }

    fun resetState() {
        _uiState.value = PublishUiState()
    }
}
