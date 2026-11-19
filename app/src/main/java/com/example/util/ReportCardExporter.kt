package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.local.DoubtQueueEntity
import com.example.data.local.EducatorProfileEntity
import com.example.data.local.LessonEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudentProfileEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SubjectMasteryStat(
    val subject: String,
    val totalLessons: Int,
    val completedLessons: Int,
    val avgQuizPercent: Int
)

/**
 * 100% offline-first report & certificate formatter that shares cleanly via standard
 * Android ACTION_SEND intent (zero extra permissions; works with WhatsApp, SMS, Notes, Email).
 */
object ReportCardExporter {

    fun computeSubjectMastery(
        studentId: String,
        lessons: List<LessonEntity>,
        quizAttempts: List<QuizAttemptEntity>
    ): List<SubjectMasteryStat> {
        val coreSubjects = listOf("Math", "Science", "English", "Social Studies")
        val allSubjects = (coreSubjects + lessons.map { it.subject }).distinct()
        return allSubjects.mapNotNull { subject ->
            val subjectLessons = lessons.filter { it.subject.equals(subject, ignoreCase = true) }
            if (subjectLessons.isEmpty()) return@mapNotNull null
            val completedCount = subjectLessons.count { lesson ->
                val watched = lesson.watchedByProfileIds.split(",").any { it.trim() == studentId }
                val passed = lesson.completedByProfileIds.split(",").any { it.trim() == studentId }
                watched && passed
            }
            val attempts = quizAttempts.filter { it.subject.equals(subject, ignoreCase = true) }
            val avgPct = if (attempts.isEmpty()) {
                if (completedCount > 0) 100 else 0
            } else {
                attempts.map { (it.score * 100) / it.total.coerceAtLeast(1) }.average().toInt()
            }
            SubjectMasteryStat(
                subject = subject,
                totalLessons = subjectLessons.size,
                completedLessons = completedCount,
                avgQuizPercent = avgPct.coerceIn(0, 100)
            )
        }
    }

    fun buildStudentReportText(
        student: StudentProfileEntity,
        stageTitle: String,
        lessons: List<LessonEntity>,
        quizAttempts: List<QuizAttemptEntity>
    ): String {
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        val mastery = computeSubjectMastery(student.id, lessons, quizAttempts)
        val totalAttempts = quizAttempts.size
        val passedAttempts = quizAttempts.count { it.passed }

        return buildString {
            appendLine("======================================")
            appendLine("TWOGETHER - STUDENT PROGRESS REPORT")
            appendLine("Learn Together, Earn Together, Rise Together")
            appendLine("======================================")
            appendLine("Date: $dateStr")
            appendLine("Student: ${student.name}")
            appendLine("Class: ${student.gradeClass} | Center: ${student.centerName}")
            appendLine("Knowledge Tree Stage: $stageTitle")
            appendLine("Total XP: ${student.xpPoints} XP | Modules Completed: ${student.modulesCompleted}")
            appendLine("Offline Quizzes Passed: $passedAttempts / $totalAttempts")
            appendLine()
            appendLine("SUBJECT MASTERY BREAKDOWN:")
            mastery.forEach { stat ->
                appendLine(
                    "- ${stat.subject}: ${stat.completedLessons}/${stat.totalLessons} lessons done | Quiz Avg: ${stat.avgQuizPercent}%"
                )
            }
            appendLine()
            appendLine("Generated offline via Twogether Education App.")
        }
    }

