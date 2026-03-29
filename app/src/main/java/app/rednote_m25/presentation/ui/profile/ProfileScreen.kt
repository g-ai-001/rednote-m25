package app.rednote_m25.presentation.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import app.rednote_m25.domain.model.Note
import app.rednote_m25.presentation.ui.components.StaggeredNotesGrid
import app.rednote_m25.presentation.viewmodel.ProfileTab
import app.rednote_m25.presentation.viewmodel.ProfileViewModel
import app.rednote_m25.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onPublishClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAvatarDialog by remember { mutableStateOf(false) }
    var showExportImportMenu by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val jsonString = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                if (!jsonString.isNullOrEmpty()) {
                    viewModel.importData(jsonString)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(uiState.importSuccess) {
        uiState.importSuccess?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearImportSuccess()
        }
    }

    LaunchedEffect(uiState.importError) {
        uiState.importError?.let {
            Toast.makeText(context, "导入失败: $it", Toast.LENGTH_SHORT).show()
            viewModel.clearImportError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人主页") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showExportImportMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多"
                        )
                    }
                    DropdownMenu(
                        expanded = showExportImportMenu,
                        onDismissRequest = { showExportImportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("导出数据") },
                            onClick = {
                                showExportImportMenu = false
                                val intent = viewModel.exportData()
                                if (intent != null) {
                                    context.startActivity(intent)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Upload, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("导入数据") },
                            onClick = {
                                showExportImportMenu = false
                                importLauncher.launch("application/json")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Download, contentDescription = null)
                            }
                        )
                    }
                    IconButton(onClick = onPublishClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "发布笔记"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ProfileHeader(
                avatarUrl = uiState.userAvatarUrl,
                onAvatarClick = { showAvatarDialog = true }
            )

            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.selectedTab == ProfileTab.MY_NOTES,
                    onClick = { viewModel.selectTab(ProfileTab.MY_NOTES) },
                    text = { Text("我的笔记") }
                )
                Tab(
                    selected = uiState.selectedTab == ProfileTab.MY_COLLECTIONS,
                    onClick = { viewModel.selectTab(ProfileTab.MY_COLLECTIONS) },
                    text = { Text("我的收藏") }
                )
            }

            when (uiState.selectedTab) {
                ProfileTab.MY_NOTES -> {
                    if (uiState.myNotes.isEmpty()) {
                        EmptyState(message = "还没有发布笔记")
                    } else {
                        MyNotesList(
                            notes = uiState.myNotes,
                            onNoteClick = onNoteClick,
                            onLikeClick = { id, isLiked -> viewModel.toggleLike(id, isLiked) },
                            onCollectClick = { id, isCollected -> viewModel.toggleCollect(id, isCollected) },
                            onDeleteClick = { note -> viewModel.showDeleteConfirmation(note) }
                        )
                    }
                }
                ProfileTab.MY_COLLECTIONS -> {
                    if (uiState.myCollections.isEmpty()) {
                        EmptyState(message = "还没有收藏笔记")
                    } else {
                        StaggeredNotesGrid(
                            notes = uiState.myCollections,
                            onNoteClick = onNoteClick,
                            onLikeClick = { id, isLiked -> viewModel.toggleLike(id, isLiked) },
                            onCollectClick = { id, isCollected -> viewModel.toggleCollect(id, isCollected) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("删除笔记") },
            text = { Text("确定要删除这篇笔记吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteNote() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("取消")
                }
            }
        )
    }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            currentAvatarUrl = uiState.userAvatarUrl,
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = { url ->
                viewModel.updateUserAvatar(url)
                showAvatarDialog = false
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    avatarUrl: String,
    onAvatarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clickable { onAvatarClick() }
        ) {
            AsyncImage(
                model = avatarUrl.ifEmpty { "https://picsum.photos/seed/avatar_me/200/200" },
                contentDescription = "头像",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "修改头像",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = FormatUtils.CURRENT_USER_NAME,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "记录生活，分享美好",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MyNotesList(
    notes: List<Note>,
    onNoteClick: (Long) -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onCollectClick: (Long, Boolean) -> Unit,
    onDeleteClick: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCardWithDelete(
                note = note,
                onNoteClick = { onNoteClick(note.id) },
                onLikeClick = { onLikeClick(note.id, !note.isLiked) },
                onCollectClick = { onCollectClick(note.id, !note.isCollected) },
                onDeleteClick = { onDeleteClick(note) }
            )
        }
    }
}

@Composable
private fun NoteCardWithDelete(
    note: Note,
    onNoteClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNoteClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = note.coverImageUrl ?: "",
                    contentDescription = note.title,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.tags.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(note.tags.take(3)) { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (note.isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "点赞",
                            tint = if (note.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onCollectClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (note.isCollected) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "收藏",
                            tint = if (note.isCollected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarSelectionDialog(
    currentAvatarUrl: String,
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit
) {
    val avatarOptions = listOf(
        "https://picsum.photos/seed/avatar1/200/200",
        "https://picsum.photos/seed/avatar2/200/200",
        "https://picsum.photos/seed/avatar3/200/200",
        "https://picsum.photos/seed/avatar4/200/200",
        "https://picsum.photos/seed/avatar5/200/200",
        "https://picsum.photos/seed/avatar6/200/200",
        "https://picsum.photos/seed/avatar_me/200/200",
        "https://picsum.photos/seed/avatar7/200/200"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择头像") },
        text = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(avatarOptions) { avatarUrl ->
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "头像选项",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onAvatarSelected(avatarUrl) }
                            .then(
                                if (avatarUrl == currentAvatarUrl) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                } else Modifier
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}