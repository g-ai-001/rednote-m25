package app.rednote_m25.presentation.ui.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import app.rednote_m25.domain.model.Comment
import app.rednote_m25.domain.model.Note
import app.rednote_m25.presentation.ui.theme.RednoteRed
import app.rednote_m25.presentation.viewmodel.NoteDetailViewModel
import app.rednote_m25.util.FormatUtils
import app.rednote_m25.util.Logger
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit = {},
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var shareBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val isDarkTheme = MaterialTheme.colorScheme.background == Color(0xFF1A1A1A)

    fun shareNote(note: Note) {
        viewModel.incrementShareCount()
        val bitmap = createShareCardBitmapInternal(note, isDarkTheme)
        shareBitmap = bitmap
        bitmap?.let { bmp ->
            shareImage(context, bmp, note.title)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    uiState.note?.let { note ->
                        IconButton(onClick = { onEditClick(note.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑"
                            )
                        }
                        IconButton(onClick = { shareNote(note) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享"
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "更多"
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
                uiState.note != null -> {
                    uiState.note.let { note ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                note.coverImageUrl?.let { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = note.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.headlineMedium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = note.authorAvatarUrl ?: "",
                                        contentDescription = note.authorName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = note.authorName,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = FormatUtils.formatDate(note.createdAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                if (note.tags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(note.tags) { tag ->
                                            AssistChip(
                                                onClick = { },
                                                label = { Text("#$tag") }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ActionButton(
                                        icon = if (note.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        count = note.likeCount,
                                        label = "点赞",
                                        isActive = note.isLiked,
                                        onClick = { viewModel.toggleLike() }
                                    )
                                    ActionButton(
                                        icon = if (note.isCollected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        count = note.collectCount,
                                        label = "收藏",
                                        isActive = note.isCollected,
                                        onClick = { viewModel.toggleCollect() }
                                    )
                                    ActionButton(
                                        icon = Icons.Outlined.ChatBubbleOutline,
                                        count = uiState.comments.size,
                                        label = "评论",
                                        onClick = { }
                                    )
                                    ActionButton(
                                        icon = Icons.Outlined.Share,
                                        count = note.shareCount,
                                        label = "分享",
                                        onClick = { shareNote(note) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                HorizontalDivider()

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "评论",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        items(uiState.comments) { comment ->
                            CommentItem(
                                comment = comment,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.newCommentText,
                                onValueChange = { viewModel.updateNewCommentText(it) },
                                placeholder = { Text("添加评论...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.addComment() },
                                enabled = uiState.newCommentText.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "发送",
                                    tint = if (uiState.newCommentText.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }  // LazyColumn
                    }  // let note
                }  // when note != null
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.authorAvatarUrl ?: "",
            contentDescription = comment.authorName,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.authorName,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = FormatUtils.formatDate(comment.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = FormatUtils.formatCount(count),
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun createShareCardBitmapInternal(note: Note, isDarkTheme: Boolean): Bitmap {
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgColor = if (isDarkTheme) android.graphics.Color.parseColor("#FF1A1A1A") else android.graphics.Color.parseColor("#FFFFFBFB")
    val textColor = if (isDarkTheme) android.graphics.Color.parseColor("#FFFFFBFB") else android.graphics.Color.parseColor("#FF1A1A1A")
    val secondaryColor = if (isDarkTheme) android.graphics.Color.parseColor("#FFAAAAAA") else android.graphics.Color.parseColor("#FF666666")

    canvas.drawColor(bgColor)

    val padding = 60f

    val rednoteRed = RednoteRed.toArgb()

    val brandPaint = Paint().apply {
        color = rednoteRed
        isAntiAlias = true
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }

    canvas.drawText("RED", padding, padding + 60f, brandPaint)
    brandPaint.textSize = 48f
    canvas.drawText("笔记", padding + 140f, padding + 60f, brandPaint)

    val titlePaint = Paint().apply {
        color = textColor
        isAntiAlias = true
        textSize = 72f
        typeface = Typeface.DEFAULT_BOLD
    }

    val titleLines = wrapTextForShare(note.title, titlePaint, 960f)
    var y = padding + 200f
    for (line in titleLines) {
        canvas.drawText(line, padding, y, titlePaint)
        y += 100f
    }

    val contentPaint = Paint().apply {
        color = secondaryColor
        isAntiAlias = true
        textSize = 48f
        typeface = Typeface.DEFAULT
    }

    val maxContentLines = 8
    val contentLines = wrapTextForShare(note.content, contentPaint, 960f).take(maxContentLines)
    y += 40f
    for (line in contentLines) {
        canvas.drawText(line, padding, y, contentPaint)
        y += 70f
    }

    val tagY = height - 300f
    if (note.tags.isNotEmpty()) {
        val tagPaint = Paint().apply {
            color = rednoteRed
            isAntiAlias = true
            textSize = 40f
            typeface = Typeface.DEFAULT
        }

        val tagsText = note.tags.take(5).joinToString(" ") { "#$it" }
        canvas.drawText(tagsText, padding, tagY, tagPaint)
    }

    val footerY = height - 150f
    val footerPaint = Paint().apply {
        color = secondaryColor
        isAntiAlias = true
        textSize = 36f
        typeface = Typeface.DEFAULT
    }

    val footerText = "来自 RED笔记 · ${FormatUtils.formatDate(note.createdAt)}"
    canvas.drawText(footerText, padding, footerY, footerPaint)

    val statsY = height - 80f
    val statsPaint = Paint().apply {
        color = rednoteRed
        isAntiAlias = true
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
    }

    val likeText = "♥ ${note.likeCount}"
    val collectText = "★ ${note.collectCount}"
    canvas.drawText(likeText, padding, statsY, statsPaint)
    canvas.drawText(collectText, padding + 200f, statsY, statsPaint)

    return bitmap
}

private fun wrapTextForShare(text: String, paint: Paint, maxWidth: Float): List<String> {
    val lines = mutableListOf<String>()
    if (text.isEmpty()) return lines

    val sb = StringBuilder()
    for (char in text) {
        val testLine = sb.toString() + char
        val textWidth = paint.measureText(testLine)

        if (textWidth > maxWidth && sb.isNotEmpty()) {
            lines.add(sb.toString())
            sb.clear()
            sb.append(char)
        } else {
            sb.append(char)
        }
    }

    if (sb.isNotEmpty()) {
        lines.add(sb.toString())
    }

    return lines
}

private fun shareImage(context: Context, bitmap: Bitmap, title: String) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "share_note_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "分享笔记"))
    } catch (e: Exception) {
        Logger.e("NoteDetailScreen", "Failed to share image", e)
    }
}
