package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.AppLanguage
import com.example.data.model.HomeworkAnalysis
import com.example.data.model.StoryStyle
import com.example.data.model.StudentProfile
import com.example.data.model.TeachingMethod
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "TeacherAiService"
private const val GENERAL_MODEL = "gemini-3.8-flash"

data class AdaptiveTeacherReply(
    val teacherReply: String,
    val methodUsed: TeachingMethod,
    val understood: Boolean,
    val usedHint: Boolean,
    val misconceptionDetected: String,
    val nextQuestion: String,
    val namedEncouragement: String
)

sealed interface AiConnectionStatus {
    data object Ready : AiConnectionStatus
    data object Busy : AiConnectionStatus
    data class Online(val modelName: String) : AiConnectionStatus
    data class Error(val message: String) : AiConnectionStatus
}

class TeacherAiService {

    private val conversationalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GENERAL_MODEL,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 600
            }
        )
    }

    private val adaptiveTeacherSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "teacherReply" to Schema.string(description = "Warm conversational response addressing the child by name, reacting to the meaning of their answer."),
                "methodUsed" to Schema.enumeration(
                    values = TeachingMethod.entries.map { it.name },
                    description = "Teaching approach selected—must switch to a new method if the child was confused."
                ),
                "understood" to Schema.boolean(description = "True if the child demonstrated understanding of the concept."),
                "usedHint" to Schema.boolean(description = "True if the teacher gave a hint before or during this turn."),
                "misconceptionDetected" to Schema.string(description = "Short phrase describing any misconception noticed, or empty string if none."),
                "nextQuestion" to Schema.string(description = "Exactly ONE gentle follow-up question, or empty string if celebrating mastery."),
                "namedEncouragement" to Schema.string(description = "Specific praise using the child's name about what they actually did.")
            )
        )
    }

    private val adaptiveModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GENERAL_MODEL,
            generationConfig = generationConfig {
                temperature = 0.6f
                responseMimeType = "application/json"
                responseSchema = adaptiveTeacherSchema
            }
        )
    }

    private val homeworkSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "recognizedQuestion" to Schema.string(description = "Exact text or math problem read from the homework photo."),
                "subjectDetected" to Schema.string(description = "School subject such as Mathematics, Science, English, Hindi, Odia, EVS, or Social Science."),
                "imageClarityOk" to Schema.boolean(description = "True if handwriting/print is clear enough to read reliably."),
                "clarityFeedback" to Schema.string(description = "Friendly note if the photo is blurry or dark, or confirmation that it is clear."),
                "firstStepExplanation" to Schema.string(description = "Explain ONLY the first gentle step without giving away the final answer."),
                "guidingQuestion" to Schema.string(description = "One warm question inviting the child to try the first step.")
            )
        )
    }

    private val homeworkModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GENERAL_MODEL,
            generationConfig = generationConfig {
                temperature = 0.3f
                responseMimeType = "application/json"
                responseSchema = homeworkSchema
            }
        )
    }

    fun streamTeacherReply(
        profile: StudentProfile,
        language: AppLanguage,
        storyStyle: StoryStyle,
        subject: String,
        topic: String,
        previousMethod: TeachingMethod,
        childMessage: String,
        conversationHistory: List<Pair<Boolean, String>>
    ): Flow<String> = flow {
        val systemBrief = buildTeacherSystemPrompt(
            profile = profile,
            language = language,
            storyStyle = storyStyle,
            subject = subject,
            topic = topic,
            previousMethod = previousMethod
        )
        val historyBlock = conversationHistory.takeLast(6).joinToString("\n") { (isTeacher, msg) ->
            if (isTeacher) "Meera Didi (AI Teacher): $msg" else "${profile.preferredName}: $msg"
        }
        val prompt = """
            $systemBrief

            Recent dialogue:
            $historyBlock

            Child (${profile.preferredName}) just said: "$childMessage"

            Reply warmly as Meera Didi (clearly an AI teacher, never robotic or menu-like).
            Respond to the meaning of their actual answer, accept mixed-language Indian phrasing, and ask AT MOST ONE clear question, then stop.
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            conversationalModel.generateContentStream(prompt).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    accumulated.append(part)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("AI teacher returned an empty response.")
        }
    }.catch { e ->
        if (e is CancellationException && e !is TimeoutCancellationException) throw e
        Log.e(TAG, "streamTeacherReply failed", e)
        throw e
    }

    suspend fun evaluateAdaptiveResponse(
        profile: StudentProfile,
        language: AppLanguage,
        subject: String,
        conceptTitle: String,
        questionAsked: String,
        childAnswer: String,
        previousMethod: TeachingMethod
    ): Result<AdaptiveTeacherReply> {
        return try {
            val prompt = """
                You are Meera Didi, a warm, expressive Indian AI teacher tutoring ${profile.preferredName} (Age ${profile.age}, ${profile.studentClass}, Board: ${profile.board}).
                Preferred teaching language: ${language.displayName} (${language.nativeLabel}).
                Subject: $subject | Concept: $conceptTitle
                Previous teaching method used: ${previousMethod.name}

                Question you asked: "$questionAsked"
                Child's actual answer: "$childAnswer"

                Instructions:
                1. Accept age-appropriate wording and mixed-language (English/Hindi/Odia) answers based on meaning.
                2. If the child is confused, says "I don't know", or answers incorrectly, DO NOT repeat the previous explanation louder—pick a DIFFERENT TeachingMethod (e.g., ACT_IT_OUT, DRAW_DIAGRAM, REAL_OBJECT, SHORTER_STORY, HOME_LANGUAGE, COMPARE_EXAMPLES, SOLVE_TOGETHER).
                3. Ask only ONE follow-up question in nextQuestion, and praise a specific achievement using ${profile.preferredName}'s name if understood.
                4. Write teacherReply, nextQuestion, and namedEncouragement in ${language.displayName} (use Devanagari for Hindi, Odia script for Odia, warm Indian English for English).
            """.trimIndent()

            val response = withTimeout(60_000L) {
                adaptiveModel.generateContent(prompt)
            }
            val jsonStr = response.text ?: throw IllegalStateException("Empty structured response")
            val json = JSONObject(jsonStr)
            Result.success(
                AdaptiveTeacherReply(
                    teacherReply = json.optString("teacherReply"),
                    methodUsed = TeachingMethod.fromRaw(json.optString("methodUsed")),
                    understood = json.optBoolean("understood", false),
                    usedHint = json.optBoolean("usedHint", false),
                    misconceptionDetected = json.optString("misconceptionDetected"),
                    nextQuestion = json.optString("nextQuestion"),
                    namedEncouragement = json.optString("namedEncouragement")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "evaluateAdaptiveResponse timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "evaluateAdaptiveResponse failed", e)
            Result.failure(e)
        }
    }

    suspend fun analyzeHomeworkPhoto(
        bitmap: Bitmap,
        profile: StudentProfile,
        language: AppLanguage,
        correctedQuestionHint: String? = null
    ): Result<HomeworkAnalysis> {
        return try {
            val scaled = scaleBitmapForUpload(bitmap)
            val promptText = if (correctedQuestionHint.isNullOrBlank()) {
                """
                    You are Meera Didi, a kind Indian AI teacher helping ${profile.preferredName} (${profile.studentClass}) with school homework.
                    Examine the uploaded notebook/textbook photo carefully.
                    1. Transcribe the exact question you see into recognizedQuestion.
                    2. Identify the school subject.
                    3. If the photo is blurry, cropped, or too dark to read accurately, set imageClarityOk = false and ask politely for a clearer photo.
                    4. Otherwise explain ONLY the FIRST step in ${language.displayName} (${language.nativeLabel}), then ask one guidingQuestion so the child tries before receiving the next hint.
                """.trimIndent()
            } else {
                """
                    You are Meera Didi helping ${profile.preferredName} (${profile.studentClass}) with this corrected homework question:
                    "$correctedQuestionHint"
                    Identify the subject, explain ONLY the first gentle step in ${language.displayName} (${language.nativeLabel}) without giving away the final answer, and ask one guidingQuestion so the child tries first.
                """.trimIndent()
            }

            val response = withTimeout(60_000L) {
                homeworkModel.generateContent(
                    content {
                        if (correctedQuestionHint.isNullOrBlank()) {
                            image(scaled)
                        }
                        text(promptText)
                    }
                )
            }
            val jsonStr = response.text ?: throw IllegalStateException("Empty homework response")
            val json = JSONObject(jsonStr)
            Result.success(
                HomeworkAnalysis(
                    recognizedQuestion = correctedQuestionHint?.takeIf { it.isNotBlank() }
                        ?: json.optString("recognizedQuestion"),
                    subjectDetected = json.optString("subjectDetected", "Mathematics"),
                    imageClarityOk = if (!correctedQuestionHint.isNullOrBlank()) true else json.optBoolean("imageClarityOk", true),
                    clarityFeedback = json.optString("clarityFeedback", "Clear photo!"),
                    firstStepExplanation = json.optString("firstStepExplanation"),
                    guidingQuestion = json.optString("guidingQuestion")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "analyzeHomeworkPhoto timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "analyzeHomeworkPhoto failed", e)
            Result.failure(e)
        }
    }

    private fun buildTeacherSystemPrompt(
        profile: StudentProfile,
        language: AppLanguage,
        storyStyle: StoryStyle,
        subject: String,
        topic: String,
        previousMethod: TeachingMethod
    ): String = """
        Role: You are Meera Didi, a warm, realistic Indian AI teacher sitting right beside ${profile.preferredName} (Age ${profile.age}, ${profile.studentClass}, School: ${profile.school.ifBlank { "Indian School" }}, Board: ${profile.board}).
        Transparency: You are clearly identified as an AI teacher, with a kind human teaching style—never a menu, newsreader, or robotic script.
        Language: Speak primarily in ${language.displayName} (${language.nativeLabel}), matching ${profile.preferredName}'s age and welcoming mixed-language Indian answers.
        Interests: ${profile.interests.joinToString(", ").ifBlank { "stories, nature, games" }}.
        Active Subject & Topic: $subject — $topic.
        Preferred Story Style: ${storyStyle.title} (${storyStyle.subtitle}).
        Previous Teaching Method: ${previousMethod.label}.
        Rules:
        - Teach ONE clear concept at a time.
        - If the child says "Why?", "I don't know", or is confused, switch to a different method (act it out, draw/imagine a model, compare two examples, use a shorter story, or bridge from their home language).
        - End with at most ONE clear question, then wait patiently.
    """.trimIndent()

    private fun scaleBitmapForUpload(bitmap: Bitmap): Bitmap {
        val maxDim = 1280
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        return Bitmap.createScaledBitmap(
            bitmap,
            (w * ratio).toInt().coerceAtLeast(1),
            (h * ratio).toInt().coerceAtLeast(1),
            true
        )
    }
}
