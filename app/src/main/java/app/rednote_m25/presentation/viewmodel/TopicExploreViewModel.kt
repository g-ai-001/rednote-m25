package app.rednote_m25.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rednote_m25.data.repository.NoteRepository
import app.rednote_m25.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicItem(
    val tag: String,
    val noteCount: Int
)

data class TopicExploreUiState(
    val topics: List<TopicItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TopicExploreViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicExploreUiState(isLoading = true))
    val uiState: StateFlow<TopicExploreUiState> = _uiState.asStateFlow()

    init {
        Logger.i("TopicExploreViewModel", "Loading topics")
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            try {
                noteRepository.getAllNotes().collect { notes ->
                    Logger.i("TopicExploreViewModel", "Loaded ${notes.size} notes")
                    val tagCounts = mutableMapOf<String, Int>()
                    notes.forEach { note ->
                        note.tags.forEach { tag ->
                            tagCounts[tag] = tagCounts.getOrDefault(tag, 0) + 1
                        }
                    }
                    val topicItems = tagCounts.map { (tag, count) ->
                        TopicItem(tag = tag, noteCount = count)
                    }.sortedByDescending { it.noteCount }
                    _uiState.update { it.copy(topics = topicItems, isLoading = false) }
                }
            } catch (e: Exception) {
                Logger.e("TopicExploreViewModel", "Failed to load topics", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}