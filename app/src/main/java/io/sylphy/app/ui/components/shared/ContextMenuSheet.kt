package io.sylphy.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.Black
import io.sylphy.app.ui.theme.BottomSheetCorner
import io.sylphy.app.ui.theme.ChipCorner
import io.sylphy.app.ui.theme.FgGhost
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

data class ContextMenuAction(
    val label: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextMenuSheet(
    track: Track,
    actions: List<ContextMenuAction>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgElevated,
        shape = BottomSheetCorner,
        scrimColor = Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 3.dp)
                        .background(FgSubtle, ChipCorner),
                )
            }
        },
    ) {
        // Track header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Artwork thumbnail placeholder — replaced by Coil in Phase 2
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(FgGhost, io.sylphy.app.ui.theme.ContainerCorner),
            )

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
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
        }

        SylphyDivider()

        // Action rows
        actions.forEach { action ->
            ContextMenuRow(action = action, onDismiss = onDismiss)
        }

        Spacer(Modifier.height(Spacing.lg))
    }
}

@Composable
private fun ContextMenuRow(
    action: ContextMenuAction,
    onDismiss: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                action.onClick()
                onDismiss()
            }
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            tint = FgMuted,
            modifier = Modifier.size(20.dp),
        )

        Spacer(Modifier.width(Spacing.md))

        Column {
            Text(
                text = action.label,
                style = SylphyType.Body,
                color = FgPrimary,
            )
            if (action.isDestructive) {
                Text(
                    text = "(irreversible)",
                    style = SylphyType.Caption,
                    color = FgMuted,
                )
            }
        }
    }

    HorizontalDividerIfNeeded(action)
}

@Composable
private fun HorizontalDividerIfNeeded(action: ContextMenuAction) {
    if (action.isDestructive) {
        SylphyDivider(modifier = Modifier.padding(horizontal = Spacing.md))
    }
}
