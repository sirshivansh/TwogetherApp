package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.EducatorProfileEntity
import com.example.ui.theme.ClayButton
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.GoldMid
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.TerracottaCoral
import com.example.ui.theme.WarmCreamBg
import com.example.util.ReportCardExporter
import java.util.Locale

/**
 * Educator dialog to assign real offline Homework that immediately syncs to every student's Backpack.
 */
@Composable
fun AssignHomeworkDialog(
    defaultSubject: String,
    onDismiss: () -> Unit,
    onAssign: (title: String, description: String, subject: String, gradeClass: Int, dueDateLabel: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember {
        mutableStateOf(
            when {
                defaultSubject.contains("Math", ignoreCase = true) -> "Math"
                defaultSubject.contains("Sci", ignoreCase = true) -> "Science"
                defaultSubject.contains("Eng", ignoreCase = true) -> "English"
                defaultSubject.contains("Soc", ignoreCase = true) -> "Social Studies"
                else -> "Math"
            }
        )
    }
    var gradeClass by remember { mutableIntStateOf(5) }
    var dueDate by remember { mutableStateOf("Due in 2 days") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Assignment, contentDescription = null, tint = TerracottaCoral)
                Text(
                    text = stringResource(R.string.edu_hw_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color = EspressoInk
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.edu_hw_title_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assign_hw_title_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Math", "Science", "English", "Social Studies", "Hindi")) { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick = { subject = s },
                            label = { Text(s) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_class_label, gradeClass),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(onClick = { if (gradeClass > 1) gradeClass-- }) { Text("-") }
                    OutlinedButton(onClick = { if (gradeClass < 10) gradeClass++ }) { Text("+") }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.edu_hw_desc_label)) },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assign_hw_desc_input")
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text(stringResource(R.string.edu_hw_due_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ClayButton(
                text = stringResource(R.string.edu_hw_publish_btn),
                containerColor = LeafGreen,
                enabled = title.trim().length >= 3 && description.trim().length >= 5,
                onClick = {
                    onAssign(title.trim(), description.trim(), subject, gradeClass, dueDate.trim())
                },
                modifier = Modifier.testTag("confirm_assign_hw_btn")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

/**
 * Verified Educator Impact Certificate preview + one-tap Android Share Sheet export.
 */
@Composable
fun EducatorCertificateDialog(
    educator: EducatorProfileEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val badgeTitle = when {
        educator.hoursTaught >= 50.0 -> stringResource(R.string.edu_badge_gold)
        educator.hoursTaught >= 25.0 -> stringResource(R.string.edu_badge_silver)
        educator.hoursTaught >= 10.0 -> stringResource(R.string.edu_badge_bronze)
        else -> stringResource(R.string.edu_cert_verified_mentor)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = GoldMid)
                Text(
                    text = stringResource(R.string.edu_cert_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color = EspressoInk
                )
            }
        },
        text = {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFFFFBF0),
                border = BorderStroke(2.dp, GoldMid),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = LeafGreen,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "TWOGETHER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF9F1B24)
                    )
                    Text(
                        text = stringResource(R.string.edu_cert_awarded_to),
                        style = MaterialTheme.typography.labelMedium,
                        color = CocoaSecondary
                    )
                    Text(
                        text = educator.fullName.ifBlank { stringResource(R.string.edu_volunteer_default) },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = EspressoInk,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SunAmberSoft
                    ) {
                        Text(
                            text = badgeTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EspressoInk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CertStatMini(
                            value = String.format(Locale.US, "%.1fh", educator.hoursTaught),
                            label = stringResource(R.string.edu_total_hours_taught)
                        )
                        CertStatMini(
                            value = "${educator.doubtsResolved}",
                            label = stringResource(R.string.edu_doubts_metric)
                        )
                        CertStatMini(
                            value = "${educator.lessonsUploadedCount}",
                            label = stringResource(R.string.edu_lessons_metric)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LeafGreenSoft
                    ) {
                        Text(
                            text = "${educator.subjectSpecialty} • ${educator.docVerificationStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoInk,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            val chooserTitle = stringResource(R.string.edu_cert_share_chooser)
            ClayButton(
                text = stringResource(R.string.edu_cert_share_btn),
                containerColor = LeafGreen,
                onClick = {
                    val text = ReportCardExporter.buildEducatorCertificateText(educator)
                    ReportCardExporter.sharePlainText(
                        context = context,
                        subject = "Twogether Educator Impact Certificate - ${educator.fullName}",
                        body = text,
                        chooserTitle = chooserTitle
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.White)
                },
                modifier = Modifier.testTag("share_edu_cert_btn")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
}

@Composable
private fun CertStatMini(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = EspressoInk
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CocoaSecondary,
            textAlign = TextAlign.Center
        )
    }
}
