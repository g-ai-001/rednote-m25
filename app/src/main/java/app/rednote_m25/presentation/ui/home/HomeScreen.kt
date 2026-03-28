package app.rednote_m25.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.rednote_m25.presentation.ui.components.StaggeredNotesGrid
import app.rednote_m25.presentation.viewmodel.HomeViewModel

enum class HomeTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("首页", Icons.Filled.Home, Icons.Outlined.Home),
    CATEGORY("分类", Icons.Filled.Category, Icons.Outlined.Category),
    PERSON("我的", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNoteClick: (Long) -> Unit,
    onSearchClick: () -> Unit = {},
    onCollectionClick: () -> Unit = {},
    onCategoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "rednote",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    }
                    IconButton(onClick = onCollectionClick) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "收藏"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            when (tab) {
                                HomeTab.HOME -> {}
                                HomeTab.CATEGORY -> onCategoryClick()
                                HomeTab.PERSON -> onProfileClick()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .wrapContentSize()
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "加载失败",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }
                else -> {
                    StaggeredNotesGrid(
                        notes = uiState.notes,
                        onNoteClick = onNoteClick,
                        onLikeClick = { id, isLiked -> viewModel.toggleLike(id, isLiked) },
                        onCollectClick = { id, isCollected -> viewModel.toggleCollect(id, isCollected) }
                    )
                }
            }
        }
    }
}
