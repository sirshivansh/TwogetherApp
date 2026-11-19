package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PortalAccountDao {
    @Query("SELECT * FROM portal_accounts WHERE ownerUid = :uid ORDER BY role ASC, displayName ASC")
    fun observeAccountsForUser(uid: String): Flow<List<PortalAccountEntity>>

    @Query("SELECT * FROM portal_accounts WHERE LOWER(email) = LOWER(:email) AND ownerUid = :uid LIMIT 1")
    suspend fun getAccountByEmail(uid: String, email: String): PortalAccountEntity?

    @Query("SELECT * FROM portal_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getAnyAccountByEmail(email: String): PortalAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: PortalAccountEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(accounts: List<PortalAccountEntity>)

    @Query("DELETE FROM portal_accounts WHERE ownerUid = :uid AND linkedEntityId = :linkedEntityId")
    suspend fun deleteByLinkedId(uid: String, linkedEntityId: String)
}

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM student_profiles WHERE ownerUid = :uid ORDER BY updatedAtMillis DESC")
    fun observeProfilesForUser(uid: String): Flow<List<StudentProfileEntity>>

    @Query("SELECT * FROM student_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): StudentProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: StudentProfileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(profiles: List<StudentProfileEntity>)

    @Query("DELETE FROM student_profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: String)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY CASE WHEN id LIKE 'lesson_edu_%' THEN 0 ELSE 1 END ASC, gradeClass ASC, subject ASC")
    fun observeAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Query("SELECT * FROM lessons ORDER BY gradeClass ASC, subject ASC")
    suspend fun getAllLessonsOnce(): List<LessonEntity>

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun countLessons(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons WHERE id = :lessonId")
    suspend fun deleteLesson(lessonId: String)
}

@Dao
interface QuizQuestionDao {
    @Query("SELECT * FROM quiz_questions WHERE lessonId = :lessonId")
    suspend fun getQuestionsForLesson(lessonId: String): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_questions")
    suspend fun getAllQuestionsOnce(): List<QuizQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestion(question: QuizQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(questions: List<QuizQuestionEntity>)

    @Query("DELETE FROM quiz_questions WHERE lessonId = :lessonId")
    suspend fun deleteQuestionsForLesson(lessonId: String)
}

@Dao
interface DoubtQueueDao {
    @Query("SELECT * FROM doubt_queue WHERE ownerUid = :uid ORDER BY createdAtMillis DESC")
    fun observeDoubtsForUser(uid: String): Flow<List<DoubtQueueEntity>>

    @Query("SELECT * FROM doubt_queue WHERE syncStatus = 'pending'")
    suspend fun getPendingDoubts(): List<DoubtQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDoubt(doubt: DoubtQueueEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(doubts: List<DoubtQueueEntity>)

    @Query("UPDATE doubt_queue SET syncStatus = :status, educatorReply = :reply WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, reply: String?)

    @Query("DELETE FROM doubt_queue WHERE id = :id")
    suspend fun deleteDoubt(id: String)
}

@Dao
interface EducatorProfileDao {
    @Query("SELECT * FROM educator_profiles WHERE ownerUid = :uid ORDER BY onboardingStep DESC, hoursTaught DESC")
    fun observeEducatorsForUser(uid: String): Flow<List<EducatorProfileEntity>>

    @Query("SELECT * FROM educator_profiles WHERE ownerUid = :uid LIMIT 1")
    fun observeEducatorForUser(uid: String): Flow<EducatorProfileEntity?>

    @Query("SELECT * FROM educator_profiles WHERE id = :educatorId LIMIT 1")
    suspend fun getEducatorById(educatorId: String): EducatorProfileEntity?

    @Query("SELECT * FROM educator_profiles WHERE ownerUid = :uid AND LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getEducatorByEmail(uid: String, email: String): EducatorProfileEntity?

    @Query("SELECT * FROM educator_profiles WHERE ownerUid = :uid LIMIT 1")
    suspend fun getEducatorForUser(uid: String): EducatorProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEducator(educator: EducatorProfileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(educators: List<EducatorProfileEntity>)

    @Query("DELETE FROM educator_profiles WHERE id = :educatorId")
    suspend fun deleteEducator(educatorId: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM ai_chat_messages WHERE studentProfileId = :profileId ORDER BY createdAtMillis ASC")
    fun observeMessages(profileId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM ai_chat_messages WHERE studentProfileId = :profileId")
    suspend fun clearForProfile(profileId: String)
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY createdAtMillis DESC LIMIT 30")
    fun observeAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM app_notifications")
    suspend fun countNotifications(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(notifications: List<AppNotificationEntity>)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllRead()
}

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework_assignments ORDER BY createdAtMillis DESC")
    fun observeAllHomeworks(): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework_assignments ORDER BY createdAtMillis DESC")
    suspend fun getAllHomeworksOnce(): List<HomeworkEntity>

    @Query("SELECT COUNT(*) FROM homework_assignments")
    suspend fun countHomeworks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHomework(homework: HomeworkEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(homeworks: List<HomeworkEntity>)
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts WHERE studentProfileId = :profileId ORDER BY attemptedAtMillis DESC")
    fun observeAttemptsForStudent(profileId: String): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(attempts: List<QuizAttemptEntity>)
}
