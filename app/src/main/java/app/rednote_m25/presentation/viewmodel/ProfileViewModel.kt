package app.rednote_m25.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rednote_m25.data.repository.ExportImportRepository
import app.rednote_m25.data.repository.NoteRepository
import app.rednote_m25.data.repository.UserPreferencesRepository
import app.rednote_m25.domain.model.Note
import app.rednote_m25.util.FormatUtils
import app.rednote_m25.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ProfileUiState(
    val myNotes: List<Note> = emptyList(),
    val myCollections: List<Note> = emptyList(),
    val selectedTab: ProfileTab = ProfileTab.MY_NOTES,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userAvatarUrl: String = "",
    val showDeleteDialog: Boolean = false,
    val noteToDelete: Note? = null,
    val exportSuccess: String? = null,
    val importSuccess: String? = null,
    val importError: String? = null
)

enum class ProfileTab {
    MY_NOTES,
    MY_COLLECTIONS
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exportImportRepository: ExportImportRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val currentUserName = FormatUtils.CURRENT_USER_NAME

    init {
        Logger.i("ProfileViewModel", "Loading profile data")
        loadMyNotes()
        loadMyCollections()
        loadUserAvatar()
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

    private fun loadUserAvatar() {
        viewModelScope.launch {
            userPreferencesRepository.userAvatarUrl.collect { url ->
                _uiState.update { it.copy(userAvatarUrl = url) }
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

    fun showDeleteConfirmation(note: Note) {
        _uiState.update { it.copy(showDeleteDialog = true, noteToDelete = note) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, noteToDelete = null) }
    }

    fun deleteNote() {
        val note = _uiState.value.noteToDelete ?: return
        Logger.i("ProfileViewModel", "Deleting note: ${note.id}")
        viewModelScope.launch {
            noteRepository.deleteNote(note.id)
            _uiState.update { it.copy(showDeleteDialog = false, noteToDelete = null) }
        }
    }

    fun updateUserAvatar(avatarUrl: String) {
        Logger.i("ProfileViewModel", "Updating user avatar")
        viewModelScope.launch {
            userPreferencesRepository.updateUserAvatar(avatarUrl)
        }
    }

    fun exportData(): Intent? {
        Logger.i("ProfileViewModel", "Exporting data")
        var intent: Intent? = null
        viewModelScope.launch {
            try {
                val jsonData = exportImportRepository.exportAllData()
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "rednote_backup_${dateFormat.format(Date())}.json"

                val exportDir = File(context.getExternalFilesDir(null), "exports")
                if (!exportDir.exists()) {
                    exportDir.mkdirs()
                }

                val file = File(exportDir, fileName)
                file.writeText(jsonData)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                _uiState.update { it.copy(exportSuccess = file.absolutePath) }
                Logger.i("ProfileViewModel", "Export successful: ${file.absolutePath}")
            } catch (e: Exception) {
                Logger.e("ProfileViewModel", "Export failed", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
        return intent
    }

    fun importData(jsonString: String) {
        Logger.i("ProfileViewModel", "Importing data")
        viewModelScope.launch {
            when (val result = exportImportRepository.importData(jsonString)) {
                is ExportImportRepository.ImportResult.Success -> {
                    val message = "导入成功：${result.notesCount}篇笔记，${result.commentsCount}条评论"
                    _uiState.update { it.copy(importSuccess = message) }
                    Logger.i("ProfileViewModel", message)
                }
                is ExportImportRepository.ImportResult.Error -> {
                    _uiState.update { it.copy(importError = result.message) }
                    Logger.e("ProfileViewModel", "Import failed: ${result.message}")
                }
            }
        }
    }

    fun clearExportSuccess() {
        _uiState.update { it.copy(exportSuccess = null) }
    }

    fun clearImportSuccess() {
        _uiState.update { it.copy(importSuccess = null) }
    }

    fun clearImportError() {
        _uiState.update { it.copy(importError = null) }
    }
}
