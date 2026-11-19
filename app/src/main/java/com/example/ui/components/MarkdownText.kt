package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.TerracottaCoral

/**
 * Clean, zero-raw-syntax Markdown renderer for Study AI answers and Mentor replies.
 * Renders headings (#, ##, ###), bold (**text**), italic (*text*), inline code (`text`),
 * bullet lists (- / * / •), numbered steps (1. / 2.), and dividers cleanly.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = EspressoInk,
    baseStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val lines = remember(markdown) {
        markdown
            .replace("\r\n", "\n")
            .split("\n")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        lines.forEach { rawLine ->
            val trimmed = rawLine.trim()
            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                trimmed == "---" || trimmed == "***" -> {
                    HorizontalDivider(
                        color = CocoaSecondary.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }

                trimmed.startsWith("### ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("### ").trim(), color),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TerracottaCoral
                    )
                }

                trimmed.startsWith("## ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("## ").trim(), color),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = color
                    )
                }

                trimmed.startsWith("# ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("# ").trim(), color),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = color
                    )
                }

                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    val content = trimmed.drop(2).trim()
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "•",
                            style = baseStyle.copy(fontWeight = FontWeight.ExtraBold),
                            color = LeafGreen,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(content, color),
                            style = baseStyle,
                            color = color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                NUMBERED_LIST_REGEX.matches(trimmed) -> {
                    val match = NUMBERED_LIST_REGEX.find(trimmed)
                    val num = match?.groupValues?.getOrNull(1) ?: "1"
                    val content = match?.groupValues?.getOrNull(2)?.trim().orEmpty()
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$num.",
                            style = baseStyle.copy(fontWeight = FontWeight.Bold),
                            color = TerracottaCoral
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = parseInlineMarkdown(content, color),
                            style = baseStyle,
                            color = color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                else -> {
                    Text(
                        text = parseInlineMarkdown(trimmed, color),
                        style = baseStyle,
                        color = color
                    )
                }
            }
        }
    }
}

private val NUMBERED_LIST_REGEX = Regex("^(\\d+)[.)]\\s+(.+)$")

internal fun parseInlineMarkdown(text: String, baseColor: Color): AnnotatedString =
    buildAnnotatedString {
        var i = 0
        val len = text.length
        while (i < len) {
            when {
                // Bold: **...**
                i + 1 < len && text[i] == '*' && text[i + 1] == '*' -> {
                    val end = text.indexOf("**", startIndex = i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = baseColor)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        i += 2 // skip stray **
                    }
                }
                // Bold: __...__
                i + 1 < len && text[i] == '_' && text[i + 1] == '_' -> {
                    val end = text.indexOf("__", startIndex = i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = baseColor)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        i += 2
                    }
                }
                // Inline code: `...`
                text[i] == '`' -> {
                    val end = text.indexOf('`', startIndex = i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                background = Color(0x22C47A3A)
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        i += 1
                    }
                }
                // Italic: *...*
                text[i] == '*' -> {
                    val end = text.indexOf('*', startIndex = i + 1)
                    if (end != -1 && end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        i += 1 // skip stray *
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
