package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.local.TwogetherDatabase
import com.example.data.remote.FirestoreDoubt
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class DoubtSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val uid = Firebase.auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Log.w(TAG, "Skipping doubt sync: user is not signed in.")
            return Result.success()
        }

        val db = TwogetherDatabase.getInstance(applicationContext)
        val doubtDao = db.doubtQueueDao()
        val pending = doubtDao.getPendingDoubts().filter { it.ownerUid == uid }
        if (pending.isEmpty()) return Result.success()

        val databaseId = applicationContext.getString(R.string.firestore_database_id)
        val firestore = FirebaseFirestore.getInstance(databaseId)

        var anyFailed = false
        for (item in pending) {
            val docPath = "users/$uid/doubts/${item.id}"
            try {
                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("doubts")
                    .document(item.id)
                val snap = docRef.get().await()
                if (snap.exists()) {
                    val existingStatus = snap.getString("status") ?: "synced"
                    if (existingStatus != "answered") {
                        docRef.update(
                            mapOf(
                                "status" to "synced",
                                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                } else {
                    val payload = FirestoreDoubt(
                        userId = uid,
                        studentProfileId = item.studentProfileId,
                        studentName = item.studentName,
                        gradeClass = item.gradeClass.toLong().coerceIn(1L, 10L),
                        subject = item.subject,
                        questionText = item.questionText,
                        status = "synced",
                        educatorReply = item.educatorReply
                    ).toCreateMap()
                    docRef.set(payload).await()
                }

                doubtDao.updateSyncStatus(item.id, "synced", item.educatorReply)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, docPath)
                anyFailed = true
            }
        }

        return if (anyFailed) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "DoubtSyncWorker"
        private const val UNIQUE_WORK_NAME = "twogether_doubt_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<DoubtSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
