package com.leetcodeautomation.data

/**
 * Picks problems automatically so the user never has to type a slug: today's Daily Challenge
 * first, then the configured backup slugs if more targets are needed. Shared by the Solve
 * button (one target) and the background streak worker (up to problemsPerRun targets).
 */
object ProblemPicker {
    suspend fun pickTargets(leetcode: LeetCodeClient, backupSlugs: String, count: Int): List<String> {
        val backups = backupSlugs.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return buildList {
            runCatching { leetcode.fetchDailyChallengeSlug() }.getOrNull()?.let { add(it) }
            addAll(backups)
        }.distinct().take(count.coerceAtLeast(1))
    }

    suspend fun pickOne(leetcode: LeetCodeClient, backupSlugs: String): String? =
        pickTargets(leetcode, backupSlugs, 1).firstOrNull()
}
