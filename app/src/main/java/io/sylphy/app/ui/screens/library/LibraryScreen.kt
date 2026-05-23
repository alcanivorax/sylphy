package io.sylphy.app.ui.screens.library

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sylphy.app.core.util.toHhMm
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.core.util.toTrackCountLabel
import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.data.model.Album
import io.sylphy.app.data.model.Artist
import io.sylphy.app.data.model.Playlist
import io.sylphy.app.data.model.ThemeMode
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.components.player.AlbumArtwork
import io.sylphy.app.ui.components.shared.ContextMenuAction
import io.sylphy.app.ui.components.shared.ContextMenuSheet
import io.sylphy.app.ui.navigation.Screen
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.BgSunken
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ContainerCorner
import io.sylphy.app.ui.theme.DmSans
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.LibraryChromeColors
import io.sylphy.app.ui.theme.SpaceMono
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType
import io.sylphy.app.ui.theme.libraryChromeColors

private val SmallShape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel(),
    themeMode: ThemeMode = ThemeMode.MONOCHROME_DARK,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = libraryChromeColors(themeMode)
    var contextMenuTrack by remember { mutableStateOf<Track?>(null) }
    var playlistTarget by remember { mutableStateOf<Track?>(null) }
    var showCreatePlaylist by remember { mutableStateOf(false) }

    val mediaPermission =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    var hasMediaPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, mediaPermission) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasMediaPermission = granted
        if (granted) viewModel.scanLibrary()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        FadeUp(delayMillis = 40) {
            LibraryHeader(
                colors = colors,
                hasPermission = hasMediaPermission,
                isScanning = uiState.scanStatus is ScanProgress.Scanning,
                onStats = { navController.navigate(Screen.Stats.route) },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onScan = {
                    if (hasMediaPermission) viewModel.scanLibrary()
                    else permissionLauncher.launch(mediaPermission)
                },
            )
        }

        FadeUp(delayMillis = 90) {
            LibrarySearch(
                colors = colors,
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
            )
        }

        ScanProgressBar(
            colors = colors,
            scanStatus = uiState.scanStatus,
            modifier = Modifier.padding(horizontal = 22.dp),
        )

        FadeUp(delayMillis = 130) {
            LibraryTabs(
                colors = colors,
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab,
            )
        }

        FadeUp(
            delayMillis = 180,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val searchResults = uiState.searchResults
            if (searchResults != null) {
                SearchResultsList(
                    colors = colors,
                    results = searchResults,
                    currentTrackId = uiState.currentTrackId,
                    isPlaying = uiState.isPlaying,
                    onTrackClick = { viewModel.playTrack(it, searchResults.tracks) },
                    onTrackLongClick = { contextMenuTrack = it },
                    onAlbumClick = { navController.navigate(Screen.AlbumDetail.route(it.id)) },
                    onArtistClick = { navController.navigate(Screen.ArtistDetail.route(it.id)) },
                )
            } else {
                when (uiState.selectedTab) {
                    LibraryTab.Songs -> SongsTab(
                        colors = colors,
                        tracks = uiState.tracks,
                        recentlyPlayed = uiState.recentlyPlayed,
                        currentTrackId = uiState.currentTrackId,
                        isPlaying = uiState.isPlaying,
                        onTrackClick = { viewModel.playTrack(it, uiState.tracks) },
                        onTrackLongClick = { contextMenuTrack = it },
                    )
                    LibraryTab.Albums -> AlbumsTab(
                        colors = colors,
                        albums = uiState.albums,
                        onAlbumClick = { navController.navigate(Screen.AlbumDetail.route(it.id)) },
                    )
                    LibraryTab.Artists -> ArtistsTab(
                        colors = colors,
                        artists = uiState.artists,
                        onArtistClick = { navController.navigate(Screen.ArtistDetail.route(it.id)) },
                    )
                    LibraryTab.Playlists -> PlaylistsTab(
                        colors = colors,
                        playlists = uiState.playlists,
                        onCreate = { showCreatePlaylist = true },
                        onPlaylistClick = { navController.navigate(Screen.PlaylistDetail.route(it.id)) },
                    )
                }
            }
        }
    }

    contextMenuTrack?.let { track ->
        ContextMenuSheet(
            track = track,
            onDismiss = { contextMenuTrack = null },
            actions = listOf(
                ContextMenuAction(
                    label = "Play next",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.playTrack(track, listOf(track)) },
                ),
                ContextMenuAction(
                    label = "Add to playlist",
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    onClick = { playlistTarget = track },
                ),
                ContextMenuAction(
                    label = if (track.isFavorite) "Remove favourite" else "Favourite",
                    icon = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    onClick = { viewModel.toggleFavorite(track) },
                ),
            ),
        )
    }

    playlistTarget?.let { track ->
        PlaylistPickerDialog(
            playlists = uiState.playlists,
            onDismiss = { playlistTarget = null },
            onCreate = {
                playlistTarget = null
                showCreatePlaylist = true
            },
            onPick = {
                viewModel.addToPlaylist(it.id, track.id)
                playlistTarget = null
            },
        )
    }

    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylist = false },
            onCreate = {
                viewModel.createPlaylist(it)
                showCreatePlaylist = false
            },
        )
    }
}

