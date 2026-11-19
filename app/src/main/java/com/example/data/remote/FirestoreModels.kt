package com.example.data.remote

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class FirestoreStudentProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val gradeClass: Long = 1L,
    val avatarId: String = "lion",
    val themeColorKey: String = "terracotta",
    val preferredLanguage: String = "Bilingual",
    val parentalConsentGiven: Boolean = true,
    val centerName: String = "Asha Kiran Kendra",
    val pinSequence: String = "0,1,2,3",
    val xpPoints: Long = 0L,
    val modulesCompleted: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "name" to name.take(50),
        "email" to email.take(120),
        "gradeClass" to gradeClass.coerceIn(1L, 10L),
        "avatarId" to avatarId,
        "themeColorKey" to themeColorKey,
        "preferredLanguage" to preferredLanguage.ifBlank { "Bilingual" }.take(30),
        "parentalConsentGiven" to parentalConsentGiven,
        "centerName" to centerName.ifBlank { "Asha Kiran Kendra" }.take(80),
        "pinSequence" to pinSequence,
        "xpPoints" to xpPoints.coerceAtLeast(0L),
        "modulesCompleted" to modulesCompleted.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class FirestoreDoubt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentProfileId: String = "",
    val studentName: String = "",
    val gradeClass: Long = 1L,
    val subject: String = "",
    val questionText: String = "",
    val status: String = "synced",
    val educatorReply: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val base = mutableMapOf<String, Any?>(
            "userId" to userId,
            "studentProfileId" to studentProfileId,
            "studentName" to studentName.take(50),
            "gradeClass" to gradeClass.coerceIn(1L, 10L),
            "subject" to subject.take(40),
            "questionText" to questionText.take(500),
            "status" to status,
            "educatorReply" to educatorReply?.take(1000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        @Suppress("UNCHECKED_CAST")
        return base.filterValues { it != null } as Map<String, Any>
    }
}

@Keep
@IgnoreExtraProperties
data class FirestoreEducatorProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val tier: String = "TIER_1_PAID",
    val board12th: String = "",
    val rollYear: String = "",
    val stream: String = "Science",
    val subjectSpecialty: String = "Mathematics",
    val referenceContact: String = "",
    val documentAttached: Boolean = false,
    val docVerificationStatus: String = "PENDING",
    val docAiSummary: String = "",
    val docConfidence: Long = 0L,
    val isBackupRoster: Boolean = false,
    val adminApproved: Boolean = false,
    val testScore: Long = 0L,
    val contractSigned: Boolean = false,
    val trainingCompleted: Boolean = false,
    val onboardingStep: Long = 1L,
    val hoursTaught: Double = 0.0,
    val doubtsResolved: Long = 0L,
    val studentsMentored: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "fullName" to fullName.take(80),
        "email" to email.take(120),
        "tier" to tier,
        "board12th" to board12th.ifBlank { "CBSE" }.take(80),
        "rollYear" to rollYear.ifBlank { "2022" }.take(30),
        "stream" to stream.ifBlank { "Science" }.take(40),
        "subjectSpecialty" to subjectSpecialty.ifBlank { "Mathematics" }.take(50),
        "referenceContact" to referenceContact.take(120),
        "documentAttached" to documentAttached,
        "docVerificationStatus" to docVerificationStatus,
        "docAiSummary" to docAiSummary.take(400),
        "docConfidence" to docConfidence.coerceIn(0L, 100L),
        "isBackupRoster" to isBackupRoster,
        "adminApproved" to adminApproved,
        "testScore" to testScore.coerceIn(0L, 3L),
        "contractSigned" to contractSigned,
        "trainingCompleted" to trainingCompleted,
        "onboardingStep" to onboardingStep.coerceIn(1L, 5L),
        "hoursTaught" to hoursTaught.coerceAtLeast(0.0),
        "doubtsResolved" to doubtsResolved.coerceAtLeast(0L),
        "studentsMentored" to studentsMentored.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class FirestoreLiveSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val gradeRange: String = "",
    val educatorName: String = "",
    val educatorTier: String = "TIER_1_PAID",
    val openSession: Boolean = true,
    val participantCount: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(100),
        "subject" to subject.take(40),
        "gradeRange" to gradeRange.take(30),
        "educatorName" to educatorName.take(80),
        "educatorTier" to educatorTier,
        "openSession" to openSession,
        "participantCount" to participantCount.coerceIn(0L, 500L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class FirestoreUploadedLesson(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val gradeClass: Long = 5L,
    val durationLabel: String = "8 min",
    val summary: String = "",
    val studyNotes: String = "",
    val language: String = "Bilingual",
    val uploadedByEducatorEmail: String = "",
    val uploadedByEducatorName: String = "Volunteer Educator",
    val qualityStatus: String = "APPROVED",
    val feedbackRating: Double = 5.0,
    val feedbackCount: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(100),
        "subject" to subject.take(40),
        "gradeClass" to gradeClass.coerceIn(1L, 10L),
        "durationLabel" to durationLabel.ifBlank { "8 min" }.take(30),
        "summary" to summary.take(300),
        "studyNotes" to studyNotes.take(1500),
        "language" to language.ifBlank { "Bilingual" }.take(30),
        "uploadedByEducatorEmail" to uploadedByEducatorEmail.take(120),
        "uploadedByEducatorName" to uploadedByEducatorName.ifBlank { "Volunteer Educator" }.take(80),
        "qualityStatus" to qualityStatus,
        "feedbackRating" to feedbackRating.coerceIn(0.0, 5.0),
        "feedbackCount" to feedbackCount.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}
