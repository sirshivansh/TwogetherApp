package com.example.ui.components

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.os.LocaleListCompat
import com.example.R
import com.example.ui.theme.ClayShadowTint
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.ElevatedClayCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.WarmSandBorder
import java.util.Locale

/**
 * English + all 22 Scheduled Indian Languages (Eighth Schedule of the Constitution of India).
 */
enum class AppLanguageOption(
    val tag: String,
    val nativeLabel: String,
    val englishName: String,
    val shortBadge: String,
    val testTag: String
) {
    ENGLISH("en", "English", "English", "EN", "lang_option_en"),
    HINDI("hi", "हिन्दी", "Hindi", "हि", "lang_option_hi"),
    MARATHI("mr", "मराठी", "Marathi", "म", "lang_option_mr"),
    ASSAMESE("as", "অসমীয়া", "Assamese", "অ", "lang_option_as"),
    BENGALI("bn", "বাংলা", "Bengali", "বা", "lang_option_bn"),
    BODO("brx", "बड़ो", "Bodo", "ब", "lang_option_brx"),
    DOGRI("doi", "डोगरी", "Dogri", "डो", "lang_option_doi"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "ગુ", "lang_option_gu"),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada", "ಕ", "lang_option_kn"),
    KASHMIRI("ks", "کٲشُر", "Kashmiri", "ک", "lang_option_ks"),
    KONKANI("kok", "कोंकणी", "Konkani", "कों", "lang_option_kok"),
    MAITHILI("mai", "मैथिली", "Maithili", "मै", "lang_option_mai"),
    MALAYALAM("ml", "മലയാളം", "Malayalam", "മ", "lang_option_ml"),
    MANIPURI("mni", "মৈতৈলোন্", "Manipuri (Meitei)", "মৈ", "lang_option_mni"),
    NEPALI("ne", "नेपाली", "Nepali", "ने", "lang_option_ne"),
    ODIA("or", "ଓଡ଼ିଆ", "Odia", "ଓ", "lang_option_or"),
    PUNJABI("pa", "ਪੰਜਾਬੀ", "Punjabi", "ਪੰ", "lang_option_pa"),
    SANSKRIT("sa", "संस्कृतम्", "Sanskrit", "सं", "lang_option_sa"),
    SANTALI("sat", "ᱥᱟᱱᱛᱟᱲᱤ", "Santali", "ᱥ", "lang_option_sat"),
    SINDHI("sd", "سنڌي", "Sindhi", "س", "lang_option_sd"),
    TAMIL("ta", "தமிழ்", "Tamil", "த", "lang_option_ta"),
    TELUGU("te", "తెలుగు", "Telugu", "తె", "lang_option_te"),
    URDU("ur", "اردو", "Urdu", "اُ", "lang_option_ur");

    companion object {
        fun fromTag(tag: String?): AppLanguageOption {
            val clean = tag?.substringBefore("-")?.substringBefore("_")?.lowercase(Locale.US)
            return entries.firstOrNull { it.tag == clean } ?: ENGLISH
        }
    }
}

fun getCurrentAppLanguage(context: Context): AppLanguageOption {
    val appLocale = AppCompatDelegate.getApplicationLocales()
    val tag = if (!appLocale.isEmpty) {
        appLocale[0]?.language
    } else {
        val locales = context.resources.configuration.locales
        if (!locales.isEmpty) locales[0]?.language else Locale.getDefault().language
    }
    return AppLanguageOption.fromTag(tag)
}

fun setAppLanguage(tag: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
}

/**
 * Resolves a Context configured with the currently active AppCompat per-app locale
 * so non-UI classes (Repository, Workers, TtsSpeaker) always use the selected language.
 */
fun Context.localizedForCurrentLocale(): Context {
    val appLocale = AppCompatDelegate.getApplicationLocales()
    val targetLocale = if (!appLocale.isEmpty) appLocale[0] else null
    if (targetLocale == null) return this
    val config = Configuration(resources.configuration)
    config.setLocale(targetLocale)
    return createConfigurationContext(config)
}

