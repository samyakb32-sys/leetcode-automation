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
}
