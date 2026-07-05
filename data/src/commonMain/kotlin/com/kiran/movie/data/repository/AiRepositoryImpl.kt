package com.kiran.movie.data.repository

import com.kiran.movie.domain.repository.AiRepository
import com.kiran.movie.domain.repository.AiSearchParameters
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: SystemInstruction? = null
)
@Serializable
private data class Content(val parts: List<Part>)
@Serializable
private data class SystemInstruction(val parts: List<Part>)
@Serializable
private data class Part(val text: String)

@Serializable
private data class GeminiResponse(val candidates: List<Candidate>? = null)
@Serializable
private data class Candidate(val content: Content)

/**
 * [geminiClient] must be a CLEAN HttpClient with no TMDB base URL or auth headers.
 * Use a separate Koin qualifier to inject it.
 */
class AiRepositoryImpl(
    private val geminiClient: HttpClient,
    private val apiKey: String
) : AiRepository {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun parseSearchQuery(query: String): AiSearchParameters {
        val prompt = """
            You are a movie recommendation assistant. Extract search parameters from the user query.
            Output ONLY a valid JSON object (no markdown, no explanation, no code fences):
            {
              "title": string_or_null,
              "genres": integer_array_or_null,
              "year": integer_or_null,
              "isTvShow": boolean,
              "aiResponse": string
            }
            Genre IDs: Action=28, Comedy=35, SciFi=878, Horror=27, Drama=18, Thriller=53, Romance=10749, Adventure=12, Animation=16, Fantasy=14, Mystery=9648, Crime=80, Documentary=99.
            title: only if user mentions a specific title, else null.
            genres: array of matching TMDB genre IDs, null if none mentioned.
            year: release year if mentioned, else null.
            isTvShow: true only if user explicitly asks for TV shows/series.
            aiResponse: a short friendly sentence like "Here are some great action movies!"
        """.trimIndent()

        return try {
            val requestBody = GeminiRequest(
                contents = listOf(Content(listOf(Part(text = query)))),
                systemInstruction = SystemInstruction(listOf(Part(text = prompt)))
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            println("GeminiAI: Sending query='$query'")

            val httpResponse = geminiClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            println("GeminiAI: Response status=${httpResponse.status}")

            val response: GeminiResponse = httpResponse.body()
            val rawText = response.candidates
                ?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "{}"

            println("GeminiAI: Raw text=$rawText")

            // Strip markdown code fences if Gemini wrapped the JSON
            val cleaned = rawText.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()

            json.decodeFromString<AiSearchParameters>(cleaned)
        } catch (e: Exception) {
            println("GeminiAI: Error - ${e.message}")
            e.printStackTrace()
            // Graceful fallback — treat the spoken words as a title search
            AiSearchParameters(
                title = query,
                aiResponse = "Showing results for \"$query\""
            )
        }
    }
}
