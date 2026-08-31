package com.leetcodeautomation.data

/** A submission language the app can solve in. Add more here to support additional languages. */
enum class SolveLanguage(val langSlug: String, val displayName: String, val promptLabel: String, val fenceTag: String) {
    PYTHON3("python3", "Python", "Python 3", "python"),
    CPP("cpp", "C++", "C++17", "cpp");

    companion object {
        fun fromSlug(slug: String): SolveLanguage = entries.find { it.langSlug == slug } ?: PYTHON3
    }
}

data class Problem(
    val questionId: String,
    val title: String,
    val titleSlug: String,
    val difficulty: String,
    val contentHtml: String,
    val starterCode: String,
)

/** What LeetCode itself knows about a problem, used to skip solved/too-hard/premium targets. */
data class ProblemMeta(
    val difficulty: String?,
    val solved: Boolean,
    val paidOnly: Boolean,
)

data class Solution(
    val code: String,
    val explanation: String,
)

data class SubmissionResult(
    val accepted: Boolean,
    val statusMsg: String,
    val lastTestcase: String? = null,
    val expectedOutput: String? = null,
    val codeOutput: String? = null,
    val compileError: String? = null,
    val runtimeError: String? = null,
    val totalCorrect: Int? = null,
    val totalTestcases: Int? = null,
) {
    val errorSummary: String
        get() {
            if (accepted) return ""
            val parts = mutableListOf("Status: $statusMsg")
            compileError?.let { parts += "Compile error: $it" }
            runtimeError?.let { parts += "Runtime error: $it" }
            lastTestcase?.let { parts += "Failing input: $it" }
            expectedOutput?.let { parts += "Expected output: $it" }
            codeOutput?.let { parts += "Actual output: $it" }
            if (totalCorrect != null && totalTestcases != null) {
                parts += "Passed $totalCorrect/$totalTestcases testcases"
            }
            return parts.joinToString("\n")
        }
}

/** One step of the solve -> submit -> fix loop, for display in the UI log. */
data class PipelineStep(
    val attempt: Int,
    val solution: Solution,
    val result: SubmissionResult,
)
