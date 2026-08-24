package com.leetcodeautomation.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class LeetCodeException(message: String) : Exception(message)

/** Talks to LeetCode's GraphQL + submission endpoints using an authenticated session cookie. */
class LeetCodeClient(
    private val sessionCookie: String,
    private val csrfToken: String,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMedia = "application/json".toMediaType()

    private val http = OkHttpClient.Builder().build()

    private fun cookieHeader() = "LEETCODE_SESSION=$sessionCookie; csrftoken=$csrfToken"

    private fun request(url: String, body: String? = null, method: String = "GET"): Request.Builder {
        val builder = Request.Builder()
            .url(url)
            .addHeader("Cookie", cookieHeader())
            .addHeader("x-csrftoken", csrfToken)
            .addHeader("Referer", "https://leetcode.com")
            .addHeader("User-Agent", "Mozilla/5.0 (leetcode-automation-android)")
        return if (body != null) {
            builder.method(method, body.toRequestBody(jsonMedia))
        } else {
            builder.method(method, null)
        }
    }

    suspend fun fetchProblem(titleSlug: String, langSlug: String = "python3"): Problem = withContext(Dispatchers.IO) {
        val query = """
            query questionData(${'$'}titleSlug: String!) {
              question(titleSlug: ${'$'}titleSlug) {
                questionId
                title
                titleSlug
                difficulty
                content
                codeSnippets { langSlug code }
              }
            }
        """.trimIndent()

        val payload = json.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            kotlinx.serialization.json.buildJsonObject {
                put("query", query)
                putJsonObject("variables") { put("titleSlug", titleSlug) }
            },
        )

        val resp = http.newCall(
            request("https://leetcode.com/graphql", payload, "POST").build()
        ).execute()

        resp.use {
            if (!it.isSuccessful) throw LeetCodeException("Failed to fetch problem: HTTP ${it.code}")
            val body = it.body?.string().orEmpty()
            val root = json.parseToJsonElement(body).jsonObject
            val question = root["data"]?.jsonObject?.get("question")?.jsonObject
                ?: throw LeetCodeException("Problem not found: $titleSlug")

            var starterCode = ""
            val snippets = question["codeSnippets"] as? kotlinx.serialization.json.JsonArray
            snippets?.forEach { el ->
                val obj = el.jsonObject
                if (obj["langSlug"]?.jsonPrimitive?.contentOrNull == langSlug) {
                    starterCode = obj["code"]?.jsonPrimitive?.contentOrNull.orEmpty()
                }
            }

            Problem(
                questionId = question["questionId"]!!.jsonPrimitive.content,
                title = question["title"]!!.jsonPrimitive.content,
                titleSlug = question["titleSlug"]!!.jsonPrimitive.content,
                difficulty = question["difficulty"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                contentHtml = question["content"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                starterCode = starterCode,
            )
        }
    }

    /** Slug of today's LeetCode "Daily Coding Challenge" — the default target for streak runs. */
    suspend fun fetchDailyChallengeSlug(): String = withContext(Dispatchers.IO) {
        val query = """
            query questionOfToday {
              activeDailyCodingChallengeQuestion {
                question { titleSlug }
              }
            }
        """.trimIndent()

        val payload = json.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            kotlinx.serialization.json.buildJsonObject { put("query", query) },
        )

        val resp = http.newCall(
            request("https://leetcode.com/graphql", payload, "POST").build()
        ).execute()

        resp.use {
            if (!it.isSuccessful) throw LeetCodeException("Failed to fetch daily challenge: HTTP ${it.code}")
            val body = it.body?.string().orEmpty()
            val root = json.parseToJsonElement(body).jsonObject
            root["data"]?.jsonObject
                ?.get("activeDailyCodingChallengeQuestion")?.jsonObject
                ?.get("question")?.jsonObject
                ?.get("titleSlug")?.jsonPrimitive?.contentOrNull
                ?: throw LeetCodeException("No daily challenge found in response")
        }
    }

    suspend fun submitSolution(
        titleSlug: String,
        questionId: String,
        code: String,
        lang: String = "python3",
    ): SubmissionResult = withContext(Dispatchers.IO) {
        val payload = json.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            kotlinx.serialization.json.buildJsonObject {
                put("lang", lang)
                put("question_id", questionId)
                put("typed_code", code)
            },
        )

        val resp = http.newCall(
            request("https://leetcode.com/problems/$titleSlug/submit/", payload, "POST").build()
        ).execute()

        val submissionId = resp.use {
            if (!it.isSuccessful) throw LeetCodeException("Submit failed: HTTP ${it.code}")
            val body = it.body?.string().orEmpty()
            json.parseToJsonElement(body).jsonObject["submission_id"]?.jsonPrimitive?.content
                ?: throw LeetCodeException("No submission_id in response: $body")
        }

        pollSubmission(submissionId)
    }

    private suspend fun pollSubmission(submissionId: String): SubmissionResult {
        val checkUrl = "https://leetcode.com/submissions/detail/$submissionId/check/"
        val deadline = System.currentTimeMillis() + 60_000

        while (System.currentTimeMillis() < deadline) {
            val resp = withContext(Dispatchers.IO) {
                http.newCall(request(checkUrl).build()).execute()
            }
            val body = resp.use { it.body?.string().orEmpty() }
            val obj = json.parseToJsonElement(body).jsonObject

            if (obj["state"]?.jsonPrimitive?.contentOrNull == "SUCCESS") {
                val statusMsg = obj["status_msg"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
                return SubmissionResult(
                    accepted = statusMsg == "Accepted",
                    statusMsg = statusMsg,
                    lastTestcase = obj["last_testcase"]?.jsonPrimitive?.contentOrNull,
                    expectedOutput = obj["expected_output"]?.jsonPrimitive?.contentOrNull,
                    codeOutput = obj["code_output"]?.jsonPrimitive?.contentOrNull,
                    compileError = obj["compile_error"]?.jsonPrimitive?.contentOrNull,
                    runtimeError = obj["runtime_error"]?.jsonPrimitive?.contentOrNull,
                    totalCorrect = obj["total_correct"]?.jsonPrimitive?.intOrNull,
                    totalTestcases = obj["total_testcases"]?.jsonPrimitive?.intOrNull,
                )
            }
            delay(1500)
        }
        throw LeetCodeException("Timed out waiting for judge result")
    }
}
