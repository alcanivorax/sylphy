package io.sylphy.app.ui.components.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSecondary
import io.sylphy.app.ui.theme.NothingRed
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyEasing
import io.sylphy.app.ui.theme.SylphyType
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun TrackInfoSection(
    track: Track?,
    modifier: Modifier = Modifier,
) {
    val isNothingOS = MaterialTheme.colorScheme.primary == NothingRed

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = track?.title ?: "\u2014",
            transitionSpec = {
                fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) togetherWith
                    fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit))
            },
            label = "track_title",
        ) { title ->
            Text(
                text = if (isNothingOS) title.uppercase() else title,
                style = SylphyType.DisplayLarge,
                color = if (track == null) FgMuted else FgPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    velocity = 28.dp,
                    repeatDelayMillis = 2000,
                    initialDelayMillis = 2000,
                ),
            )
        }

        Spacer(Modifier.height(Spacing.xs))

        AnimatedContent(
            targetState = track?.artist.orEmpty(),
            transitionSpec = {
                fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) togetherWith
                    fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit))
            },
            label = "track_artist",
        ) { artist ->
            Text(
                text = artist,
                style = SylphyType.Body,
                color = FgSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
