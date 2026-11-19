package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.local.AppNotificationEntity
import com.example.data.local.TwogetherDatabase
import com.example.ui.components.localizedForCurrentLocale
import com.example.util.TwogetherNotifier
import java.util.UUID
import java.util.concurrent.TimeUnit

class HomeworkReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val localized = applicationContext.localizedForCurrentLocale()
        val db = TwogetherDatabase.getInstance(applicationContext)
        val lessons = db.lessonDao().getAllLessonsOnce()
        val pendingLesson = lessons.firstOrNull { lesson ->
            lesson.completedByProfileIds.isBlank()
        }

        val title = localized.getString(R.string.notif_pending_homework_title)
        val message = if (pendingLesson != null) {
            val subjectLabel = when (pendingLesson.subject.lowercase()) {
                "math", "maths" -> localized.getString(R.string.subject_math)
                "science" -> localized.getString(R.string.subject_science)
                "english" -> localized.getString(R.string.subject_english)
                "social studies" -> localized.getString(R.string.subject_social_studies)
                "hindi" -> localized.getString(R.string.subject_hindi)
                else -> pendingLesson.subject
            }
            localized.getString(R.string.notif_pending_homework_msg_lesson, subjectLabel, pendingLesson.title)
        } else {
            localized.getString(R.string.notif_pending_homework_msg_default)
        }

        val notif = AppNotificationEntity(
            id = "notif_${UUID.randomUUID().toString().take(12)}",
            studentProfileId = "all",
            title = title,
            message = message,
            category = "homework",
            targetTab = "backpack",
            isRead = false
        )
        db.appNotificationDao().insertNotification(notif)
        TwogetherNotifier.showSystemNotification(
            context = applicationContext,
            notificationId = notif.id.hashCode(),
            title = title,
            message = message
        )
        return Result.success()
    }

    companion object {
        private const val PERIODIC_REMINDER_WORK = "twogether_daily_homework_reminder"
        private const val INSTANT_REMINDER_WORK = "twogether_instant_homework_reminder"

        fun scheduleDailyReminders(context: Context) {
            val request = PeriodicWorkRequestBuilder<HomeworkReminderWorker>(
                12, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                PERIODIC_REMINDER_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun enqueueImmediateReminder(context: Context) {
            val request = OneTimeWorkRequestBuilder<HomeworkReminderWorker>().build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                INSTANT_REMINDER_WORK,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
