package io.sylphy.app.ui.screens.library

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.ui.components.shared.ButtonVariant
import io.sylphy.app.ui.components.shared.SylphyButton
import io.sylphy.app.ui.components.shared.SylphyDivider
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permission = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(Spacing.lg),
    ) {
        SylphyButton(
            text = if (permission.status.isGranted) "Scan Library" else "Grant Permission",
            variant = ButtonVariant.Outline,
            onClick = {
                if (permission.status.isGranted) viewModel.scanLibrary()
                else permission.launchPermissionRequest()
            },
        )

        Spacer(Modifier.height(Spacing.md))

        AnimatedVisibility(uiState.scanStatus is ScanProgress.Scanning) {
            val progress = (uiState.scanStatus as? ScanProgress.Scanning)?.progress ?: 0f
            val found = (uiState.scanStatus as? ScanProgress.Scanning)?.found ?: 0
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.px1),
                    color = FgPrimary,
                    trackColor = FgSubtle,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Scanning  $found  ·  ${(progress * 100).toInt()}%",
                    style = SylphyType.CodeSmall,
                    color = FgMuted,
                )
                Spacer(Modifier.height(Spacing.md))
            }
        }

        LazyColumn {
            items(
                items = uiState.tracks,
                key = { it.id },
            ) { track ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playTrack(track, uiState.tracks) }
                        .padding(vertical = Spacing.sm),
                ) {
                    Text(
                        text = track.title,
                        style = SylphyType.Code,
                        color = FgPrimary,
                        maxLines = 1,
                    )
                    Text(
                        text = track.artist,
                        style = SylphyType.BodySmall,
                        color = FgMuted,
                        maxLines = 1,
                    )
                }
                SylphyDivider()
            }
        }
    }
}