@Composable
private fun LibraryHeader(
    colors: LibraryChromeColors,
    hasPermission: Boolean,
    isScanning: Boolean,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    onScan: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "LIBRARY",
            fontFamily = SpaceMono,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.fg,
            letterSpacing = 0.88.sp,
            lineHeight = 22.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onStats, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.QueryStats,
                    contentDescription = "Sort and stats",
                    tint = colors.muted2,
                    modifier = Modifier.size(17.dp),
                )
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.muted2,
                    modifier = Modifier.size(17.dp),
                )
            }
            Text(
                text = when {
                    isScanning -> "Scanning"
                    hasPermission -> "Scan"
                    else -> "Allow"
                },
                fontFamily = SpaceMono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.muted2,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .clip(SmallShape)
                    .border(1.dp, colors.border2, SmallShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onScan,
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun LibrarySearch(
    colors: LibraryChromeColors,
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = DmSans,
            fontSize = 14.sp,
            color = colors.fg,
        ),
        cursorBrush = SolidColor(colors.fg),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 14.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(42.dp)
                    .fillMaxWidth()
                    .clip(SmallShape)
                    .background(colors.searchBg)
                    .border(1.dp, colors.searchBorder, SmallShape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.muted2,
                    modifier = Modifier.size(15.dp),
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Search songs, albums, artists",
                            fontFamily = DmSans,
                            fontSize = 14.sp,
                            color = colors.muted2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
                if (value.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = colors.muted2,
                        modifier = Modifier
                            .size(17.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onValueChange("") },
                    )
                }
            }
        },
    )
}

