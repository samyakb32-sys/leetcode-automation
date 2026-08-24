package com.leetcodeautomation.data

/**
 * AI providers the app can generate solutions with. NVIDIA is the free, zero-config default;
 * the rest let someone bring their own key for a provider they already use. OpenAI/Groq/Gemini
 * all speak the same OpenAI-compatible chat completions format NvidiaSolver already implements
 * (just a different base URL + key); Anthropic's Messages API has a different shape and gets
 * its own solver implementation.
 */
enum class AiProvider(
    val id: String,
    val label: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val keyHint: String,
) {
    NVIDIA(
        id = "nvidia",
        label = "NVIDIA (free)",
        defaultBaseUrl = "https://integrate.api.nvidia.com/v1/chat/completions",
        defaultModel = "meta/llama-3.3-70b-instruct",
        keyHint = "Free key from build.nvidia.com",
    ),
    OPENAI(
        id = "openai",
        label = "OpenAI",
        defaultBaseUrl = "https://api.openai.com/v1/chat/completions",
        defaultModel = "gpt-4o-mini",
        keyHint = "From platform.openai.com/api-keys",
    ),
    GROQ(
        id = "groq",
        label = "Groq",
        defaultBaseUrl = "https://api.groq.com/openai/v1/chat/completions",
        defaultModel = "llama-3.3-70b-versatile",
        keyHint = "From console.groq.com/keys",
    ),
    GEMINI(
        id = "gemini",
        label = "Google Gemini",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
        defaultModel = "gemini-2.0-flash",
        keyHint = "From aistudio.google.com/apikey",
    ),
    ANTHROPIC(
        id = "anthropic",
        label = "Anthropic",
        defaultBaseUrl = "https://api.anthropic.com/v1/messages",
        defaultModel = "claude-3-5-haiku-20241022",
        keyHint = "From console.anthropic.com",
    ),
    CUSTOM(
        id = "custom",
        label = "Other",
        defaultBaseUrl = "",
        defaultModel = "",
        keyHint = "Any OpenAI-compatible chat completions endpoint",
    );

    companion object {
        fun fromId(id: String): AiProvider = entries.find { it.id == id } ?: NVIDIA
    }
}