    fun buildEducatorCertificateText(educator: EducatorProfileEntity): String {
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        val badgeTier = when {
            educator.hoursTaught >= 50.0 -> "Gold Impact Mentor (50+ hrs)"
            educator.hoursTaught >= 25.0 -> "Silver Impact Mentor (25+ hrs)"
            educator.hoursTaught >= 10.0 -> "Bronze Impact Mentor (10+ hrs)"
            else -> "Verified Community Educator"
        }
        val tierName = when (educator.tier) {
            "TIER_1_PAID" -> "Tier 1 Paid Rural Educator"
            "TIER_3_RETIRED" -> "Tier 3 Retired Senior Mentor"
            else -> "Tier 2 Youth Volunteer Educator"
        }
        return buildString {
            appendLine("======================================")
            appendLine("TWOGETHER - VERIFIED IMPACT CERTIFICATE")
            appendLine("Learn Together, Earn Together, Rise Together")
            appendLine("======================================")
            appendLine("Awarded To: ${educator.fullName.ifBlank { "Twogether Educator" }}")
            appendLine("Role: $tierName")
            appendLine("Specialty: ${educator.subjectSpecialty}")
            appendLine("Verification Status: ${educator.docVerificationStatus}")
            appendLine("Recognition Medal: $badgeTier")
            appendLine()
            appendLine("COMMUNITY IMPACT RECORD:")
            appendLine("- Total Teaching Hours: ${String.format(Locale.US, "%.1f", educator.hoursTaught)} hrs")
            appendLine("- Students Mentored: ${educator.studentsMentored}")
            appendLine("- Doubts Resolved: ${educator.doubtsResolved}")
            appendLine("- Offline Lessons Published: ${educator.lessonsUploadedCount}")
            appendLine()
            appendLine("Issued Date: $dateStr")
            appendLine("Verified via Twogether Education Platform.")
        }
    }

    fun buildAdminImpactReportText(
        students: List<StudentProfileEntity>,
        educators: List<EducatorProfileEntity>,
        lessons: List<LessonEntity>,
        doubts: List<DoubtQueueEntity>
    ): String {
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        val primaryBand = students.count { it.gradeClass in 1..5 }
        val upperBand = students.count { it.gradeClass in 6..10 }
        val totalXp = students.sumOf { it.xpPoints }
        val totalModules = students.sumOf { it.modulesCompleted }
        val totalHours = educators.sumOf { it.hoursTaught }
        val verifiedEducators = educators.count { it.adminApproved || it.docVerificationStatus == "AI_VERIFIED" }
        val backupEducators = educators.count { it.isBackupRoster }
        val offlineLessons = lessons.count { it.downloaded }
        val answeredDoubts = doubts.count { it.syncStatus == "answered" }
        val pendingDoubts = doubts.count { it.syncStatus == "pending" }

        return buildString {
            appendLine("======================================")
            appendLine("TWOGETHER - NGO IMPACT SUMMARY REPORT")
            appendLine("Learn Together, Earn Together, Rise Together")
            appendLine("======================================")
            appendLine("Generated: $dateStr")
            appendLine()
            appendLine("1. STUDENT REACH & OUTCOMES")
            appendLine("- Active Learners: ${students.size} (Class 1-5: $primaryBand, Class 6-10: $upperBand)")
            appendLine("- Cumulative Student XP: $totalXp XP")
            appendLine("- Total Syllabus Modules Completed: $totalModules")
            appendLine()
            appendLine("2. EDUCATOR FORCE & VERIFICATION")
            appendLine("- Registered Educators: ${educators.size} (Verified: $verifiedEducators)")
            appendLine("- Total Verified Teaching Hours: ${String.format(Locale.US, "%.1f", totalHours)} hrs")
            appendLine("- Backup Continuity Roster: $backupEducators active")
            appendLine()
            appendLine("3. OFFLINE EQUITY & DOUBT RESOLUTION")
            appendLine("- Offline-Ready Lessons: $offlineLessons / ${lessons.size}")
            appendLine("- Student Doubts Answered: $answeredDoubts / ${doubts.size} (Offline Queue: $pendingDoubts)")
            appendLine()
            appendLine("Exported directly from Twogether Admin Command Center.")
        }
    }

    fun sharePlainText(context: Context, subject: String, body: String, chooserTitle: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }
}
