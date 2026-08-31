package com.leetcodeautomation.data

/**
 * Picks problems automatically so the user never has to type a slug.
 *
 * "Already solved" is judged by LeetCode's own record for the account (a problem's `status`),
 * combined with the app's local run history — relying on local history alone meant anything the
 * user had solved directly on leetcode.com looked unsolved and got attempted again.
 */
object ProblemPicker {
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

    /** Today's Daily Challenge slug, or null if it couldn't be fetched. */
    suspend fun pickDaily(leetcode: LeetCodeClient): String? =
        runCatching { leetcode.fetchDailyChallengeSlug() }.getOrNull()

    /**
     * An unsolved Easy problem to practice on — never the Daily Challenge.
     *
     * Prefers LeetCode's own list of Easy problems the account hasn't solved, so it keeps finding
     * fresh ones without the user maintaining a slug list, and falls back to the configured backup
     * slugs (also filtered to unsolved + Easy) when that list can't be fetched.
     */
    suspend fun pickPractice(
        leetcode: LeetCodeClient,
        backupSlugs: String,
        excludeSlugs: Set<String> = emptySet(),
    ): String? {
        // Random rather than first, so a stale "not started" status can't pin us to one problem.
        leetcode.fetchUnsolvedEasySlugs()
            .filterNot { it in excludeSlugs }
            .randomOrNull()
            ?.let { return it }

        val backups = backupSlugs.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        var lookupFailed = false
        for (slug in backups) {
            if (slug in excludeSlugs) continue
            val meta = leetcode.fetchProblemMeta(slug)
            when {
                // Couldn't determine anything (network/rate limit). Keep looking, but remember so
                // we don't claim the list is exhausted when we simply couldn't check.
                meta == null -> lookupFailed = true
                meta.solved || meta.paidOnly -> Unit
                meta.difficulty == "Easy" -> return slug
            }
        }
        if (lookupFailed) {
            throw LeetCodeException("Couldn't check problems on LeetCode — check your connection and try again.")
        }
        return null
    }
}
