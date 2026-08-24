package com.leetcodeautomation.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val STREAK_WORK_NAME = "leetcode_streak_automation"

/**
 * Runs automatically on WorkManager's schedule to protect the user's LeetCode streak: solves
 * today's daily challenge (plus configured backup slugs) up to `problemsPerRun` problems.
 */
class StreakWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = SettingsRepository(applicationContext).load()
        if (!settings.streakEnabled) return Result.success()
        if (settings.leetcodeSession.isBlank() || settings.csrfToken.isBlank() || !settings.hasAiCredential) {
            return Result.failure()
        }

        val leetcode = LeetCodeClient(settings.leetcodeSession, settings.csrfToken)
        val solver = settings.toSolver()
        val pipeline = Pipeline(leetcode, solver)

        val backups = settings.backupSlugs.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val targets = buildList {
            runCatching { leetcode.fetchDailyChallengeSlug() }.getOrNull()?.let { add(it) }
            addAll(backups)
        }.distinct().take(settings.problemsPerRun.coerceAtLeast(1))

        if (targets.isEmpty()) return Result.failure()

        val language = SolveLanguage.fromSlug(settings.submissionLanguage)
        val solved = mutableListOf<String>()
        val failed = mutableListOf<String>()
        for (slug in targets) {
            val outcome = runCatching {
                pipeline.run(slug, settings.maxFixAttempts, language) { /* no live UI while backgrounded */ }
            }
            if (outcome.getOrNull()?.result?.accepted == true) solved.add(slug) else failed.add(slug)
        }

        StreakNotifier.notifyResult(applicationContext, solved, failed)
        return if (failed.isEmpty()) Result.success() else Result.failure()
    }
}

/** Schedules (or cancels) the recurring streak-automation work per the user's settings. */
object StreakScheduler {

    fun schedule(context: Context, settings: Settings) {
        val workManager = WorkManager.getInstance(context)
        if (!settings.streakEnabled) {
            workManager.cancelUniqueWork(STREAK_WORK_NAME)
            return
        }

        val initialDelayMs = millisUntilNext(settings.solveHour, settings.solveMinute)
        val repeatDays = settings.repeatEveryDays.coerceAtLeast(1)

        val request = PeriodicWorkRequestBuilder<StreakWorker>(repeatDays.toLong(), TimeUnit.DAYS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            STREAK_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(STREAK_WORK_NAME)
    }

    private fun millisUntilNext(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis - now.timeInMillis
    }
}
