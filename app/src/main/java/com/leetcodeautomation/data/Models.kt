package com.leetcodeautomation.data

data class Problem(
    val questionId: String,
    val title: String,
    val titleSlug: String,
    val difficulty: String,
    val contentHtml: String,
    val starterCode: String,
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