@Composable
private fun ScanProgressBar(
    colors: LibraryChromeColors,
    scanStatus: ScanProgress,
    modifier: Modifier = Modifier,
) {
    val scanning = scanStatus as? ScanProgress.Scanning
    AnimatedVisibility(
        visible = scanning != null,
        enter = fadeIn(tween(Duration.Normal)),
        exit = fadeOut(tween(Duration.Slow)),
    ) {
        scanning?.let {
            Column(modifier = modifier.padding(top = 8.dp)) {
                LinearProgressIndicator(
                    progress = { it.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(CircleShape),
                    color = colors.accent,
                    trackColor = colors.border,
                )
                Text(
                    text = "SCANNING ${it.found} TRACKS · ${(it.progress * 100).toInt()}%",
                    fontFamily = SpaceMono,
                    fontSize = 9.sp,
                    color = colors.muted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun LibraryTabs(
    colors: LibraryChromeColors,
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .drawBehind {
                drawLine(
                    color = colors.border,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        LibraryTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier
                    .padding(end = if (tab == LibraryTab.Playlists) 0.dp else 22.dp)
                    .height(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onTabSelected(tab) },
                contentAlignment = Alignment.TopStart,
            ) {
                Text(
                    text = tab.name,
                    fontFamily = SpaceMono,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) colors.fg else colors.muted2,
                    letterSpacing = 1.33.sp,
                    lineHeight = 12.sp,
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(colors.accent),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongsTab(
    colors: LibraryChromeColors,
    tracks: List<Track>,
    recentlyPlayed: List<Track>,
    currentTrackId: String?,
    isPlaying: Boolean,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
) {
    val sectioned = remember(tracks) {
        tracks.groupBy {
            it.title.firstOrNull()?.uppercaseChar()?.takeIf { c -> c.isLetter() }?.toString() ?: "#"
        }.toSortedMap(compareBy { if (it == "#") "\u0000" else it })
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (recentlyPlayed.isNotEmpty()) {
            item(key = "recent") {
                RecentSection(colors = colors, tracks = recentlyPlayed.take(12), onTrackClick = onTrackClick)
                Divider(colors = colors)
            }
        }

        sectioned.forEach { (letter, sectionTracks) ->
            item(key = "header_$letter") { AlphaHeader(colors = colors, title = letter) }
            itemsIndexed(sectionTracks, key = { _, track -> track.id }) { index, track ->
                LibrarySongRow(
                    colors = colors,
                    track = track,
                    isActive = track.id == currentTrackId,
                    isPlaying = isPlaying && track.id == currentTrackId,
                    showDivider = index > 0,
                    onTrackClick = onTrackClick,
                    onTrackLongClick = onTrackLongClick,
                )
            }
        }
    }
}

@Composable
private fun RecentSection(
    colors: LibraryChromeColors,
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(colors = colors, text = "Recent")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tracks, key = { it.id }) { track ->
                RecentCard(colors = colors, track = track, onClick = { onTrackClick(track) })
            }
        }
    }
}

@Composable
private fun RecentCard(
    colors: LibraryChromeColors,
    track: Track,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(108.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Text(
            text = track.title,
            fontFamily = DmSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.6.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = track.artist,
            fontFamily = SpaceMono,
            fontSize = 9.sp,
            color = colors.muted2,
            letterSpacing = 0.54.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun SectionLabel(colors: LibraryChromeColors, text: String) {
    Text(
        text = text,
        fontFamily = SpaceMono,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = colors.muted,
        letterSpacing = 1.8.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 10.dp),
    )
}

@Composable
private fun Divider(colors: LibraryChromeColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 14.dp)
            .height(1.dp)
            .background(colors.border),
    )
}

@Composable
private fun AlphaHeader(colors: LibraryChromeColors, title: String) {
    Text(
        text = title,
        fontFamily = SpaceMono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = colors.sectionLetter,
        letterSpacing = 1.1.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 4.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibrarySongRow(
    colors: LibraryChromeColors,
    track: Track,
    isActive: Boolean,
    isPlaying: Boolean,
    showDivider: Boolean,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "library_song_row_scale",
    )
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .scale(scale)
            .background(if (pressed) colors.accentDim else Color.Transparent)
            .drawBehind {
                if (showDivider) {
                    drawLine(
                        color = colors.border,
                        start = Offset(80.dp.toPx(), 0f),
                        end = Offset(size.width - 22.dp.toPx(), 0f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTrackClick(track)
                },
                onLongClick = { onTrackLongClick(track) },
            )
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontFamily = DmSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) colors.playIndicator else colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.2.sp,
            )
            Text(
                text = track.artist,
                fontFamily = SpaceMono,
                fontSize = 9.5.sp,
                color = colors.muted2,
                letterSpacing = 0.57.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (isPlaying) {
            EqBars(color = colors.playIndicator)
        }
        Text(
            text = track.durationMs.toMmSs(),
            fontFamily = SpaceMono,
            fontSize = 10.sp,
            color = colors.muted,
            letterSpacing = 0.4.sp,
        )
    }
}

@Composable
private fun EqBars(color: Color) {
    Row(
        modifier = Modifier.size(width = 14.dp, height = 14.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(0.6f, 1f, 0.4f).forEachIndexed { index, heightFraction ->
            val scale = remember { Animatable(0.3f) }
            LaunchedEffect(index) {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, delayMillis = index * 200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                )
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(heightFraction * scale.value)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(1.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun AlbumsTab(
    colors: LibraryChromeColors,
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(albums, key = { it.id }) { album ->
            LibraryGridCard(
                colors = colors,
                title = album.title,
                subtitle = album.artist,
                artworkPath = album.artworkPath,
                onClick = { onAlbumClick(album) },
            )
        }
    }
}

@Composable
private fun ArtistsTab(
    colors: LibraryChromeColors,
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(artists, key = { _, artist -> artist.id }) { index, artist ->
            EntityRow(
                colors = colors,
                title = artist.name,
                subtitle = "${artist.albumCount} albums · ${artist.trackCount.toTrackCountLabel()}",
                leading = null,
                showDivider = index > 0,
                onClick = { onArtistClick(artist) },
            )
        }
    }
}

@Composable
private fun PlaylistsTab(
    colors: LibraryChromeColors,
    playlists: List<Playlist>,
    onCreate: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "create_playlist") {
            EntityRow(
                colors = colors,
                title = "Create playlist",
                subtitle = "New custom queue",
                leading = Icons.Default.Add,
                showDivider = false,
                onClick = onCreate,
            )
        }
        itemsIndexed(playlists, key = { _, playlist -> playlist.id }) { index, playlist ->
            EntityRow(
                colors = colors,
                title = playlist.name,
                subtitle = "${playlist.trackCount.toTrackCountLabel()} · ${playlist.durationMs.toHhMm()}",
                leading = null,
                showDivider = index >= 0,
                onClick = { onPlaylistClick(playlist) },
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    colors: LibraryChromeColors,
    results: SearchResults,
    currentTrackId: String?,
    isPlaying: Boolean,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (results.tracks.isEmpty() && results.albums.isEmpty() && results.artists.isEmpty()) {
            item { SectionLabel(colors = colors, text = "No Results") }
        }
        if (results.tracks.isNotEmpty()) {
            item { SectionLabel(colors = colors, text = "Songs") }
            itemsIndexed(results.tracks, key = { _, track -> track.id }) { index, track ->
                LibrarySongRow(
                    colors = colors,
                    track = track,
                    isActive = track.id == currentTrackId,
                    isPlaying = isPlaying && track.id == currentTrackId,
                    showDivider = index > 0,
                    onTrackClick = onTrackClick,
                    onTrackLongClick = onTrackLongClick,
                )
            }
        }
        if (results.albums.isNotEmpty()) {
            item { SectionLabel(colors = colors, text = "Albums") }
            items(results.albums, key = { it.id }) { album ->
                EntityRow(
                    colors = colors,
                    title = album.title,
                    subtitle = album.artist,
                    leading = null,
                    showDivider = true,
                    onClick = { onAlbumClick(album) },
                )
            }
        }
        if (results.artists.isNotEmpty()) {
            item { SectionLabel(colors = colors, text = "Artists") }
            items(results.artists, key = { it.id }) { artist ->
                EntityRow(
                    colors = colors,
                    title = artist.name,
                    subtitle = artist.trackCount.toTrackCountLabel(),
                    leading = null,
                    showDivider = true,
                    onClick = { onArtistClick(artist) },
                )
            }
        }
    }
}

@Composable
private fun LibraryGridCard(
    colors: LibraryChromeColors,
    title: String,
    subtitle: String,
    artworkPath: String?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
    ) {
        Text(
            text = title,
            fontFamily = DmSans,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = subtitle,
            fontFamily = SpaceMono,
            fontSize = 9.sp,
            color = colors.muted2,
            letterSpacing = 0.54.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun EntityRow(
    colors: LibraryChromeColors,
    title: String,
    subtitle: String,
    leading: ImageVector?,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .drawBehind {
                if (showDivider) {
                    drawLine(
                        color = colors.border,
                        start = Offset(80.dp.toPx(), 0f),
                        end = Offset(size.width - 22.dp.toPx(), 0f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(SmallShape)
                .background(colors.surface2)
                .border(1.dp, colors.border2, SmallShape),
            contentAlignment = Alignment.Center,
        ) {
            if (leading != null) {
                Icon(imageVector = leading, contentDescription = null, tint = colors.fg, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = title.take(1).uppercase(),
                    fontFamily = SpaceMono,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.muted,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = DmSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                fontFamily = SpaceMono,
                fontSize = 9.5.sp,
                color = colors.muted2,
                letterSpacing = 0.57.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun FadeUp(
    delayMillis: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(10f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(durationMillis = 450, delayMillis = delayMillis))
    }
    LaunchedEffect(Unit) {
        y.animateTo(0f, tween(durationMillis = 450, delayMillis = delayMillis))
    }
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = y.value
        },
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackRow(
    track: Track,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "track_row_scale",
    )
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Layout.trackRowHeight)
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTrackClick(track)
                },
                onLongClick = { onTrackLongClick(track) },
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArtwork(artworkPath = track.artworkPath, size = Layout.albumArtSizeSm)
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.title, style = SylphyType.Code, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = track.artist, style = SylphyType.BodySmall, color = FgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(text = track.durationMs.toMmSs(), style = SylphyType.CodeSmall, color = FgSubtle)
    }
}

@Composable
internal fun SectionHeader(title: String) {
    Text(
        text = title,
        style = SylphyType.CodeSmall,
        color = FgMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    )
}

@Composable
private fun PlaylistPickerDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onPick: (Playlist) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElevated,
        title = { Text(text = "Add to playlist", style = SylphyType.Heading, color = FgPrimary) },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = "Create new playlist",
                        style = SylphyType.Body,
                        color = FgPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onCreate)
                            .padding(vertical = Spacing.sm),
                    )
                }
                items(playlists, key = { it.id }) { playlist ->
                    Text(
                        text = playlist.name,
                        style = SylphyType.Body,
                        color = FgPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(playlist) }
                            .padding(vertical = Spacing.sm),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = SylphyType.Body, color = FgMuted)
            }
        },
    )
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElevated,
        title = { Text(text = "Create playlist", style = SylphyType.Heading, color = FgPrimary) },
        text = {
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = SylphyType.Body.copy(color = FgPrimary),
                cursorBrush = SolidColor(FgPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (name.isNotBlank()) onCreate(name.trim())
                }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(ContainerCorner)
                            .background(BgSunken)
                            .border(Layout.borderThin, BorderDefault, ContainerCorner)
                            .padding(horizontal = Spacing.md),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (name.isEmpty()) {
                            Text(text = "Playlist name", style = SylphyType.Body, color = FgMuted)
                        }
                        innerTextField()
                    }
                },
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name.trim()) }, enabled = name.isNotBlank()) {
                Text(text = "Create", style = SylphyType.Body, color = if (name.isNotBlank()) FgPrimary else FgSubtle)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = SylphyType.Body, color = FgMuted)
            }
        },
    )
}
