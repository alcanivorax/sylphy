package io.sylphy.app.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ContainerCorner
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun AlbumArtwork(
    artworkPath: String?,
    modifier: Modifier = Modifier,
    size: Dp = Layout.albumArtSize,
) {
    val context = LocalContext.current
    val sizedModifier = if (size == Dp.Unspecified) modifier else modifier.size(size)

    Box(
        modifier = sizedModifier
            .clip(ContainerCorner)
            .border(Layout.borderThin, BorderDefault, ContainerCorner)
            .background(BgElevated),
        contentAlignment = Alignment.Center,
    ) {
        if (!artworkPath.isNullOrBlank()) {
            val imageData = when {
                artworkPath.startsWith("content://") -> artworkPath
                artworkPath.startsWith("file://") -> artworkPath
                else -> "file://$artworkPath"
            }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageData)
                    .crossfade(Duration.Normal)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = "-",
                style = SylphyType.Display,
                color = FgSubtle,
            )
        }
    }
}
