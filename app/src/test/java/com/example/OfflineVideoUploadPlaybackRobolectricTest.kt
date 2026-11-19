package com.example

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.TextureView
import androidx.media3.ui.PlayerView
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.data.repository.TwogetherRepository
import com.example.ui.viewmodel.LessonProgressionTracker
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineVideoUploadPlaybackRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        try {
            WorkManager.initialize(context, Configuration.Builder().build())
        } catch (_: IllegalStateException) {
            // Already initialized in this test JVM
        }
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:testtwogether")
                    .setApiKey("test-api-key")
                    .setProjectId("twogether-offline-test")
                    .build()
            )
        }
    }

    @Test
    fun educatorUploadsGalleryVideo_offlineAirplaneSwitchToStudent_videoPersistsUnlockedAndUsesTextureView() = runBlocking {
        val repository = TwogetherRepository(context)

        // 1. Log in as Educator (priya.teacher@twogether.org / Teach@1234)
        val educatorLogin = repository.authenticateOnLaunch(
            expectedRole = "EDUCATOR",
            rawEmail = "priya.teacher@twogether.org",
            rawPassword = "Teach@1234"
        )
        assertTrue("Educator login should succeed", educatorLogin.isSuccess)
        val educatorAccount = educatorLogin.getOrThrow()
        val educatorProfile = repository.getEducatorById(educatorAccount.linkedEntityId)
        assertNotNull("Educator profile should exist in Room", educatorProfile)

        // 2. Simulate picking a 5-second MP4 from the device gallery via PickVisualMedia
        val sourceMp4Bytes = context.resources.openRawResource(R.raw.lesson_fractions).use { it.readBytes() }
        assertTrue("Source MP4 fixture must be non-empty", sourceMp4Bytes.isNotEmpty())
        val galleryUri = Uri.parse("content://media/picker/0/video/judge_demo_5sec.mp4")
        Shadows.shadowOf(context.contentResolver).registerInputStream(
            galleryUri,
            ByteArrayInputStream(sourceMp4Bytes)
        )

        // 3. Publish the lesson with the picked gallery video
        val publishResult = repository.publishEducatorLesson(
            uid = TwogetherRepository.LOCAL_WORKSPACE_UID,
            educator = educatorProfile!!,
            title = "Judge Demo 5s Fractions Clip",
            subject = "Math",
            gradeClass = 5,
            language = "Bilingual",
            durationLabel = "1 min",
            summary = "Short 5-second offline gallery video test",
            studyNotes = "Watch offline in Airplane Mode.",
            quizQuestion = "What is the top number of a fraction?",
            options = listOf("Numerator", "Denominator", "Factor", "Quotient"),
            correctIndex = 0,
            explanation = "The top number is the numerator.",
            customVideoUri = galleryUri.toString()
        )
        assertTrue("Lesson publish should succeed offline without FirestoreError", publishResult.isSuccess)
        val uploadedLesson = publishResult.getOrThrow()

        // 4. Verify the video was copied to persistent internal storage (offline_videos/)
        val savedPath = uploadedLesson.localFilePath
        assertNotNull("Uploaded lesson must have a persistent localFilePath", savedPath)
        assertFalse(
            "Must store internal file path, NOT temporary content:// URI",
            savedPath!!.startsWith("content://")
        )
        assertTrue(
            "Video must be copied into filesDir/offline_videos/",
            savedPath.contains("offline_videos") && savedPath.endsWith(".mp4")
        )
        val savedFile = File(savedPath)
        assertTrue("Copied video file must exist on disk", savedFile.exists())
        assertArrayEquals(
            "Copied offline MP4 bytes must match original gallery video byte-for-byte",
            sourceMp4Bytes,
            savedFile.readBytes()
        )

        // 5. Simulate Airplane Mode + temporary gallery URI becoming inaccessible
        Shadows.shadowOf(context.contentResolver).registerInputStream(
            galleryUri,
            ByteArrayInputStream(ByteArray(0))
        )

        // 6. Switch to Student (asha.student@twogether.org / Asha@1234)
        val studentLogin = repository.authenticateOnLaunch(
            expectedRole = "STUDENT",
            rawEmail = "asha.student@twogether.org",
            rawPassword = "Asha@1234"
        )
        assertTrue("Student login should succeed offline", studentLogin.isSuccess)
        val studentAccount = studentLogin.getOrThrow()
        val studentProfile = repository.getStudentProfileById(studentAccount.linkedEntityId)
        assertNotNull("Student profile Asha should exist in Room", studentProfile)

        // 7. Verify uploaded video is visible FIRST in Backpack, marked downloaded, unlocked, and file still intact
        val lessonsForStudent = repository.observeLessons().first()
        assertEquals(
            "Educator's newly uploaded video should appear first in Student Backpack",
            uploadedLesson.id,
            lessonsForStudent.first().id
        )
        assertTrue("Uploaded lesson must be offline-ready (downloaded=true)", lessonsForStudent.first().downloaded)

        val unlockMap = LessonProgressionTracker.computeUnlockMap(
            lessons = lessonsForStudent,
            studentProfileId = studentProfile!!.id,
            bypassLockForStaff = false
        )
        assertEquals(
            "Educator's newly uploaded video must be immediately UNLOCKED for Student Asha",
            true,
            unlockMap[uploadedLesson.id]
        )
        assertTrue(
            "Persisted offline video file must still exist after switching to Student",
            File(lessonsForStudent.first().localFilePath!!).length() == sourceMp4Bytes.size.toLong()
        )

        // 8. Verify ExoPlayer PlayerView XML uses TextureView (fixes Compose Dialog black-screen bug)
        val playerView = LayoutInflater.from(context)
            .inflate(R.layout.view_exo_texture_player, null) as PlayerView
        assertTrue(
            "PlayerView must use TextureView surface_type to render video frames inside Compose Dialog",
            playerView.videoSurfaceView is TextureView
        )
    }

    @Test
    fun all12BundledNcertAnimatedVideos_existInApkAndExtractOffline() = runBlocking {
        val repository = TwogetherRepository(context)
        repository.ensureLocalSeedData(TwogetherRepository.LOCAL_WORKSPACE_UID)

        val lessons = repository.observeLessons().first()
        assertTrue("Expected at least 12 NCERT syllabus lessons", lessons.size >= 12)

        val coreLessons = lessons.filterNot { it.id.startsWith("lesson_edu_") }
        assertEquals("Expected 12 unique NCERT syllabus lessons", 12, coreLessons.size)

        val uniqueRawNames = coreLessons.map { it.rawResName }.toSet()
        assertEquals("Every syllabus lesson must have its own unique bundled animated MP4", 12, uniqueRawNames.size)

        for (lesson in coreLessons) {
            assertTrue("Lesson ${lesson.id} must be pre-bundled offline (downloaded=true)", lesson.downloaded)
            val file = lesson.localFilePath?.let { File(it) }
            assertNotNull("Lesson ${lesson.id} must have materialized local MP4 path", file)
            assertTrue(
                "Bundled animated MP4 for ${lesson.id} must exist and be > 100 KB (real animated video)",
                file!!.exists() && file.length() > 100_000L
            )
            val quizzes = repository.getQuizForLesson(lesson.id)
            assertTrue("Lesson ${lesson.id} should have at least 3 NCERT quiz questions", quizzes.size >= 3)
        }
    }
}
