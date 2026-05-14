package io.sylphy.app.ui.components.shared

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.sylphy.app.ui.theme.BgSunken
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.BorderStrong
import io.sylphy.app.ui.theme.ContainerCorner
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun SylphySearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onFocusChange: (Boolean) -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) Layout.borderThick else Layout.borderThin,
        animationSpec = tween(Duration.Fast),
        label = "search_border",
    )
    val borderColor = if (isFocused) BorderStrong else BorderDefault

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .onFocusChanged { state ->
                isFocused = state.isFocused
                onFocusChange(state.isFocused)
            },
        singleLine = true,
        textStyle = SylphyType.Body.copy(color = FgPrimary),
        cursorBrush = SolidColor(FgPrimary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* caller handles */ }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(BgSunken, ContainerCorner)
                    .border(borderWidth, borderColor, ContainerCorner)
                    .padding(horizontal = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = FgMuted,
                    modifier = Modifier.size(16.dp),
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Spacing.sm),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = SylphyType.Body,
                            color = FgMuted,
                        )
                    }
                    innerTextField()
                }

                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = FgMuted,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        },
    )
}
