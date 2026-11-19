package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PortalAccountEntity::class,
        StudentProfileEntity::class,
        LessonEntity::class,
        QuizQuestionEntity::class,
        DoubtQueueEntity::class,
        EducatorProfileEntity::class,
        ChatMessageEntity::class,
        AppNotificationEntity::class,
        HomeworkEntity::class,
        QuizAttemptEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class TwogetherDatabase : RoomDatabase() {
    abstract fun portalAccountDao(): PortalAccountDao
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun lessonDao(): LessonDao
    abstract fun quizQuestionDao(): QuizQuestionDao
    abstract fun doubtQueueDao(): DoubtQueueDao
    abstract fun educatorProfileDao(): EducatorProfileDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun appNotificationDao(): AppNotificationDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun quizAttemptDao(): QuizAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: TwogetherDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_notifications` (
                        `id` TEXT NOT NULL,
                        `studentProfileId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `targetTab` TEXT NOT NULL,
                        `isRead` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `portal_accounts` (
                        `email` TEXT NOT NULL,
                        `ownerUid` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `linkedEntityId` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`email`)
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE `student_profiles` ADD COLUMN `email` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `student_profiles` ADD COLUMN `preferredLanguage` TEXT NOT NULL DEFAULT 'Bilingual'")
                db.execSQL("ALTER TABLE `student_profiles` ADD COLUMN `parentalConsentGiven` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `student_profiles` ADD COLUMN `centerName` TEXT NOT NULL DEFAULT 'Asha Kiran Kendra'")

                db.execSQL("ALTER TABLE `lessons` ADD COLUMN `studyNotes` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lessons` ADD COLUMN `language` TEXT NOT NULL DEFAULT 'Bilingual'")
                db.execSQL("ALTER TABLE `lessons` ADD COLUMN `uploadedByEducatorEmail` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lessons` ADD COLUMN `uploadedByEducatorName` TEXT NOT NULL DEFAULT 'Twogether Curriculum'")
                db.execSQL("ALTER TABLE `lessons` ADD COLUMN `qualityStatus` TEXT NOT NULL DEFAULT 'APPROVED'")
                db.execSQL("ALTER TABLE `lessons` ADD COLUMN `feedbackRating` REAL NOT NULL DEFAULT 5.0")
                db.execSQL("ALTER TABLE `lessons` ADD COLUMN `feedbackCount` INTEGER NOT NULL DEFAULT 1")

                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `email` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `referenceContact` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `docVerificationStatus` TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `docAiSummary` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `docConfidence` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `docExtractedName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `docExtractedBoard` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `isBackupRoster` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `adminApproved` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `educator_profiles` ADD COLUMN `lessonsUploadedCount` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `lessons` ADD COLUMN `watchedByProfileIds` TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `homework_assignments` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `gradeClass` INTEGER NOT NULL,
                        `dueDateLabel` TEXT NOT NULL,
                        `assignedByName` TEXT NOT NULL,
                        `completedByProfileIds` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quiz_attempts` (
                        `id` TEXT NOT NULL,
                        `studentProfileId` TEXT NOT NULL,
                        `lessonId` TEXT NOT NULL,
                        `lessonTitle` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `gradeClass` INTEGER NOT NULL,
                        `score` INTEGER NOT NULL,
                        `total` INTEGER NOT NULL,
                        `passed` INTEGER NOT NULL,
                        `attemptedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): TwogetherDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TwogetherDatabase::class.java,
                    "twogether_offline.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
