package app.rednote_m25.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import app.rednote_m25.presentation.ui.components.StaggeredNotesGrid
import app.rednote_m25.presentation.viewmodel.ProfileTab
import app.rednote_m25.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onPublishClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            ProfileHeader()

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
                        StaggeredNotesGrid(
                            notes = uiState.myNotes,
                            onNoteClick = onNoteClick,
                            onLikeClick = { id, isLiked -> viewModel.toggleLike(id, isLiked) },
                            onCollectClick = { id, isCollected -> viewModel.toggleCollect(id, isCollected) }
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
}

@Composable
private fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = "https://picsum.photos/seed/avatar_me/200/200",
            contentDescription = "头像",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "当前用户",
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
