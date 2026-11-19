package com.example

import com.example.data.local.DoubtQueueEntity
import com.example.data.local.EducatorProfileEntity
import com.example.data.local.LessonEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudentProfileEntity
import com.example.ui.components.OfflineFlashcardDeck
import com.example.util.ReportCardExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineStudyToolsUnitTest {

    @Test
    fun flashcardDeckCoversAllCoreSubjectsAndGrades() {
        val cards = OfflineFlashcardDeck.cards
        assertTrue("Expected at least 15 NCERT flashcards", cards.size >= 15)
        assertEquals("Flashcard IDs must be unique", cards.size, cards.map { it.id }.toSet().size)

        val subjects = cards.map { it.subject }.toSet()
        assertTrue(subjects.contains("Math"))
        assertTrue(subjects.contains("Science"))
        assertTrue(subjects.contains("English"))
        assertTrue(subjects.contains("Social Studies"))

        cards.forEach { card ->
            assertTrue("Question blank for ${card.id}", card.frontQuestion.isNotBlank())
            assertTrue("Answer blank for ${card.id}", card.backAnswer.isNotBlank())
            assertTrue("Tip blank for ${card.id}", card.memoryTip.isNotBlank())
            assertTrue("Grade out of range for ${card.id}", card.gradeClass in 1..10)
        }
    }

    @Test
    fun subjectMasteryAndStudentReportFormatAccurately() {
        val student = StudentProfileEntity(
            id = "stu_asha",
            ownerUid = "local_offline_workspace",
            name = "Asha",
            email = "asha.student@twogether.org",
            gradeClass = 5,
            avatarId = "owl",
            themeColorKey = "terracotta",
            pinSequence = "0,1,2,3",
            xpPoints = 220,
            modulesCompleted = 3
        )
        val lessons = listOf(
            LessonEntity(
                id = "l1",
                title = "Fractions",
                subject = "Math",
                gradeClass = 5,
                durationLabel = "5 min",
                summary = "Fractions",
                rawResName = "lesson_fractions",
                downloaded = true,
                watchedByProfileIds = "stu_asha",
                completedByProfileIds = "stu_asha"
            ),
            LessonEntity(
                id = "l2",
                title = "Linear Equations",
                subject = "Math",
                gradeClass = 8,
                durationLabel = "6 min",
                summary = "Algebra",
                rawResName = "lesson_c8_linear_equations",
                downloaded = true
            ),
            LessonEntity(
                id = "l3",
                title = "Photosynthesis",
                subject = "Science",
                gradeClass = 6,
                durationLabel = "6 min",
                summary = "Plants",
                rawResName = "lesson_photosynthesis",
                downloaded = true
            )
        )
        val attempts = listOf(
            QuizAttemptEntity(
                id = "qa1",
                studentProfileId = "stu_asha",
                lessonId = "l1",
                lessonTitle = "Fractions",
                subject = "Math",
                gradeClass = 5,
                score = 3,
                total = 3,
                passed = true
            )
        )

        val stats = ReportCardExporter.computeSubjectMastery(student.id, lessons, attempts)
        val mathStat = stats.first { it.subject == "Math" }
        assertEquals(2, mathStat.totalLessons)
        assertEquals(1, mathStat.completedLessons)
        assertEquals(100, mathStat.avgQuizPercent)

        val report = ReportCardExporter.buildStudentReportText(student, "Young Tree", lessons, attempts)
        assertTrue(report.contains("TWOGETHER - STUDENT PROGRESS REPORT"))
        assertTrue(report.contains("Asha"))
        assertTrue(report.contains("220 XP"))
        assertTrue(report.contains("Math: 1/2 lessons done | Quiz Avg: 100%"))
    }

    @Test
    fun educatorCertificateAndAdminImpactReportsContainKeyMetrics() {
        val educator = EducatorProfileEntity(
            id = "edu_priya",
            ownerUid = "local_offline_workspace",
            fullName = "Priya Sharma",
            email = "priya.teacher@twogether.org",
            tier = "TIER_1_PAID",
            subjectSpecialty = "Mathematics & Science",
            docVerificationStatus = "ADMIN_APPROVED",
            adminApproved = true,
            lessonsUploadedCount = 4,
            hoursTaught = 28.5,
            doubtsResolved = 12,
            studentsMentored = 19,
            isBackupRoster = true
        )
        val cert = ReportCardExporter.buildEducatorCertificateText(educator)
        assertTrue(cert.contains("TWOGETHER - VERIFIED IMPACT CERTIFICATE"))
        assertTrue(cert.contains("Priya Sharma"))
        assertTrue(cert.contains("Silver Impact Mentor"))
        assertTrue(cert.contains("28.5 hrs"))

        val doubts = listOf(
            DoubtQueueEntity(
                id = "d1",
                ownerUid = "local_offline_workspace",
                studentProfileId = "stu_asha",
                studentName = "Asha",
                gradeClass = 5,
                subject = "Math",
                questionText = "How to add fractions?",
                syncStatus = "answered",
                educatorReply = "Make denominators equal first."
            )
        )
        val adminReport = ReportCardExporter.buildAdminImpactReportText(
            students = emptyList(),
            educators = listOf(educator),
            lessons = emptyList(),
            doubts = doubts
        )
        assertTrue(adminReport.contains("TWOGETHER - NGO IMPACT SUMMARY REPORT"))
        assertTrue(adminReport.contains("Verified: 1"))
        assertTrue(adminReport.contains("Student Doubts Answered: 1 / 1"))
    }
}
