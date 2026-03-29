package app.rednote_m25.presentation.ui.publish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.rednote_m25.presentation.viewmodel.PublishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    onBackClick: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: PublishViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.publishSuccess) {
        if (uiState.publishSuccess) {
            onPublishSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.draftId != null) "编辑草稿" else "发布笔记") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveDraft() }) {
                        Text("存草稿")
                    }
                    IconButton(
                        onClick = { viewModel.publish() },
                        enabled = !uiState.isPublishing
                    ) {
                        if (uiState.isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "发布"
                            )
                        }
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("标题") },
                placeholder = { Text("请输入笔记标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("内容") },
                placeholder = { Text("请输入笔记内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.tags,
                onValueChange = { viewModel.updateTags(it) },
                label = { Text("标签") },
                placeholder = { Text("多个标签用逗号分隔，如：美食,旅行,摄影") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.coverImageUrl,
                onValueChange = { viewModel.updateCoverImageUrl(it) },
                label = { Text("封面图片URL（可选）") },
                placeholder = { Text("留空则使用随机图片") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.imageUrls,
                onValueChange = { viewModel.updateImageUrls(it) },
                label = { Text("图片URL列表（可选，多个用逗号分隔）") },
                placeholder = { Text("留空则使用随机图片") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.videoUrls,
                onValueChange = { viewModel.updateVideoUrls(it) },
                label = { Text("视频URL列表（可选，多个用逗号分隔）") },
                placeholder = { Text("支持本地视频路径或网络URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.publish() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isPublishing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("发布笔记")
            }
        }
    }
}
