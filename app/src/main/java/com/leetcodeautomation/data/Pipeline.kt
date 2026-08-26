package com.leetcodeautomation.data

/**
 * Runs: extract problem -> AI solve -> submit -> (accepted? done : fix -> submit again),
 * emitting each attempt via [onStep] so the UI can show live progress.
 */
class Pipeline(
    private val leetcode: LeetCodeClient,
    private val solver: AiSolver,
) {
    suspend fun run(
        titleSlug: String,
        maxFixAttempts: Int,
        language: SolveLanguage = SolveLanguage.PYTHON3,
        onStep: suspend (PipelineStep) -> Unit,
    ): PipelineStep {
        val problem = leetcode.fetchProblem(titleSlug, language.langSlug)
        var solution = solver.solve(problem.title, problem.contentHtml, problem.starterCode, language)

        val attempts = maxFixAttempts.coerceAtLeast(1)
        var lastStep: PipelineStep? = null
        for (attempt in 1..attempts) {
            val result = leetcode.submitSolution(problem.titleSlug, problem.questionId, solution.code, language.langSlug)
            val step = PipelineStep(attempt, solution, result)
            onStep(step)
            lastStep = step

            if (result.accepted || attempt == attempts) break
            solution = solver.fix(problem.title, solution.code, result.errorSummary, language)
        }
        return lastStep!!
    }
}
