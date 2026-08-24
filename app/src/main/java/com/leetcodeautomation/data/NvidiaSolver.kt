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

private const val SYSTEM_PROMPT = """You are an expert competitive programmer. You write correct, efficient
Python 3 solutions for LeetCode problems that fit the given starter code signature exactly.
Always respond with:
1. A brief explanation of the approach.
2. Time and space complexity.
3. The final code in a single ```python fenced block containing ONLY the completed class/function
matching the provided starter code (no imports beyond typing/collections/etc. if needed, no test code)."""

/**
 * Generates and fixes LeetCode solutions via an OpenAI-compatible chat completions API.
 * Defaults to NVIDIA NIM, but [baseUrl] can point at any compatible provider (OpenAI, Groq,
 * Together AI, etc.) so a user isn't locked into one AI vendor.
 */
class NvidiaSolver(
    private val apiKey: String,
    private val model: String = "meta/llama-3.3-70b-instruct",
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://integrate.api.nvidia.com/v1/chat/completions"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private fun extractCode(text: String): String {
        val regex = Regex("```(?:python3?|py)?\\s*\\n(.*?)```", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(text)
            ?: throw SolverException("AI response did not contain a fenced Python code block")
        return match.groupValues[1].trim()
    }

    private suspend fun ask(userPrompt: String): Solution = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("model", model)
            put("max_tokens", 4096)
            put("temperature", 0.2)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
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

            Solution(code = extractCode(text), explanation = text)
        }
    }

    suspend fun solve(title: String, contentHtml: String, starterCode: String): Solution {
        val prompt = """
            Solve this LeetCode problem.

            Title: $title

            Problem statement (HTML):
            $contentHtml

            Starter code (Python 3) — complete it, keep the class/function signature identical:
            ```python
            $starterCode
            ```
        """.trimIndent()
        return ask(prompt)
    }

    suspend fun fix(title: String, previousCode: String, errorSummary: String): Solution {
        val prompt = """
            Your previous submission for the LeetCode problem "$title" was rejected by the judge.

            Previous code:
            ```python
            $previousCode
            ```

            Judge feedback:
            $errorSummary

            Diagnose the bug and provide a corrected, complete solution with the same signature.
        """.trimIndent()
        return ask(prompt)
    }
}

/** True once the user has supplied *some* AI provider credential (NVIDIA's, or a custom one). */
val Settings.hasAiCredential: Boolean
    get() = nvidiaApiKey.isNotBlank() || customApiKey.isNotBlank()

/** Builds a solver using the custom provider if configured, falling back to NVIDIA otherwise. */
fun Settings.toSolver(): NvidiaSolver = NvidiaSolver(
    apiKey = customApiKey.ifBlank { nvidiaApiKey },
    model = aiModel,
    baseUrl = customApiBaseUrl.ifBlank { NvidiaSolver.DEFAULT_BASE_URL },
)
