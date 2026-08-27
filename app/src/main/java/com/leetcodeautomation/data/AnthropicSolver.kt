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

/**
 * Generates and fixes LeetCode solutions via Anthropic's native Messages API, which has a
 * different shape than the OpenAI-style chat completions format NvidiaSolver speaks: the system
 * prompt is a top-level field (not a "system" message), auth uses an `x-api-key` header plus an
 * `anthropic-version` header instead of a bearer token, and the response's text lives under
 * `content[0].text` rather than `choices[0].message.content`.
 */
class AnthropicSolver(
    private val apiKey: String,
    private val model: String = AiProvider.ANTHROPIC.defaultModel,
) : AiSolver {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private suspend fun ask(userPrompt: String, language: SolveLanguage): Solution = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("model", model)
            put("max_tokens", 2048)
            put("temperature", 0.2)
            put("system", systemPrompt(language))
            putJsonArray("messages") {
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
            .url(AiProvider.ANTHROPIC.defaultBaseUrl)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
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
            val text = root["content"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.contentOrNull
                ?: throw SolverException("Unexpected AI provider response: $respBody")

            Solution(code = extractFencedCode(text), explanation = text)
        }
    }

    override suspend fun solve(title: String, contentHtml: String, starterCode: String, language: SolveLanguage): Solution =
        ask(solvePrompt(title, contentHtml, starterCode, language), language)

    override suspend fun fix(title: String, previousCode: String, errorSummary: String, language: SolveLanguage): Solution =
        ask(fixPrompt(title, previousCode, errorSummary, language), language)
}
