package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ClayCard

val TwogetherBrandCrimson = Color(0xFFA81920)
val TwogetherBrandPurple = Color(0xFF6A1B7A)
val TwogetherBrandSlate = Color(0xFF22313F)

/**
 * Brand logo card matching the official Twogether visual identity:
 * crisp white canvas, dark-slate mentor & student desk icon,
 * bold crimson "Twogether" wordmark, and deep purple serif tagline.
 */
@Composable
fun TwogetherBrandHeader(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    ClayCard(
        containerColor = Color.White,
        cornerRadius = if (compact) 22.dp else 28.dp,
        elevationDp = if (compact) 4.dp else 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("twogether_brand_header")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 16.dp else 22.dp,
                    vertical = if (compact) 14.dp else 22.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_twogether_logo),
                contentDescription = stringResource(R.string.auth_hero_cd),
                modifier = Modifier.size(
                    width = if (compact) 74.dp else 104.dp,
                    height = if (compact) 58.dp else 80.dp
                )
            )
            Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = TwogetherBrandCrimson,
                fontSize = if (compact) 30.sp else 40.sp,
                lineHeight = if (compact) 34.sp else 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 13.sp else 15.sp,
                    lineHeight = if (compact) 18.sp else 21.sp
                ),
                color = TwogetherBrandPurple,
                textAlign = TextAlign.Center
            )
        }
    }
}
