package com.example.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Backpack
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DoubtQueueEntity
import com.example.data.local.EducatorProfileEntity
import com.example.data.local.LessonEntity
import com.example.data.local.StudentProfileEntity
import com.example.ui.components.localizedForCurrentLocale
import com.example.ui.theme.AliveAmbientBackground
import com.example.ui.theme.AlivePulseDot
import com.example.ui.theme.AliveStaggeredEntry
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayError
import com.example.ui.theme.ClayErrorContainer
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.ElevatedClayCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.ListenButton
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.StudentAvatarBadge
import com.example.ui.theme.SunAmber
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.TerracottaCoral
import com.example.ui.theme.TerracottaSoft
import com.example.ui.theme.WarmCreamBg
import com.example.util.ReportCardExporter
import com.example.util.TtsSpeaker
import java.util.Locale

private enum class AdminTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    METRICS(R.string.admin_tab_impact, Icons.Rounded.Analytics),
    STUDENTS(R.string.admin_tab_students, Icons.Rounded.Groups),
    EDUCATORS(R.string.admin_tab_educators, Icons.Rounded.School),
    CONTENT(R.string.admin_tab_content, Icons.Rounded.Backpack)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    students: List<StudentProfileEntity>,
    educators: List<EducatorProfileEntity>,
    lessons: List<LessonEntity>,
    doubts: List<DoubtQueueEntity>,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onCreateStudent: (String, String, String, Int, String, String) -> Unit,
    onCreateEducator: (String, String, String, String, String) -> Unit,
    onUpdateStudent: (StudentProfileEntity) -> Unit,
    onDeleteStudent: (String, String) -> Unit,
    onApproveEducator: (EducatorProfileEntity, Boolean) -> Unit,
    onUpdateEducatorTier: (EducatorProfileEntity, String) -> Unit,
    onToggleBackupRoster: (EducatorProfileEntity, Boolean) -> Unit,
    onDeleteEducator: (String, String) -> Unit,
    onUpdateLessonQuality: (LessonEntity, String) -> Unit,
    onDeleteLesson: (LessonEntity) -> Unit,
    onReplyDoubtAsAdmin: (DoubtQueueEntity, String) -> Unit,
    onDeleteDoubt: (DoubtQueueEntity) -> Unit,
    onBroadcastAlert: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AdminTab.METRICS) }
    var editingStudent by remember { mutableStateOf<StudentProfileEntity?>(null) }
    var showCreateStudentDialog by remember { mutableStateOf(false) }
    var showCreateEducatorDialog by remember { mutableStateOf(false) }
    var replyTargetDoubt by remember { mutableStateOf<DoubtQueueEntity?>(null) }
    var adminReplyText by remember { mutableStateOf("") }
    var showBroadcastDialog by remember { mutableStateOf(false) }

    val primaryBandCount = students.count { it.gradeClass in 1..5 }
    val upperBandCount = students.count { it.gradeClass in 6..10 }
    val totalXp = students.sumOf { it.xpPoints }
    val totalModules = students.sumOf { it.modulesCompleted }
    val totalHours = educators.sumOf { it.hoursTaught }
    val backupCount = educators.count { it.isBackupRoster }
    val offlineReadyCount = lessons.count { it.downloaded }
    val pendingDoubtsCount = doubts.count { it.syncStatus == "pending" }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AliveAmbientBackground()

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // Admin Command Header
            item {
                AliveStaggeredEntry(index = 0) {
                    ClayCard(
                        containerColor = TerracottaSoft,
                        cornerRadius = 28.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(TerracottaCoral, RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(R.string.admin_command_center),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = EspressoInk
                                        )
                                        Text(
                                            text = stringResource(R.string.admin_oversight_sub),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CocoaSecondary
                                        )
                                    }
                                }
                                val adminSummarySpeech = stringResource(
                                    R.string.admin_summary_speech,
                                    students.size,
                                    educators.size,
                                    lessons.size,
                                    doubts.size
                                )
                                ListenButton(
                                    isSpeaking = speakingId == "admin_summary",
                                    onClick = {
                                        ttsSpeaker.toggleSpeak("admin_summary", adminSummarySpeech)
                                    }
                                )
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(AdminTab.entries) { tab ->
                                    val selected = activeTab == tab
                                    Surface(
                                        onClick = { activeTab = tab },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selected) EspressoInk else Color.White.copy(alpha = 0.78f),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("admin_tab_${tab.name.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                tab.icon,
                                                contentDescription = null,
                                                tint = if (selected) Color.White else EspressoInk,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = stringResource(tab.labelRes),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (selected) Color.White else EspressoInk,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (activeTab) {
                AdminTab.METRICS -> {
                    item {
                        AliveStaggeredEntry(index = 1) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AdminKpiTile(
                                    title = stringResource(R.string.admin_kpi_reach),
                                    value = stringResource(R.string.admin_kpi_learners, students.size),
                                    sub = stringResource(R.string.admin_kpi_bands, primaryBandCount, upperBandCount),
                                    bg = LeafGreenSoft,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminKpiTile(
                                    title = stringResource(R.string.admin_kpi_outcomes),
                                    value = "$totalXp XP",
                                    sub = stringResource(R.string.admin_kpi_modules_done, totalModules),
                                    bg = SunAmberSoft,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminKpiTile(
                                    title = stringResource(R.string.admin_kpi_offline_equity),
                                    value = "$offlineReadyCount/${lessons.size}",
                                    sub = stringResource(R.string.admin_kpi_doubts_queued, pendingDoubtsCount),
                                    bg = SoftOatSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminKpiTile(
                                    title = stringResource(R.string.admin_tab_educators),
                                    value = stringResource(R.string.admin_kpi_educators_active, educators.size),
                                    sub = stringResource(R.string.admin_kpi_hours_backup, totalHours, backupCount),
                                    bg = TerracottaSoft,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        AliveStaggeredEntry(index = 2) {
                            ClayCard(
                                containerColor = ElevatedClayCard,
                                cornerRadius = 24.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = LeafGreen)
                                        Text(
                                            text = stringResource(R.string.admin_safeguard_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EspressoInk
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.admin_safeguard_body, backupCount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CocoaSecondary
                                    )
                                    ClayButton(
                                        text = stringResource(R.string.admin_broadcast_btn),
                                        onClick = { showBroadcastDialog = true },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Campaign, contentDescription = null, tint = Color.White)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_broadcast_button")
                                    )
                                    val adminCtx = LocalContext.current
                                    val exportChooser = stringResource(R.string.admin_export_chooser)
                                    ClayButton(
                                        text = stringResource(R.string.admin_export_report_btn),
                                        containerColor = LeafGreen,
                                        onClick = {
                                            val report = ReportCardExporter.buildAdminImpactReportText(
                                                students = students,
                                                educators = educators,
                                                lessons = lessons,
                                                doubts = doubts
                                            )
                                            ReportCardExporter.sharePlainText(
                                                context = adminCtx,
                                                subject = "Twogether NGO Impact Report",
                                                body = report,
                                                chooserTitle = exportChooser
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.White)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_export_report_button")
                                    )
                                }
                            }
                        }
                    }
                }

                AdminTab.STUDENTS -> {
                    item {
                        ClayButton(
                            text = stringResource(R.string.admin_add_student_btn),
                            containerColor = LeafGreen,
                            onClick = { showCreateStudentDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_add_student_button")
                        )
                    }
                    items(students, key = { it.id }) { student ->
                        ClayCard(
                            containerColor = ElevatedClayCard,
                            cornerRadius = 24.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    StudentAvatarBadge(avatarId = student.avatarId, size = 48.dp)
                                    val yesNo = stringResource(
                                        if (student.parentalConsentGiven) R.string.admin_yes else R.string.admin_no
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${student.name} • ${stringResource(R.string.common_class_label, student.gradeClass)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EspressoInk
                                        )
                                        Text(
                                            text = "${student.email.ifBlank { stringResource(R.string.admin_pin_profile) }} • ${localizedLessonLanguageName(student.preferredLanguage)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CocoaSecondary
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.admin_student_meta,
                                                student.centerName,
                                                student.xpPoints,
                                                yesNo
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (student.parentalConsentGiven) LeafGreen else ClayError
                                        )
                                    }
                                    IconButton(
                                        onClick = { editingStudent = student },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            Icons.Rounded.Edit,
                                            contentDescription = stringResource(R.string.admin_cd_edit_student),
                                            tint = EspressoInk
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteStudent(student.id, student.name) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = stringResource(R.string.admin_cd_delete_student),
                                            tint = ClayError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.EDUCATORS -> {
                    item {
                        ClayButton(
                            text = stringResource(R.string.admin_add_educator_btn),
                            containerColor = SunAmber,
                            onClick = { showCreateEducatorDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_add_educator_button")
                        )
                    }
                    items(educators, key = { it.id }) { edu ->
                        ClayCard(
                            containerColor = ElevatedClayCard,
                            cornerRadius = 24.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = edu.fullName.ifBlank { stringResource(R.string.admin_unnamed_educator) },
                                                style = MaterialTheme.typography.titleLarge,
                                                color = EspressoInk
                                            )
                                            if (edu.adminApproved) {
                                                Icon(
                                                    Icons.Rounded.CheckCircle,
                                                    contentDescription = stringResource(R.string.admin_cd_approved),
                                                    tint = LeafGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${edu.email} • ${edu.subjectSpecialty}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CocoaSecondary
                                        )
                                        if (edu.referenceContact.isNotBlank()) {
                                            Text(
                                                text = stringResource(R.string.admin_ref_label, edu.referenceContact),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = CocoaSecondary
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onDeleteEducator(edu.id, edu.fullName) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = stringResource(R.string.admin_cd_remove_educator),
                                            tint = ClayError
                                        )
                                    }
                                }

                                // Gemini Document AI Verification Report Card
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = when (edu.docVerificationStatus) {
                                        "ADMIN_APPROVED", "AI_VERIFIED" -> LeafGreenSoft
                                        "REJECTED" -> ClayErrorContainer
                                        else -> SunAmberSoft
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = EspressoInk, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = stringResource(R.string.admin_doc_status, edu.docVerificationStatus, edu.docConfidence),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = EspressoInk,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (edu.docAiSummary.isNotBlank()) {
                                            Text(edu.docAiSummary, style = MaterialTheme.typography.bodyMedium, color = EspressoInk)
                                        } else {
                                            Text(
                                                text = stringResource(
                                                    R.string.admin_doc_board,
                                                    edu.board12th,
                                                    edu.rollYear,
                                                    edu.documentAttached
                                                ),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = CocoaSecondary
                                            )
                                        }
                                    }
                                }

                                // Tier Switcher + Backup Roster
                                Text(
                                    text = stringResource(R.string.admin_tier_assignment),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CocoaSecondary
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        "TIER_1_PAID" to stringResource(R.string.auth_tier_1_paid),
                                        "TIER_2_UNPAID" to stringResource(R.string.admin_tier_2_vol),
                                        "TIER_3_RETIRED" to stringResource(R.string.auth_tier_3_retired)
                                    ).forEach { (key, lbl) ->
                                        FilterChip(
                                            selected = edu.tier == key,
                                            onClick = { onUpdateEducatorTier(edu, key) },
                                            label = { Text(lbl) }
                                        )
                                    }
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = edu.isBackupRoster,
                                            onCheckedChange = { onToggleBackupRoster(edu, it) }
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.admin_backup_roster),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { onApproveEducator(edu, false) }) {
                                            Text(stringResource(R.string.admin_reject))
                                        }
                                        ClayButton(
                                            text = stringResource(
                                                if (edu.adminApproved) R.string.admin_approved_check else R.string.admin_approve
                                            ),
                                            containerColor = LeafGreen,
                                            onClick = { onApproveEducator(edu, true) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.CONTENT -> {
                    item {
                        Text(
                            text = stringResource(R.string.admin_lessons_title, lessons.size),
                            style = MaterialTheme.typography.titleLarge,
                            color = EspressoInk
                        )
                    }
                    items(lessons, key = { it.id }) { lesson ->
                        ClayCard(
                            containerColor = ElevatedClayCard,
                            cornerRadius = 22.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.admin_lesson_item_title, lesson.title, lesson.gradeClass),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.admin_lesson_item_sub,
                                            lesson.uploadedByEducatorName,
                                            localizedLessonLanguageName(lesson.language),
                                            lesson.feedbackRating,
                                            lesson.feedbackCount
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CocoaSecondary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilterChip(
                                        selected = lesson.qualityStatus == "APPROVED",
                                        onClick = {
                                            val next = if (lesson.qualityStatus == "APPROVED") "PENDING_REVIEW" else "APPROVED"
                                            onUpdateLessonQuality(lesson, next)
                                        },
                                        label = {
                                            Text(
                                                stringResource(
                                                    if (lesson.qualityStatus == "APPROVED") R.string.admin_cd_approved else R.string.admin_review
                                                )
                                            )
                                        }
                                    )
                                    IconButton(onClick = { onDeleteLesson(lesson) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = stringResource(R.string.admin_cd_delete_lesson),
                                            tint = ClayError
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.admin_doubts_mod_title, doubts.size),
                            style = MaterialTheme.typography.titleLarge,
                            color = EspressoInk
                        )
                    }
                    items(doubts, key = { it.id }) { doubt ->
                        ClayCard(
                            containerColor = ElevatedClayCard,
                            cornerRadius = 22.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val syncLabel = when (doubt.syncStatus) {
                                        "answered" -> stringResource(R.string.doubt_badge_answered)
                                        "synced" -> stringResource(R.string.doubt_badge_synced)
                                        else -> stringResource(R.string.doubt_badge_pending)
                                    }
                                    Text(
                                        text = "${doubt.studentName} (${stringResource(R.string.common_class_label, doubt.gradeClass)} • ${localizedSubjectName(doubt.subject)}) • $syncLabel",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { onDeleteDoubt(doubt) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = stringResource(R.string.admin_cd_delete_doubt),
                                            tint = ClayError
                                        )
                                    }
                                }
                                Text(doubt.questionText, style = MaterialTheme.typography.bodyLarge, color = EspressoInk)
                                if (doubt.educatorReply.isNullOrBlank()) {
                                    ClayButton(
                                        text = stringResource(R.string.admin_answer_doubt),
                                        onClick = {
                                            replyTargetDoubt = doubt
                                            adminReplyText = ""
                                        }
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.admin_reply_prefix, doubt.educatorReply),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LeafGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingStudent?.let { student ->
        AdminEditStudentDialog(
            student = student,
            onDismiss = { editingStudent = null },
            onSave = { updated ->
                editingStudent = null
                onUpdateStudent(updated)
            }
        )
    }

    replyTargetDoubt?.let { doubt ->
        AlertDialog(
            onDismissRequest = { replyTargetDoubt = null },
            containerColor = WarmCreamBg,
            title = { Text(stringResource(R.string.edu_reply_to, doubt.studentName)) },
            text = {
                OutlinedTextField(
                    value = adminReplyText,
                    onValueChange = { adminReplyText = it },
                    label = { Text(stringResource(R.string.admin_coordinator_answer)) },
                    minLines = 3
                )
            },
            confirmButton = {
                ClayButton(
                    text = stringResource(R.string.edu_send_reply),
                    enabled = adminReplyText.isNotBlank(),
                    onClick = {
                        onReplyDoubtAsAdmin(doubt, adminReplyText)
                        replyTargetDoubt = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { replyTargetDoubt = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showBroadcastDialog) {
        AdminBroadcastDialog(
            onDismiss = { showBroadcastDialog = false },
            onSend = { title, msg, cat ->
                showBroadcastDialog = false
                onBroadcastAlert(title, msg, cat, "backpack")
            }
        )
    }

    if (showCreateStudentDialog) {
        AdminCreateStudentDialog(
            onDismiss = { showCreateStudentDialog = false },
            onCreate = { n, e, p, g, l, c ->
                showCreateStudentDialog = false
                onCreateStudent(n, e, p, g, l, c)
            }
        )
    }

    if (showCreateEducatorDialog) {
        AdminCreateEducatorDialog(
            onDismiss = { showCreateEducatorDialog = false },
            onCreate = { n, e, p, t, s ->
                showCreateEducatorDialog = false
                onCreateEducator(n, e, p, t, s)
            }
        )
    }
}

@Composable
private fun AdminKpiTile(
    title: String,
    value: String,
    sub: String,
    bg: Color,
    modifier: Modifier = Modifier
) {
    ClayCard(
        containerColor = bg,
        cornerRadius = 22.dp,
        modifier = modifier.widthIn(min = 148.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AlivePulseDot(color = EspressoInk.copy(alpha = 0.7f), sizeDp = 6.dp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = CocoaSecondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = EspressoInk,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelMedium,
                color = CocoaSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminEditStudentDialog(
    student: StudentProfileEntity,
    onDismiss: () -> Unit,
    onSave: (StudentProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }
    var gradeClass by remember { mutableIntStateOf(student.gradeClass) }
    var language by remember { mutableStateOf(student.preferredLanguage) }
    var center by remember { mutableStateOf(student.centerName) }
    var bonusXp by remember { mutableIntStateOf(student.xpPoints) }
    var consent by remember { mutableStateOf(student.parentalConsentGiven) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        title = { Text(stringResource(R.string.admin_manage_student, student.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.admin_name_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = center,
                    onValueChange = { center = it },
                    label = { Text(stringResource(R.string.admin_ngo_center)) },
                    singleLine = true
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Bilingual", "English", "Hindi").forEach { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = { language = lang },
                            label = { Text(localizedLessonLanguageName(lang)) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.common_class_label, gradeClass))
                    OutlinedButton(onClick = { if (gradeClass > 1) gradeClass-- }) { Text("-") }
                    OutlinedButton(onClick = { if (gradeClass < 10) gradeClass++ }) { Text("+") }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.admin_xp_label, bonusXp))
                    OutlinedButton(onClick = { bonusXp += 50 }) { Text(stringResource(R.string.admin_plus_50_xp)) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = consent, onCheckedChange = { consent = it })
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.admin_guardian_consent))
                }
            }
        },
        confirmButton = {
            ClayButton(
                text = stringResource(R.string.common_save),
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        student.copy(
                            name = name.trim(),
                            gradeClass = gradeClass,
                            preferredLanguage = language,
                            centerName = center.trim(),
                            xpPoints = bonusXp,
                            parentalConsentGiven = consent
                        )
                    )
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminBroadcastDialog(
    onDismiss: () -> Unit,
    onSend: (String, String, String) -> Unit
) {
    val defaultTitle = stringResource(R.string.admin_default_alert_title)
    val defaultMsg = stringResource(R.string.admin_default_alert_msg)
    var title by remember { mutableStateOf(defaultTitle) }
    var message by remember { mutableStateOf(defaultMsg) }
    var category by remember { mutableStateOf("homework") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        title = { Text(stringResource(R.string.admin_send_alert_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.admin_alert_title_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(stringResource(R.string.admin_message_label)) },
                    minLines = 2
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "homework" to stringResource(R.string.admin_cat_homework),
                        "live" to stringResource(R.string.admin_cat_live),
                        "doubt" to stringResource(R.string.admin_cat_doubt)
                    ).forEach { (k, l) ->
                        FilterChip(selected = category == k, onClick = { category = k }, label = { Text(l) })
                    }
                }
            }
        },
        confirmButton = {
            ClayButton(
                text = stringResource(R.string.admin_send_alert_btn),
                enabled = title.isNotBlank() && message.isNotBlank(),
                onClick = { onSend(title.trim(), message.trim(), category) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminCreateStudentDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var center by remember { mutableStateOf("Asha Kiran NGO Center") }
    var grade by remember { mutableIntStateOf(5) }
    var language by remember { mutableStateOf("Bilingual") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        title = { Text(stringResource(R.string.admin_add_student_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.admin_student_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.admin_student_email)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.auth_password_min_6)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = center,
                    onValueChange = { center = it },
                    label = { Text(stringResource(R.string.admin_ngo_center)) },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.common_class_label, grade))
                    OutlinedButton(onClick = { if (grade > 1) grade-- }) { Text("-") }
                    OutlinedButton(onClick = { if (grade < 10) grade++ }) { Text("+") }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Bilingual", "English", "Hindi").forEach { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = { language = lang },
                            label = { Text(localizedLessonLanguageName(lang)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            ClayButton(
                text = stringResource(R.string.admin_create_student),
                enabled = name.isNotBlank() && email.contains("@") && password.length >= 6,
                onClick = { onCreate(name, email, password, grade, language, center) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminCreateEducatorDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit
) {
    val defaultSubject = LocalContext.current.localizedForCurrentLocale().getString(R.string.subject_math)
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(defaultSubject) }
    var tier by remember { mutableStateOf("TIER_1_PAID") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        title = { Text(stringResource(R.string.admin_add_educator_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(stringResource(R.string.admin_educator_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.admin_educator_email)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.auth_password_min_6)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(stringResource(R.string.admin_primary_subject)) },
                    singleLine = true
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "TIER_1_PAID" to stringResource(R.string.admin_tier_1),
                        "TIER_2_UNPAID" to stringResource(R.string.admin_tier_2),
                        "TIER_3_RETIRED" to stringResource(R.string.admin_tier_3)
                    ).forEach { (k, l) ->
                        FilterChip(selected = tier == k, onClick = { tier = k }, label = { Text(l) })
                    }
                }
            }
        },
        confirmButton = {
            ClayButton(
                text = stringResource(R.string.admin_create_educator),
                enabled = fullName.isNotBlank() && email.contains("@") && password.length >= 6,
                onClick = { onCreate(fullName, email, password, tier, subject) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
