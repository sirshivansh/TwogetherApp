package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portal_accounts")
data class PortalAccountEntity(
    @PrimaryKey val email: String,
    val ownerUid: String,
    val role: String, // "ADMIN", "STUDENT", "EDUCATOR"
    val passwordHash: String,
    val displayName: String,
    val linkedEntityId: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_profiles")
data class StudentProfileEntity(
    @PrimaryKey val id: String,
    val ownerUid: String,
    val name: String,
    val email: String = "",
    val gradeClass: Int, // 1..10
    val avatarId: String,
    val themeColorKey: String,
    val preferredLanguage: String = "Bilingual", // "English", "Hindi", "Bilingual"
    val parentalConsentGiven: Boolean = true,
    val centerName: String = "Asha Kiran Kendra",
    val pinSequence: String, // 4-tap color/shape PIN e.g. "0,1,2,3"
    val xpPoints: Int = 0,
    val modulesCompleted: Int = 0,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val gradeClass: Int, // 1..10
    val durationLabel: String,
    val summary: String,
    val studyNotes: String = "",
    val language: String = "Bilingual",
    val rawResName: String, // e.g., "lesson_fractions"
    val localFilePath: String? = null,
    val downloaded: Boolean = false,
    val watchedByProfileIds: String = "", // comma-separated profile IDs that watched the video
    val completedByProfileIds: String = "", // comma-separated profile IDs that passed the quiz
    val uploadedByEducatorEmail: String = "",
    val uploadedByEducatorName: String = "Twogether Curriculum",
    val qualityStatus: String = "APPROVED", // "APPROVED", "PENDING_REVIEW"
    val feedbackRating: Double = 5.0,
    val feedbackCount: Int = 1
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int, // 0..3
    val explanation: String
)

@Entity(tableName = "doubt_queue")
data class DoubtQueueEntity(
    @PrimaryKey val id: String,
    val ownerUid: String,
    val studentProfileId: String,
    val studentName: String,
    val gradeClass: Int,
    val subject: String,
    val questionText: String,
    val attachmentUri: String? = null,
    val syncStatus: String = "pending", // "pending", "synced", "answered"
    val educatorReply: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "educator_profiles")
data class EducatorProfileEntity(
    @PrimaryKey val id: String,
    val ownerUid: String,
    val fullName: String,
    val email: String = "",
    val tier: String = "TIER_1_PAID", // TIER_1_PAID, TIER_2_UNPAID, TIER_3_RETIRED
    val board12th: String = "",
    val rollYear: String = "",
    val stream: String = "Science",
    val subjectSpecialty: String = "Mathematics",
    val referenceContact: String = "",
    val documentAttached: Boolean = false,
    val documentUri: String? = null,
    val docVerificationStatus: String = "PENDING", // PENDING, AI_VERIFIED, NEEDS_REVIEW, ADMIN_APPROVED, REJECTED
    val docAiSummary: String = "",
    val docConfidence: Int = 0,
    val docExtractedName: String = "",
    val docExtractedBoard: String = "",
    val isBackupRoster: Boolean = false,
    val adminApproved: Boolean = false,
    val lessonsUploadedCount: Int = 0,
    val testScore: Int = 0,
    val contractSigned: Boolean = false,
    val trainingCompleted: Boolean = false,
    val onboardingStep: Int = 1, // 1..5
    val hoursTaught: Double = 0.0,
    val doubtsResolved: Int = 0,
    val studentsMentored: Int = 0
)

@Entity(tableName = "ai_chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val studentProfileId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val engineBadge: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey val id: String,
    val studentProfileId: String, // "all" or specific profile id
    val title: String,
    val message: String,
    val category: String = "homework", // "homework", "doubt", "live", "tree", "educator_verify"
    val targetTab: String = "backpack", // "backpack", "ai_chat", "doubts", "tree"
    val isRead: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "homework_assignments")
data class HomeworkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val gradeClass: Int,
    val dueDateLabel: String,
    val assignedByName: String = "Priya Sharma",
    val completedByProfileIds: String = "", // comma-separated profile IDs
    val syncStatus: String = "synced", // "synced", "queued_offline"
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val studentProfileId: String,
    val lessonId: String,
    val lessonTitle: String,
    val subject: String,
    val gradeClass: Int,
    val score: Int,
    val total: Int,
    val passed: Boolean,
    val attemptedAtMillis: Long = System.currentTimeMillis()
)
