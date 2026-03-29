package app.rednote_m25.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rednote_m25.data.repository.NoteRepository
import app.rednote_m25.data.repository.UserPreferencesRepository
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

data class PublishUiState(
    val draftId: Long? = null,
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val coverImageUrl: String = "",
    val imageUrls: String = "",
    val videoUrls: String = "",
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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

    fun updateVideoUrls(urls: String) {
        _uiState.update { it.copy(videoUrls = urls) }
    }

    fun loadDraft(draft: Note) {
        _uiState.update {
            it.copy(
                draftId = draft.id,
                title = draft.title,
                content = draft.content,
                tags = draft.tags.joinToString(","),
                coverImageUrl = draft.coverImageUrl ?: "",
                imageUrls = draft.imageUrls.joinToString(","),
                videoUrls = draft.videoUrls.joinToString(",")
            )
        }
    }

    fun saveDraft() {
        val state = _uiState.value
        Logger.i("PublishViewModel", "Saving draft")

        viewModelScope.launch {
            try {
                val userAvatar = userPreferencesRepository.userAvatarUrl.first()
                val coverUrl = state.coverImageUrl.ifBlank {
                    "https://picsum.photos/seed/${System.currentTimeMillis()}/400/300"
                }
                val imageList = if (state.imageUrls.isBlank()) {
                    emptyList()
                } else {
                    state.imageUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                }
                val videoList = if (state.videoUrls.isBlank()) {
                    emptyList()
                } else {
                    state.videoUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                }

                val note = Note(
                    id = state.draftId ?: 0,
                    title = state.title,
                    content = state.content,
                    coverImageUrl = coverUrl,
                    imageUrls = imageList,
                    videoUrls = videoList,
                    authorName = FormatUtils.CURRENT_USER_NAME,
                    authorAvatarUrl = userAvatar.ifEmpty { null },
                    tags = state.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    isDraft = true,
                    updatedAt = System.currentTimeMillis()
                )

                if (state.draftId != null) {
                    noteRepository.updateNote(note)
                } else {
                    noteRepository.insertNote(note)
                }
                Logger.i("PublishViewModel", "Draft saved successfully")
                _uiState.update { it.copy(publishSuccess = true) }
            } catch (e: Exception) {
                Logger.e("PublishViewModel", "Failed to save draft", e)
                _uiState.update { it.copy(error = e.message ?: "保存失败") }
            }
        }
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

        Logger.i("PublishViewModel", "Publishing note")

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, error = null) }
            try {
                val userAvatar = userPreferencesRepository.userAvatarUrl.first()
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
                    authorName = FormatUtils.CURRENT_USER_NAME,
                    authorAvatarUrl = userAvatar.ifEmpty { null },
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
