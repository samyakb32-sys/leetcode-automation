package com.leetcodeautomation.data

/**
 * Picks problems automatically so the user never has to type a slug: today's Daily Challenge
 * first, then the configured backup slugs if more targets are needed. Shared by the Solve
 * button (one target) and the background streak worker (up to problemsPerRun targets).
 */
object ProblemPicker {
    /**
     * [excludeSlugs] is normally the set of already-accepted slugs, so repeated taps don't just
     * hand back today's Daily Challenge forever — falls back to including them anyway once every
     * candidate has been solved, rather than erroring out with nothing left to try.
     */
    suspend fun pickTargets(
        leetcode: LeetCodeClient,
        backupSlugs: String,
        count: Int,
        excludeSlugs: Set<String> = emptySet(),
    ): List<String> {
        val backups = backupSlugs.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val candidates = buildList {
            runCatching { leetcode.fetchDailyChallengeSlug() }.getOrNull()?.let { add(it) }
            addAll(backups)
        }.distinct()
        val unsolved = candidates.filterNot { it in excludeSlugs }
        return unsolved.ifEmpty { candidates }.take(count.coerceAtLeast(1))
    }

    suspend fun pickOne(leetcode: LeetCodeClient, backupSlugs: String, excludeSlugs: Set<String> = emptySet()): String? =
        pickTargets(leetcode, backupSlugs, 1, excludeSlugs).firstOrNull()

    /** Today's Daily Challenge slug, or null if it couldn't be fetched. */
    suspend fun pickDaily(leetcode: LeetCodeClient): String? =
        runCatching { leetcode.fetchDailyChallengeSlug() }.getOrNull()

    /**
     * Picks from the configured backup slugs only — never the Daily Challenge — for a "solve
     * something else" action. Skips [excludeSlugs] (already-accepted problems) first, falling
     * back to repeats once the whole backup list is exhausted.
     */
    fun pickFromBackups(backupSlugs: String, excludeSlugs: Set<String> = emptySet()): String? {
        val backups = backupSlugs.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val unsolved = backups.filterNot { it in excludeSlugs }
        return unsolved.ifEmpty { backups }.firstOrNull()
    }
}
