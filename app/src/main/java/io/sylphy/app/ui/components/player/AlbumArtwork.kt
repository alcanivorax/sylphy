package io.sylphy.app.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ContainerCorner
import io.sylphy.app.ui.theme.Layout

@Composable
fun AlbumArtwork(
    artworkPath: String?,
    modifier: Modifier = Modifier,
    size: Dp = Layout.albumArtSize,
) {
    val sizedModifier = if (size == Dp.Unspecified) modifier else modifier.size(size)

    Box(
        modifier = sizedModifier
            .clip(ContainerCorner)
            .background(BgElevated)
            .border(Layout.borderThin, BorderDefault, ContainerCorner),
    )
}
