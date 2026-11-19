package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.local.LessonEntity
import com.example.data.local.SeedData
import com.example.data.repository.GeminiOnlineMode
import com.example.data.repository.OfflineFaqCatalog
import com.example.data.repository.OfflineKnowledgeSnapshot
import com.example.data.repository.OfflineSyllabusEngine
import com.example.ui.components.parseInlineMarkdown
import com.example.ui.viewmodel.LessonProgressionTracker
import com.example.util.OfflineContentTranslator
import com.example.util.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TwogetherCurriculumUnitTest {
    @Test
    fun offlineSyllabusEngineSolvesFractionsAndConcepts() {
        val frac = OfflineSyllabusEngine.answer("Solve 3/4 + 1/2", gradeClass = 5)
        assertTrue("Expected simplified fraction 5/4 in: $frac", frac.contains("5/4"))

        val photo = OfflineSyllabusEngine.answer("Explain photosynthesis", gradeClass = 6)
        assertTrue("Expected chlorophyll in: $photo", photo.lowercase().contains("chlorophyll"))

        val math = OfflineSyllabusEngine.answer("24 * 5", gradeClass = 4)
        assertTrue("Expected 120 in: $math", math.contains("120"))
    }

    @Test
    fun offlineSyllabusEngineFindsNumeratorAndRoomDbLessons() {
        val snapshot = OfflineKnowledgeSnapshot(
            lessons = SeedData.initialLessons,
            questions = SeedData.initialQuizzes,
            homeworks = SeedData.initialHomeworks
        )
        val answer = OfflineSyllabusEngine.answer("What is a numerator?", gradeClass = 5, knowledge = snapshot).markdown
        assertTrue("Expected numerator explanation in: $answer", answer.lowercase().contains("numerator"))
        assertTrue("Expected top number or fraction explanation in: $answer", answer.lowercase().contains("top"))

        val customLesson = SeedData.initialLessons.first().copy(
            id = "lesson_custom_mitochondria",
            title = "Cell Powerhouse: Mitochondria",
            studyNotes = "Mitochondria converts glucose into ATP energy inside eukaryotic cells."
        )
        val customSnapshot = snapshot.copy(lessons = snapshot.lessons + customLesson)
        val customAnswer = OfflineSyllabusEngine.answer("Explain mitochondria", gradeClass = 8, knowledge = customSnapshot).markdown
        assertTrue("Expected Room DB lesson match in: $customAnswer", customAnswer.contains("Mitochondria"))
        assertTrue("Expected ATP energy from study notes in: $customAnswer", customAnswer.contains("ATP"))
    }

    @Test
    fun lessonProgressionTrackerEnforcesSequentialSubjectUnlock() {
        val math1 = LessonEntity(
            id = "m1", title = "Math 1", subject = "Math", gradeClass = 1,
            summary = "", durationLabel = "3m", rawResName = "lesson_fractions",
            watchedByProfileIds = "", completedByProfileIds = ""
        )
        val math2 = LessonEntity(
            id = "m2", title = "Math 2", subject = "Math", gradeClass = 5,
            summary = "", durationLabel = "4m", rawResName = "lesson_fractions",
            watchedByProfileIds = "", completedByProfileIds = ""
        )
        val sci1 = LessonEntity(
            id = "s1", title = "Sci 1", subject = "Science", gradeClass = 4,
            summary = "", durationLabel = "3m", rawResName = "lesson_photosynthesis",
            watchedByProfileIds = "", completedByProfileIds = ""
        )

        // Initially: Lesson 1 per subject unlocked; Lesson 2 locked
        val initialMap = LessonProgressionTracker.computeUnlockMap(listOf(math1, math2, sci1), "stu_1")
        assertEquals(true, initialMap["m1"])
        assertEquals(false, initialMap["m2"])
        assertEquals(true, initialMap["s1"])

        // Only video watched (no quiz yet) -> Lesson 2 still locked
        val watchedOnly = math1.copy(watchedByProfileIds = "stu_1")
        val afterWatchMap = LessonProgressionTracker.computeUnlockMap(listOf(watchedOnly, math2, sci1), "stu_1")
        assertEquals(false, afterWatchMap["m2"])

        // Both video watched AND quiz passed -> Lesson 2 unlocks!
        val fullyDone = watchedOnly.copy(completedByProfileIds = "stu_1")
        val unlockedMap = LessonProgressionTracker.computeUnlockMap(listOf(fullyDone, math2, sci1), "stu_1")
        assertEquals(true, unlockedMap["m2"])

        // Staff bypass unlocks everything
        val staffMap = LessonProgressionTracker.computeUnlockMap(
            listOf(math1, math2, sci1),
            studentProfileId = null,
            bypassLockForStaff = true
        )
        assertEquals(true, staffMap["m2"])
    }

    @Test
    fun offlineContentTranslatorSupportsIndianScheduledLanguages() {
        val hiTitle = OfflineContentTranslator.translateLessonTitle("lesson_c5_fractions", "Fractions with Roti Slices", "hi")
        val mrTitle = OfflineContentTranslator.translateLessonTitle("lesson_c5_fractions", "Fractions with Roti Slices", "mr")
        val taTitle = OfflineContentTranslator.translateLessonTitle("lesson_c5_fractions", "Fractions with Roti Slices", "ta")
        val bnTitle = OfflineContentTranslator.translateLessonTitle("lesson_c5_fractions", "Fractions with Roti Slices", "bn")

        assertTrue(hiTitle.contains("भिन्न"))
        assertTrue(mrTitle.contains("अपूर्णांक"))
        assertTrue(taTitle.contains("பின்னங்கள்"))
        assertTrue(bnTitle.contains("ভগ্নাংশ"))
    }

    @Test
    fun markdownParserStripsRawAsterisksCleanly() {
        val parsed = parseInlineMarkdown("**Numerator** is the *top* number in `3/4`.", Color.Black)
        assertEquals("Numerator is the top number in  3/4 .", parsed.text)
        assertFalse(parsed.text.contains("**"))
    }

    @Test
    fun seedDataHasLessonsQuizzesAndHomeworks() {
        assertEquals(12, SeedData.initialLessons.size)
        assertTrue(SeedData.initialLessons.any { it.gradeClass == 1 })
        assertTrue(SeedData.initialLessons.any { it.gradeClass == 10 })
        assertTrue(SeedData.initialLessons.all { it.downloaded })
        assertEquals(12, SeedData.initialLessons.map { it.rawResName }.distinct().size)
        assertTrue(SeedData.initialHomeworks.size >= 4)
        SeedData.initialLessons.forEach { lesson ->
            val quizzes = SeedData.initialQuizzes.filter { it.lessonId == lesson.id }
            assertTrue("Lesson ${lesson.id} should have at least 3 NCERT quiz questions", quizzes.size >= 3)
        }
    }

    @Test
    fun seedNotificationsIncludePendingHomeworkReminder() {
        assertTrue(
            SeedData.initialNotifications.any {
                it.title == "Pending Homework Reminder" &&
                    it.message.contains("Maths homework is still incomplete")
            }
        )
    }

    @Test
    fun offlineFaqCatalogAndDefaultOnlineGeminiModelConfigured() {
        assertTrue(OfflineFaqCatalog.faqs.size >= 8)
        assertTrue(OfflineFaqCatalog.faqs.any { it.category == "App Help" })
        assertTrue(OfflineFaqCatalog.faqs.any { it.category == "Maths" })
        assertEquals("gemini-3.8-flash", GeminiOnlineMode.BALANCED.modelId)
        assertEquals("gemini-3.5-flash-lite", GeminiOnlineMode.FAST.modelId)
    }

    @Test
    fun passwordHasherVerifiesValidAndRejectsInvalidPasswords() {
        val hash = PasswordHasher.sha256("Asha@1234")
        assertTrue(PasswordHasher.verify("Asha@1234", hash))
        assertTrue(PasswordHasher.verify("  Asha@1234  ", hash))
        assertFalse(PasswordHasher.verify("WrongPass", hash))
        assertFalse(PasswordHasher.verify("", hash))
    }

    @Test
    fun demoPortalAccountsAndPreFilledEducatorAreConsistent() {
        val accounts = SeedData.defaultPortalAccounts("uid_demo")
        val admin = accounts.find { it.email == "admin@twogether.org" }
        val student = accounts.find { it.email == "asha.student@twogether.org" }
        val educator = accounts.find { it.email == "priya.teacher@twogether.org" }

        assertNotNull(admin)
        assertNotNull(student)
        assertNotNull(educator)

        assertEquals("ADMIN", admin?.role)
        assertEquals("STUDENT", student?.role)
        assertEquals("EDUCATOR", educator?.role)

        assertTrue(PasswordHasher.verify("Admin@1234", admin!!.passwordHash))
        assertTrue(PasswordHasher.verify("Asha@1234", student!!.passwordHash))
        assertTrue(PasswordHasher.verify("Teach@1234", educator!!.passwordHash))

        val educators = SeedData.defaultEducatorsForUser("uid_demo")
        val priya = educators.find { it.email == "priya.teacher@twogether.org" }
        assertNotNull(priya)
        assertEquals("AI_VERIFIED", priya?.docVerificationStatus)
        assertTrue(priya!!.adminApproved)
        assertTrue(priya.isBackupRoster)
        assertTrue(priya.lessonsUploadedCount >= 2)
    }
}
