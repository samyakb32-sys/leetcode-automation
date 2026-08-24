package com.leetcodeautomation.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SolverException(message: String) : Exception(message)

/** Generates and fixes LeetCode solutions. Implemented by each AI provider's own request shape. */
interface AiSolver {
    suspend fun solve(title: String, contentHtml: String, starterCode: String, language: SolveLanguage): Solution
    suspend fun fix(title: String, previousCode: String, errorSummary: String, language: SolveLanguage): Solution
}

internal fun systemPrompt(language: SolveLanguage) = """You are an expert competitive programmer. You write correct, efficient
${language.promptLabel} solutions for LeetCode problems that fit the given starter code signature exactly.
Always respond with:
1. A brief explanation of the approach.
2. Time and space complexity.
3. The final code in a single ```${language.fenceTag} fenced block containing ONLY the completed class/function
matching the provided starter code (no extra includes/imports beyond what's needed, no test code, no main function
unless the starter code already has one)."""

internal fun solvePrompt(title: String, contentHtml: String, starterCode: String, language: SolveLanguage) = """
    Solve this LeetCode problem.

    Title: $title

    Problem statement (HTML):
    $contentHtml

    Starter code (${language.promptLabel}) — complete it, keep the class/function signature identical:
    ```${language.fenceTag}
    $starterCode
    ```
""".trimIndent()

internal fun fixPrompt(title: String, previousCode: String, errorSummary: String, language: SolveLanguage) = """
    Your previous submission for the LeetCode problem "$title" was rejected by the judge.

    Previous code:
    ```${language.fenceTag}
    $previousCode
    ```

    Judge feedback:
    $errorSummary

    Diagnose the bug and provide a corrected, complete solution with the same signature.
""".trimIndent()

internal fun extractFencedCode(text: String): String {
    val regex = Regex("```(?:\\w+)?\\s*\\n(.*?)```", RegexOption.DOT_MATCHES_ALL)
    val match = regex.find(text)
        ?: throw SolverException("AI response did not contain a fenced code block")
    return match.groupValues[1].trim()
}

/**
 * Generates and fixes LeetCode solutions via an OpenAI-compatible chat completions API.
 * Defaults to NVIDIA NIM, but [baseUrl] can point at any compatible provider (OpenAI, Groq,
 * Gemini's OpenAI-compat endpoint, etc.) so a user isn't locked into one AI vendor.
 */
class NvidiaSolver(
    private val apiKey: String,
    private val model: String = AiProvider.NVIDIA.defaultModel,
    private val baseUrl: String = AiProvider.NVIDIA.defaultBaseUrl,
) : AiSolver {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private suspend fun ask(userPrompt: String, language: SolveLanguage): Solution = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("model", model)
            // 2048 covers the explanation + code for the vast majority of LeetCode problems and
            // generates noticeably faster than 4096 without truncating real answers.
            put("max_tokens", 2048)
            put("temperature", 0.2)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", systemPrompt(language))
                })
                add(buildJsonObject {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }
        }

        val body = json.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            payload,
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val resp = http.newCall(request).execute()
        resp.use {
            if (!it.isSuccessful) {
                throw SolverException("AI provider error: HTTP ${it.code} ${it.body?.string()}")
            }
            val respBody = it.body?.string().orEmpty()
            val root = json.parseToJsonElement(respBody).jsonObject
            val text = root["choices"]?.jsonArray?.get(0)?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.contentOrNull
                ?: throw SolverException("Unexpected AI provider response: $respBody")

            Solution(code = extractFencedCode(text), explanation = text)
        }
    }

    override suspend fun solve(title: String, contentHtml: String, starterCode: String, language: SolveLanguage): Solution =
        ask(solvePrompt(title, contentHtml, starterCode, language), language)

    override suspend fun fix(title: String, previousCode: String, errorSummary: String, language: SolveLanguage): Solution =
        ask(fixPrompt(title, previousCode, errorSummary, language), language)
}

/** True once the user has supplied a credential for their selected AI provider. */
val Settings.hasAiCredential: Boolean
    get() = when (AiProvider.fromId(aiProvider)) {
        AiProvider.NVIDIA -> nvidiaApiKey.isNotBlank()
        AiProvider.OPENAI -> openaiApiKey.isNotBlank()
        AiProvider.GROQ -> groqApiKey.isNotBlank()
        AiProvider.GEMINI -> geminiApiKey.isNotBlank()
        AiProvider.ANTHROPIC -> anthropicApiKey.isNotBlank()
        AiProvider.CUSTOM -> customApiKey.isNotBlank()
    }

/** Builds a solver for whichever AI provider the user has selected. */
fun Settings.toSolver(): AiSolver {
    val model = aiModel.ifBlank { AiProvider.fromId(aiProvider).defaultModel }
    return when (val provider = AiProvider.fromId(aiProvider)) {
        AiProvider.NVIDIA -> NvidiaSolver(apiKey = nvidiaApiKey, model = model, baseUrl = provider.defaultBaseUrl)
        AiProvider.OPENAI -> NvidiaSolver(apiKey = openaiApiKey, model = model, baseUrl = provider.defaultBaseUrl)
        AiProvider.GROQ -> NvidiaSolver(apiKey = groqApiKey, model = model, baseUrl = provider.defaultBaseUrl)
        AiProvider.GEMINI -> NvidiaSolver(apiKey = geminiApiKey, model = model, baseUrl = provider.defaultBaseUrl)
        AiProvider.ANTHROPIC -> AnthropicSolver(apiKey = anthropicApiKey, model = model)
        AiProvider.CUSTOM -> NvidiaSolver(
            apiKey = customApiKey,
            model = model,
            baseUrl = customApiBaseUrl.ifBlank { AiProvider.NVIDIA.defaultBaseUrl },
        )
    }
}