/**
 * Globe icon button in the Top-Right that opens a scrollable, searchable Claymorphic
 * Animated Dropdown supporting English + all 22 Indian Scheduled Languages.
 */
@Composable
fun ClayLanguageSelectorButton(
    modifier: Modifier = Modifier,
    onLanguageSelected: ((AppLanguageOption) -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showDropdown by remember { mutableStateOf(false) }
    val currentOption = getCurrentAppLanguage(context)
    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(showDropdown) {
        transitionState.targetState = showDropdown
    }

    Box(modifier = modifier) {
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showDropdown = !showDropdown
            },
            shape = RoundedCornerShape(18.dp),
            color = SunAmberSoft,
            border = BorderStroke(1.3.dp, WarmSandBorder.copy(alpha = 0.85f)),
            shadowElevation = 3.dp,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("language_selector_globe_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Language,
                    contentDescription = stringResource(R.string.cd_language_selector),
                    tint = EspressoInk,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = currentOption.shortBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = EspressoInk,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (showDropdown || transitionState.currentState || !transitionState.isIdle) {
            val offsetY = with(LocalDensity.current) { 52.dp.roundToPx() }
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = 0, y = offsetY),
                onDismissRequest = { showDropdown = false },
                properties = PopupProperties(focusable = true)
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(animationSpec = tween(180)),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(150)
                    ) + fadeOut(animationSpec = tween(140))
                ) {
                    ClayLanguageDropdownMenu(
                        currentOption = currentOption,
                        onSelect = { option ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDropdown = false
                            setAppLanguage(option.tag)
                            onLanguageSelected?.invoke(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClayLanguageDropdownMenu(
    currentOption: AppLanguageOption,
    onSelect: (AppLanguageOption) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredOptions = remember(searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) {
            AppLanguageOption.entries
        } else {
            AppLanguageOption.entries.filter {
                it.englishName.lowercase(Locale.US).contains(q) ||
                    it.nativeLabel.lowercase(Locale.US).contains(q) ||
                    it.tag.lowercase(Locale.US).contains(q)
            }
        }
    }

    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .padding(4.dp)
            .widthIn(min = 264.dp, max = 304.dp)
            .drawBehind {
                val r = 24.dp.toPx()
                drawRoundRect(
                    color = ClayShadowTint.copy(alpha = 0.22f),
                    topLeft = Offset(2.dp.toPx(), 7.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(r, r)
                )
            }
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ElevatedClayCard, SoftOatSurface)
                )
            )
            .border(1.5.dp, WarmSandBorder.copy(alpha = 0.85f), shape)
            .drawBehind {
                val r = 24.dp.toPx()
                val inset = 2.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.88f),
                            Color.White.copy(alpha = 0.25f),
                            ClayShadowTint.copy(alpha = 0.14f)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                    cornerRadius = CornerRadius(r - inset, r - inset),
                    style = Stroke(width = 2.2.dp.toPx())
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.language_menu_title) + " (22+1)",
                    style = MaterialTheme.typography.labelLarge,
                    color = CocoaSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.language_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.common_close),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.85f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.72f),
                    unfocusedBorderColor = WarmSandBorder.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_search_input")
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredOptions, key = { it.tag }) { option ->
                    val isSelected = option == currentOption
                    val rowBg = if (isSelected) LeafGreenSoft else Color.White.copy(alpha = 0.76f)
                    val rowBorder = if (isSelected) LeafGreen.copy(alpha = 0.45f) else WarmSandBorder.copy(alpha = 0.4f)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = rowBg,
                        border = BorderStroke(1.2.dp, rowBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelect(option) }
                            .testTag(option.testTag)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) LeafGreen else SunAmberSoft
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.shortBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else EspressoInk,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                                Column {
                                    Text(
                                        text = option.nativeLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = EspressoInk,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (option.nativeLabel != option.englishName) {
                                        Text(
                                            text = option.englishName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CocoaSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            if (isSelected) {
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = stringResource(R.string.language_active_badge),
                                    tint = LeafGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
