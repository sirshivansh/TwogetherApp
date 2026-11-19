package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.data.local.ChatMessageEntity
import com.example.data.local.HomeworkEntity
import com.example.data.local.LessonEntity
import com.example.data.local.QuizQuestionEntity
import com.example.util.OfflineContentTranslator
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.Locale

data class AiStreamUpdate(
    val text: String,
    val engineBadge: String,
    val isFinal: Boolean
)

data class OfflineKnowledgeSnapshot(
    val lessons: List<LessonEntity> = emptyList(),
    val questions: List<QuizQuestionEntity> = emptyList(),
    val homeworks: List<HomeworkEntity> = emptyList()
)

data class DocumentVerificationResult(
    val isValidDocument: Boolean,
    val documentType: String,
    val detectedName: String,
    val detectedBoard: String,
    val detectedPassStatus: String,
    val confidenceScore: Int,
    val decision: String, // "AI_VERIFIED", "NEEDS_REVIEW", "REJECTED"
    val summary: String,
    val modelUsed: String
)

/**
 * Single default online Gemini model configuration (no confusing model-picker pills in the UI).
 */
enum class GeminiOnlineMode(
    val modelId: String,
    val label: String,
    val shortBadge: String
) {
    BALANCED(
        modelId = "gemini-3.8-flash",
        label = "Online AI",
        shortBadge = "Study AI (Online)"
    ),
    FAST(
        modelId = "gemini-3.5-flash-lite",
        label = "Online AI",
        shortBadge = "Study AI (Online)"
    )
}

data class OfflineFaqItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String
)

object OfflineFaqCatalog {
    val faqs: List<OfflineFaqItem> = listOf(
        OfflineFaqItem(
            id = "faq_numerator",
            category = "Maths",
            question = "What is a Numerator and Denominator?",
            answer = "### Numerator & Denominator\n- **Numerator** = the **top number** in a fraction. It tells you **how many pieces** are taken or eaten.\n- **Denominator** = the **bottom number**. It tells you the **total equal slices** in one whole.\n\n**Example:** In `3/4` of a roti, **3** is the numerator and **4** is the denominator."
        ),
        OfflineFaqItem(
            id = "faq_fractions",
            category = "Maths",
            question = "How do I add fractions like 3/4 + 1/2?",
            answer = "### Adding Fractions Step-by-Step\n1. Make the bottom numbers (**denominators**) equal: `1/2 = 2/4`.\n2. Add only the top numbers (**numerators**): `3 + 2 = 5`.\n3. Final answer: **5/4** (1 whole roti and `1/4` left over)!"
        ),
        OfflineFaqItem(
            id = "faq_photosynthesis",
            category = "Science",
            question = "How does Photosynthesis work in green leaves?",
            answer = "### Photosynthesis (How Leaves Cook Food)\n- **Inputs:** Water (from roots) + Carbon Dioxide (from air) + **Sunlight**.\n- **Chlorophyll** is the green pigment that traps sunlight energy.\n- **Outputs:** **Glucose** (plant food) + fresh **Oxygen** for us to breathe!"
        ),
        OfflineFaqItem(
            id = "faq_water_cycle",
            category = "Science",
            question = "What are the 3 steps of the Water Cycle?",
            answer = "### The Water Cycle\n1. **Evaporation:** Sunlight warms river and pond water into invisible vapor.\n2. **Condensation:** Rising vapor cools high in the sky to form clouds.\n3. **Precipitation:** Heavy water droplets fall back to Earth as rain!"
        ),
        OfflineFaqItem(
            id = "faq_newton",
            category = "Science",
            question = "What are Newton's 3 Laws of Motion?",
            answer = "### Newton's Laws in Cricket\n1. **1st Law (Inertia):** A cricket ball stays still until a bat or hand pushes it.\n2. **2nd Law (`F = m × a`):** Hitting with more force makes the ball accelerate faster.\n3. **3rd Law (Action-Reaction):** When the bat pushes the ball forward, the ball pushes back equally on the bat."
        ),
        OfflineFaqItem(
            id = "faq_linear_eq",
            category = "Maths",
            question = "How do I solve 2x + 4 = 14?",
            answer = "### Balancing Linear Equations\nKeep both sides balanced like a market weighing scale:\n1. Subtract `4` from both sides: `2x = 10`.\n2. Divide both sides by `2`: **x = 5**!"
        ),
        OfflineFaqItem(
            id = "faq_grammar",
            category = "English",
            question = "What is the difference between a Noun and a Verb?",
            answer = "### Nouns vs. Verbs\n- **Noun:** A naming word for a person, place, animal, or thing (*Asha, mango, river, school*).\n- **Verb:** An action word showing what happens (*read, plant, solve, teach*)."
        ),
        OfflineFaqItem(
            id = "faq_constitution",
            category = "Civics",
            question = "What is Right to Education (Article 21A)?",
            answer = "### Right to Education (Article 21A)\n- Guarantees **free and compulsory education** for every child aged **6 to 14 years**.\n- Helps every learner rise together regardless of background."
        ),
        OfflineFaqItem(
            id = "faq_shapes",
            category = "Maths",
            question = "How many faces, edges, and vertices does a Cube have?",
            answer = "### 2D & 3D Shapes (NCERT Class 3)\n- **Cube (Dice):** **6 flat square faces**, **12 edges**, and **8 vertices (corners)**.\n- **Circle (2D):** Curved boundary with **0 straight sides and 0 corners**.\n- **Rolling vs. Sliding:** Curved surfaces roll (sphere/ball), flat faces slide (brick), and a cylinder (glass) does both!"
        ),
        OfflineFaqItem(
            id = "faq_acids_bases",
            category = "Science",
            question = "How do Litmus and Turmeric test Acids vs. Bases?",
            answer = "### Acids, Bases & Indicators (NCERT Class 7)\n- **Acids (sour — lemon, curd):** Turn **Blue litmus → RED** (*trick: A-B-R*).\n- **Bases (bitter/soapy — soap, baking soda):** Turn **Red litmus → BLUE** (*trick: B-R-B*).\n- **Turmeric (Haldi):** Turns **reddish-brown** in soap (base) and stays yellow in acid.\n- **Neutralization:** `Acid + Base → Salt + Water + Heat`."
        ),
        OfflineFaqItem(
            id = "faq_pythagoras",
            category = "Maths",
            question = "How do I use the Pythagoras Theorem (a² + b² = c²)?",
            answer = "### Pythagoras Theorem (NCERT Class 10)\n- Works in any **right-angled triangle (90°)** where `c` is the longest side (**hypotenuse**).\n- **Formula:** `a² + b² = c²` (e.g., `3² + 4² = 9 + 16 = 25 = 5²`).\n- **Ladder Example:** Wall `8 m`, base `6 m` → `c² = 64 + 36 = 100` → **Ladder = 10 m**!"
        ),
        OfflineFaqItem(
            id = "faq_offline_videos",
            category = "App Help",
            question = "How do I watch video lessons without internet?",
            answer = "### Offline Backpack Videos\n1. Open the **Backpack** tab.\n2. All **12 NCERT animated explanatory videos** (Classes 1–10) are pre-bundled directly inside the app package!\n3. Tap **Watch Video** on any unlocked topic to play crisp diagrams + audio 100% offline."
        ),
        OfflineFaqItem(
            id = "faq_doubt_outbox",
            category = "App Help",
            question = "What happens when I ask a doubt offline?",
            answer = "### Offline Doubt Outbox\n1. Type or speak your doubt anytime without internet.\n2. It is safely stored in your **Doubt Outbox**.\n3. As soon as you reconnect to Wi-Fi or mobile data, it automatically syncs to verified Twogether mentors!"
        )
    )
}

/**
 * Offline-First Study AI Service & Cost-Efficient Gemini Vision Document Verifier:
 * - Defaults strictly to Offline-First Study AI (searching Room DB Lessons, Study Notes,
 *   Quiz Questions, Homework, Glossary, Math Solver, and on-device Gemini Nano if present).
 * - Silently uses a single default Gemini model (`gemini-3.8-flash` -> `gemini-3.5-flash-lite`)
 *   ONLY when online and explicitly requested, falling back cleanly to Offline AI on any error.
 */
class HybridAiService {

    private val verificationSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "isValidDocument" to Schema.boolean(
                    description = "True if image clearly shows an educational marksheet, passing certificate, or teacher/college ID."
                ),
                "documentType" to Schema.string(
                    description = "Detected document type, e.g., 12th Marksheet, Passing Certificate, Teacher ID, or Invalid Image."
                ),
                "detectedName" to Schema.string(
                    description = "Full name visible on the document, or Unknown."
                ),
                "detectedBoard" to Schema.string(
                    description = "Board or institution name visible (e.g., CBSE, State Board, University), or Unknown."
                ),
                "detectedPassStatus" to Schema.string(
                    description = "Passed, Unclear, or Not Found."
                ),
                "confidenceScore" to Schema.integer(
                    description = "Confidence from 0 to 100."
                ),
                "decision" to Schema.enumeration(
                    values = listOf("AI_VERIFIED", "NEEDS_REVIEW", "REJECTED"),
                    description = "Verification outcome."
                ),
                "summary" to Schema.string(
                    description = "Short 1-sentence factual explanation."
                )
            )
        )
    }

    private fun buildSystemInstruction(gradeClass: Int, langTag: String): Content = content {
        val langName = OfflineContentTranslator.nativeLanguageLabel(langTag)
        text(
            "You are Twogether's warm, encouraging tutor for students in Classes 1 to 10 (currently helping a Class $gradeClass learner). " +
                "Respond in $langName when appropriate. Explain concepts step-by-step using simple everyday examples (rotis, cricket, trees, markets). " +
                "Format cleanly with short bullet points or numbered steps and bold key terms."
        )
    }

    private fun createChatModel(modelId: String, gradeClass: Int, langTag: String) = Firebase.ai.generativeModel(
        modelName = modelId,
        systemInstruction = buildSystemInstruction(gradeClass, langTag)
    )

    private fun createVisionVerifierModel(modelId: String) = Firebase.ai.generativeModel(
        modelName = modelId,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = verificationSchema
        }
    )

    suspend fun isGeminiNanoAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return try {
            val client = Generation.getClient()
            client.checkStatus() == FeatureStatus.AVAILABLE
        } catch (e: Exception) {
            Log.d(TAG, "Gemini Nano status check unavailable on this device: ${e.message}")
            false
        }
    }

    internal fun sanitizeChatHistory(history: List<ChatMessageEntity>): List<Content> {
        val cleaned = mutableListOf<Pair<String, String>>()
        for (msg in history.takeLast(12)) {
            val text = msg.content.trim()
            if (text.isBlank()) continue
            val role = if (msg.role == "user") "user" else "model"
            if (cleaned.isEmpty() && role == "model") continue
            if (cleaned.isNotEmpty() && cleaned.last().first == role) {
                cleaned[cleaned.lastIndex] = role to text
            } else {
                cleaned.add(role to text)
            }
        }
        if (cleaned.isNotEmpty() && cleaned.last().first == "user") {
            cleaned.removeAt(cleaned.lastIndex)
        }
        return cleaned.map { (role, text) ->
            content(role = role) { text(text) }
        }
    }

    fun askQuestionStream(
        question: String,
        gradeClass: Int,
        history: List<ChatMessageEntity>,
        isOnline: Boolean,
        offlineModeSelected: Boolean,
        onlineModelMode: GeminiOnlineMode = GeminiOnlineMode.BALANCED,
        knowledge: OfflineKnowledgeSnapshot = OfflineKnowledgeSnapshot()
    ): Flow<AiStreamUpdate> = flow {
        val cleanQuestion = question.trim()
        val langTag = OfflineContentTranslator.currentLanguageTag()

        if (cleanQuestion.isBlank()) {
            emit(
                AiStreamUpdate(
                    text = "Ask any Class $gradeClass **Maths**, **Science**, **English**, or **Homework** question!",
                    engineBadge = "Offline Study AI",
                    isFinal = true
                )
            )
            return@flow
        }

        val useOffline = offlineModeSelected || !isOnline
        if (useOffline) {
            // 1. Instant local curriculum + Room DB search (100% deterministic & fast)
            val groundedAnswer = OfflineSyllabusEngine.answer(
                rawQuery = cleanQuestion,
                gradeClass = gradeClass,
                knowledge = knowledge,
                langTag = langTag
            )
            if (groundedAnswer.foundSpecificMatch) {
                emit(
                    AiStreamUpdate(
                        text = groundedAnswer.markdown,
                        engineBadge = groundedAnswer.sourceBadge,
                        isFinal = true
                    )
                )
                return@flow
            }

            // 2. Try on-device Gemini Nano if supported on device
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val nanoClient = Generation.getClient()
                    if (nanoClient.checkStatus() == FeatureStatus.AVAILABLE) {
                        val prompt =
                            "You are a kind school tutor for Class $gradeClass. Answer clearly in 3-5 short bullet points: $cleanQuestion"
                        val sb = StringBuilder()
                        withTimeout(25_000L) {
                            nanoClient.generateContentStream(prompt).collect { chunk ->
                                val part = chunk.candidates.firstOrNull()?.text.orEmpty()
                                sb.append(part)
                                emit(
                                    AiStreamUpdate(
                                        text = sb.toString(),
                                        engineBadge = "Offline Study AI (On-Device)",
                                        isFinal = false
                                    )
                                )
                            }
                        }
                        if (sb.isNotBlank()) {
                            emit(
                                AiStreamUpdate(
                                    text = sb.toString(),
                                    engineBadge = "Offline Study AI (On-Device)",
                                    isFinal = true
                                )
                            )
                            return@flow
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Gemini Nano unavailable, using local study engine", e)
                }
            }

            emit(
                AiStreamUpdate(
                    text = groundedAnswer.markdown,
                    engineBadge = groundedAnswer.sourceBadge,
                    isFinal = true
                )
            )
            return@flow
        }

        // Online mode: single clean default Gemini model with silent Flash-Lite & Offline fallback
        val candidateModels = listOf(
            GeminiOnlineMode.BALANCED.modelId to "Study AI (Online)",
            GeminiOnlineMode.FAST.modelId to "Study AI (Online)"
        )
        val chatHistoryContent = sanitizeChatHistory(history)

        for ((modelId, badge) in candidateModels) {
            val accumulated = StringBuilder()
            try {
                val model = createChatModel(modelId = modelId, gradeClass = gradeClass, langTag = langTag)
                val chat = model.startChat(history = chatHistoryContent)
                withTimeout(45_000L) {
                    chat.sendMessageStream(cleanQuestion).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        accumulated.append(piece)
                        emit(
                            AiStreamUpdate(
                                text = accumulated.toString(),
                                engineBadge = badge,
                                isFinal = false
                            )
                        )
                    }
                }
                if (accumulated.isNotBlank()) {
                    emit(
                        AiStreamUpdate(
                            text = accumulated.toString(),
                            engineBadge = badge,
                            isFinal = true
                        )
                    )
                    return@flow
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Online model $modelId unavailable: ${e.message}")
            }
        }

        // Never break: gracefully fall back to Offline Study AI if network/API is unreachable
        val fallback = OfflineSyllabusEngine.answer(
            rawQuery = cleanQuestion,
            gradeClass = gradeClass,
            knowledge = knowledge,
            langTag = langTag
        )
        emit(
            AiStreamUpdate(
                text = fallback.markdown,
                engineBadge = "Offline Study AI (Auto-Fallback)",
                isFinal = true
            )
        )
    }

    suspend fun verifyEducatorDocument(
        context: Context,
        imageUri: Uri,
        expectedName: String,
        expectedBoard: String
    ): Result<DocumentVerificationResult> {
        val bitmap = loadScaledBitmap(context, imageUri)
            ?: return Result.failure(IllegalArgumentException("Could not read the selected document image."))

        val prompt = """
            Analyze this uploaded document image for Twogether Volunteer Educator verification.
            Applicant claimed name: "${expectedName.ifBlank { "Not specified" }}"
            Applicant claimed 12th board/institution: "${expectedBoard.ifBlank { "Not specified" }}"

            Evaluate carefully:
            1. Is this genuinely an academic certificate, 12th marksheet, degree, or official teacher/college ID (NOT a random object, blank page, or selfie)?
            2. Extract visible name, board/institution, and PASS status if readable.
            3. Use decision="AI_VERIFIED" if it is clearly a valid educational/ID document (confidence >= 70) and does not contradict applicant details; "NEEDS_REVIEW" if blurry/partial or name differs; "REJECTED" if clearly not an academic/ID document.
        """.trimIndent()

        val visionModels = listOf("gemini-3.5-flash-lite", "gemini-3.8-flash")
        var lastError: Exception? = null

        for (modelId in visionModels) {
            try {
                val model = createVisionVerifierModel(modelId)
                val response = withTimeout(60_000L) {
                    model.generateContent(
                        content {
                            image(bitmap)
                            text(prompt)
                        }
                    )
                }
                val jsonString = response.text
                    ?: throw IllegalStateException("Vision model returned empty output.")
                val json = JSONObject(jsonString)
                val isValid = json.optBoolean("isValidDocument", false)
                val docType = json.optString("documentType", "Document").take(60)
                val detectedName = json.optString("detectedName", "Unknown").take(80)
                val detectedBoard = json.optString("detectedBoard", "Unknown").take(80)
                val passStatus = json.optString("detectedPassStatus", "Unclear").take(30)
                val confidence = json.optInt("confidenceScore", 50).coerceIn(0, 100)
                val rawDecision = json.optString("decision", "NEEDS_REVIEW")
                val decision = when (rawDecision) {
                    "AI_VERIFIED", "NEEDS_REVIEW", "REJECTED" -> rawDecision
                    else -> if (isValid && confidence >= 70) "AI_VERIFIED" else "NEEDS_REVIEW"
                }
                val summary = json.optString("summary", "Analyzed by $modelId.").take(350)

                return Result.success(
                    DocumentVerificationResult(
                        isValidDocument = isValid,
                        documentType = docType,
                        detectedName = detectedName,
                        detectedBoard = detectedBoard,
                        detectedPassStatus = passStatus,
                        confidenceScore = confidence,
                        decision = decision,
                        summary = summary,
                        modelUsed = modelId
                    )
                )
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "Document vision verification timed out on $modelId", e)
                lastError = IllegalStateException("Document verification timed out after 60s.", e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Document vision verification failed on $modelId: ${e.message}", e)
                lastError = e
            }
        }

        val err = lastError ?: IllegalStateException("Unable to verify document right now.")
        Log.e(TAG, "Educator document verification failed across all models", err)
        return Result.failure(err)
    }

    private suspend fun loadScaledBitmap(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            val maxEdge = maxOf(boundsOpts.outWidth, boundsOpts.outHeight).coerceAtLeast(1)
            var sampleSize = 1
            while (maxEdge / sampleSize > 1024) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode document image Uri", e)
            null
        }
    }

    companion object {
        private const val TAG = "HybridAiService"
    }
}

data class OfflineAiResult(
    val markdown: String,
    val sourceBadge: String,
    val foundSpecificMatch: Boolean
)

/**
 * Full-text Tokenized Offline Study AI Engine for Classes 1-10.
 * Searches Room DB Lessons (Titles, Summaries, Study Notes), Quiz Questions & Explanations,
 * Homework Assignments, Offline FAQs, Built-in Glossary, and deterministic Math Solvers.
 */
object OfflineSyllabusEngine {

    private val stopWords = setOf(
        "what", "is", "are", "was", "were", "the", "a", "an", "of", "in", "on", "to", "for",
        "me", "my", "please", "explain", "define", "tell", "about", "how", "do", "does",
        "did", "can", "you", "mean", "means", "meaning", "called", "why", "which", "who",
        "with", "from", "and", "or", "by", "this", "that", "it", "be", "help"
    )

    private data class GlossaryEntry(
        val keywords: List<String>,
        val title: String,
        val markdown: String
    )

    private val curriculumGlossary = listOf(
        GlossaryEntry(
            keywords = listOf("numerator", "numerators", "top number"),
            title = "Numerator",
            markdown = "### Numerator (Fractions)\n- The **Numerator** is the **top number** in a fraction.\n- It shows **how many equal parts** are taken, shaded, or eaten.\n- **Example:** In `3/4` of a roti, **3** is the numerator (3 pieces eaten out of 4)."
        ),
        GlossaryEntry(
            keywords = listOf("denominator", "denominators", "bottom number"),
            title = "Denominator",
            markdown = "### Denominator (Fractions)\n- The **Denominator** is the **bottom number** in a fraction.\n- It tells you the **total equal parts** one whole is divided into.\n- **Tip:** Always make denominators equal before adding or subtracting fractions!"
        ),
        GlossaryEntry(
            keywords = listOf("chlorophyll", "green pigment"),
            title = "Chlorophyll",
            markdown = "### Chlorophyll (Plant Science)\n- **Chlorophyll** is the natural green pigment inside plant leaves.\n- It traps **sunlight energy** so leaves can perform **photosynthesis** and cook food (glucose)."
        ),
        GlossaryEntry(
            keywords = listOf("evaporation", "vapor", "vapour"),
            title = "Evaporation",
            markdown = "### Evaporation (Water Cycle)\n- **Evaporation** happens when heat from the Sun turns liquid water from rivers, ponds, or wet clothes into invisible **water vapor** that rises into the air."
        ),
        GlossaryEntry(
            keywords = listOf("condensation", "clouds", "cloud"),
            title = "Condensation",
            markdown = "### Condensation (Water Cycle)\n- **Condensation** happens when warm water vapor cools high in the sky and turns back into tiny water droplets that form **clouds**."
        ),
        GlossaryEntry(
            keywords = listOf("inertia"),
            title = "Inertia",
            markdown = "### Inertia (Newton's 1st Law)\n- **Inertia** is the tendency of an object to stay at rest or keep moving in a straight line unless an outside force pushes or pulls it.\n- **Cricket Example:** A ball sitting on the pitch won't move until the bowler or bat applies force!"
        ),
        GlossaryEntry(
            keywords = listOf("photosynthesis", "glucose", "oxygen"),
            title = "Photosynthesis",
            markdown = "### Photosynthesis\n- Green leaves make food using **Sunlight + Water + Carbon Dioxide**.\n- **Chlorophyll** traps sunlight energy.\n- Output: **Glucose** (energy for the plant) + **Oxygen** (fresh air for us!)."
        ),
        GlossaryEntry(
            keywords = listOf("fraction", "fractions", "half", "quarter", "roti"),
            title = "Fractions",
            markdown = "### Understanding Fractions\n- A **fraction** shows equal parts of a whole (like slicing a roti).\n- **Top = Numerator** (pieces taken).\n- **Bottom = Denominator** (total equal slices).\n- Try typing a sum like `3/4 + 1/2` to see step-by-step solving!"
        ),
        GlossaryEntry(
            keywords = listOf("noun", "nouns", "naming word"),
            title = "Noun",
            markdown = "### Noun (Naming Word)\n- A **Noun** names a **person, place, animal, or thing**.\n- **Examples:** *Asha, village, goat, mango, school*."
        ),
        GlossaryEntry(
            keywords = listOf("verb", "verbs", "action word", "tense"),
            title = "Verb",
            markdown = "### Verb (Action Word)\n- A **Verb** shows an action or state of being.\n- **Action Verbs:** *plant, solve, run, teach, read*.\n- **Helping Verbs:** *is, are, was, will*."
        ),
        GlossaryEntry(
            keywords = listOf("algebra", "equation", "linear", "variable"),
            title = "Linear Equations",
            markdown = "### Solving Linear Equations\n- Treat an equation like a balanced market scale — do the **same operation on both sides**.\n- **Example:** `2x + 4 = 14`\n1. Subtract 4 from both sides: `2x = 10`\n2. Divide both sides by 2: **x = 5**!"
        ),
        GlossaryEntry(
            keywords = listOf("article 21a", "constitution", "fundamental", "rights", "duties"),
            title = "Article 21A & Rights",
            markdown = "### Constitution & Article 21A\n- **Article 21A** guarantees **free and compulsory education** for every child aged **6 to 14 years**.\n- Fundamental Duties encourage us to protect nature, respect everyone, and build kinder communities."
        )
    )

    internal fun tokenize(raw: String): List<String> {
        return raw.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9\\s/+\\-*=]"), " ")
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it !in stopWords }
            .distinct()
    }

    /**
     * Backward-compatible overload returning plain Markdown string.
     */
    fun answer(rawQuery: String, gradeClass: Int): String =
        answer(rawQuery, gradeClass, OfflineKnowledgeSnapshot()).markdown

    fun answer(
        rawQuery: String,
        gradeClass: Int,
        knowledge: OfflineKnowledgeSnapshot,
        langTag: String = "en"
    ): OfflineAiResult {
        val q = rawQuery.lowercase(Locale.US).trim()
        val tokens = tokenize(rawQuery)

        // 1. Exact FAQ match
        OfflineFaqCatalog.faqs.firstOrNull { it.question.equals(rawQuery.trim(), ignoreCase = true) }?.let { faq ->
            return OfflineAiResult(
                markdown = localizeIfNeeded(faq.answer, langTag),
                sourceBadge = "Offline FAQ • ${faq.category}",
                foundSpecificMatch = true
            )
        }

        // 2. Deterministic Fraction Addition Solver (e.g. 3/4 + 1/2)
        val fractionRegex = Regex("(\\d+)\\s*/\\s*(\\d+)\\s*\\+\\s*(\\d+)\\s*/\\s*(\\d+)")
        fractionRegex.find(q)?.let { m ->
            val n1 = m.groupValues[1].toIntOrNull() ?: 1
            val d1 = m.groupValues[2].toIntOrNull() ?: 1
            val n2 = m.groupValues[3].toIntOrNull() ?: 1
            val d2 = m.groupValues[4].toIntOrNull() ?: 1
            if (d1 != 0 && d2 != 0) {
                val commonDen = d1 * d2
                val num = n1 * d2 + n2 * d1
                val gcdVal = gcd(num, commonDen)
                val simpNum = num / gcdVal
                val simpDen = commonDen / gcdVal
                val md = "### Solving `$n1/$d1 + $n2/$d2`\n" +
                    "1. Find a common denominator: **$commonDen**.\n" +
                    "2. Convert & add numerators: `${n1 * d2}/$commonDen + ${n2 * d1}/$commonDen = $num/$commonDen`.\n" +
                    "3. Simplified Answer: **$simpNum/$simpDen**!"
                return OfflineAiResult(localizeIfNeeded(md, langTag), "Offline Math Solver", true)
            }
        }

        // 3. Deterministic Simple Linear Equation Solver (e.g. 2x + 4 = 14 or 3x - 6 = 9)
        val linearRegex = Regex("(\\d*)x\\s*([+-])\\s*(\\d+)\\s*=\\s*(\\d+)")
        linearRegex.find(q.replace(" ", ""))?.let { m ->
            val a = m.groupValues[1].ifBlank { "1" }.toIntOrNull() ?: 1
            val sign = m.groupValues[2]
            val b = m.groupValues[3].toIntOrNull() ?: 0
            val c = m.groupValues[4].toIntOrNull() ?: 0
            if (a != 0) {
                val rhs = if (sign == "+") c - b else c + b
                val xVal = if (rhs % a == 0) "${rhs / a}" else String.format(Locale.US, "%.2f", rhs.toDouble() / a)
                val opWord = if (sign == "+") "Subtract $b from" else "Add $b to"
                val md = "### Solving Linear Equation\n" +
                    "1. $opWord both sides: `${a}x = $rhs`.\n" +
                    "2. Divide both sides by `$a`: **x = $xVal**!"
                return OfflineAiResult(localizeIfNeeded(md, langTag), "Offline Math Solver", true)
            }
        }

        // 4. Deterministic Arithmetic Solver (e.g. 24 * 5, 81 / 9)
        val mathRegex = Regex("(\\d+)\\s*([+\\-*x/])\\s*(\\d+)")
        mathRegex.find(q)?.let { m ->
            val a = m.groupValues[1].toLongOrNull() ?: 0L
            val op = m.groupValues[2]
            val b = m.groupValues[3].toLongOrNull() ?: 0L
            val result = when (op) {
                "+" -> "${a + b}"
                "-" -> "${a - b}"
                "*", "x" -> "${a * b}"
                "/" -> if (b != 0L) String.format(Locale.US, "%.2f", a.toDouble() / b) else "undefined (cannot divide by 0)"
                else -> ""
            }
            if (result.isNotEmpty()) {
                val md = "### Step-by-Step Math Helper (Class $gradeClass)\n- Expression: `$a $op $b`\n- Result: **$result**"
                return OfflineAiResult(localizeIfNeeded(md, langTag), "Offline Math Solver", true)
            }
        }

        // 5. Search ALL Local Room DB Lessons (Titles, Summaries, Study Notes) + Quiz Questions + Homework
        val bestLesson = knowledge.lessons
            .map { lesson ->
                val haystack = "${lesson.title} ${lesson.subject} ${lesson.summary} ${lesson.studyNotes}".lowercase(Locale.US)
                val score = scoreTokens(tokens, q, haystack, titleText = lesson.title.lowercase(Locale.US))
                lesson to score
            }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }

        val bestQuiz = knowledge.questions
            .map { qq ->
                val haystack = "${qq.questionText} ${qq.explanation} ${qq.optionA} ${qq.optionB} ${qq.optionC} ${qq.optionD}".lowercase(Locale.US)
                val score = scoreTokens(tokens, q, haystack)
                qq to score
            }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }

        val bestHomework = knowledge.homeworks
            .map { hw ->
                val haystack = "${hw.title} ${hw.description} ${hw.subject}".lowercase(Locale.US)
                val score = scoreTokens(tokens, q, haystack, titleText = hw.title.lowercase(Locale.US))
                hw to score
            }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }

        val glossaryMatch = curriculumGlossary.firstOrNull { entry ->
            entry.keywords.any { kw -> q.contains(kw) || tokens.any { t -> t == kw || (t.length >= 4 && kw.contains(t)) } }
        }

        // Combine Glossary + Room Lesson citation when both match (e.g., "numerator" -> definition + Sharing Rotis lesson note!)
        if (glossaryMatch != null) {
            val sb = StringBuilder(glossaryMatch.markdown)
            if (bestLesson != null) {
                val l = bestLesson.first
                val title = OfflineContentTranslator.translateLessonTitle(l.id, l.title, langTag)
                val notes = OfflineContentTranslator.translateStudyNotes(l.id, l.studyNotes, langTag)
                sb.append("\n\n---\n### From Your Backpack Lesson\n")
                sb.append("**$title** (Class ${l.gradeClass} • ${OfflineContentTranslator.translateSubject(l.subject, langTag)})\n")
                if (notes.isNotBlank()) {
                    sb.append(notes)
                }
            }
            return OfflineAiResult(
                markdown = localizeIfNeeded(sb.toString(), langTag),
                sourceBadge = if (bestLesson != null) "Offline Notes • ${bestLesson.first.subject}" else "Offline Study Glossary",
                foundSpecificMatch = true
            )
        }

        if (bestLesson != null && bestLesson.second >= (bestQuiz?.second ?: 0) && bestLesson.second >= (bestHomework?.second ?: 0)) {
            val l = bestLesson.first
            val title = OfflineContentTranslator.translateLessonTitle(l.id, l.title, langTag)
            val summary = OfflineContentTranslator.translateLessonSummary(l.id, l.summary, langTag)
            val notes = OfflineContentTranslator.translateStudyNotes(l.id, l.studyNotes, langTag)
            val sb = StringBuilder()
            sb.append("### $title\n")
            sb.append("**Class ${l.gradeClass} • ${OfflineContentTranslator.translateSubject(l.subject, langTag)}**\n")
            if (summary.isNotBlank()) sb.append("$summary\n")
            if (notes.isNotBlank()) {
                sb.append("\n**Key Study Notes:**\n")
                sb.append(notes)
            }
            bestQuiz?.first?.takeIf { it.lessonId == l.id }?.let { qq ->
                sb.append("\n\n**Quick Practice Insight:**\n- ${qq.explanation}")
            }
            return OfflineAiResult(
                markdown = sb.toString(),
                sourceBadge = "Offline Backpack • Class ${l.gradeClass}",
                foundSpecificMatch = true
            )
        }

        if (bestQuiz != null && bestQuiz.second >= (bestHomework?.second ?: 0)) {
            val qq = bestQuiz.first
            val sourceLesson = knowledge.lessons.firstOrNull { it.id == qq.lessonId }
            val sb = StringBuilder()
            sb.append("### From Practice Quiz Bank\n")
            sb.append("**Question:** ${OfflineContentTranslator.translateQuizQuestion(qq.id, qq.questionText, langTag)}\n")
            sb.append("**Answer Explanation:** ${OfflineContentTranslator.translateQuizExplanation(qq.id, qq.explanation, langTag)}")
            if (sourceLesson != null) {
                sb.append("\n\n- Source Lesson: **${OfflineContentTranslator.translateLessonTitle(sourceLesson.id, sourceLesson.title, langTag)}**")
            }
            return OfflineAiResult(
                markdown = sb.toString(),
                sourceBadge = "Offline Quiz Notes",
                foundSpecificMatch = true
            )
        }

        if (bestHomework != null) {
            val hw = bestHomework.first
            val sb = StringBuilder()
            sb.append("### Related Homework Assignment\n")
            sb.append("**${OfflineContentTranslator.translateHomeworkTitle(hw.id, hw.title, langTag)}** (Class ${hw.gradeClass})\n")
            sb.append("${OfflineContentTranslator.translateHomeworkDescription(hw.id, hw.description, langTag)}\n")
            sb.append("- **Schedule:** ${hw.dueDateLabel} • Assigned by ${hw.assignedByName}")
            return OfflineAiResult(
                markdown = sb.toString(),
                sourceBadge = "Offline Homework Hub",
                foundSpecificMatch = true
            )
        }

        // 6. Fuzzy FAQ search (even single strong keyword >= 4 chars)
        val bestFaq = OfflineFaqCatalog.faqs
            .map { faq ->
                val haystack = "${faq.question} ${faq.answer} ${faq.category}".lowercase(Locale.US)
                faq to scoreTokens(tokens, q, haystack)
            }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }

        if (bestFaq != null) {
            return OfflineAiResult(
                markdown = localizeIfNeeded(bestFaq.first.answer, langTag),
                sourceBadge = "Offline FAQ • ${bestFaq.first.category}",
                foundSpecificMatch = true
            )
        }

        // 7. Helpful offline guidance fallback
        val fallbackMd = "### Offline Study Buddy (Class $gradeClass)\n" +
            "I searched your offline lessons, quizzes, and homework notes, and didn't find an exact match for **\"${rawQuery.trim()}\"** yet.\n" +
            "- Try asking about **Numerator**, **Denominator**, **Photosynthesis**, **Chlorophyll**, **Water Cycle**, **Newton's Laws**, **Nouns & Verbs**, or **Article 21A**.\n" +
            "- Or type a math problem like `3/4 + 1/2`, `2x + 4 = 14`, or `24 * 5`.\n" +
            "- You can also send this question to verified human mentors in the **Doubts** tab!"
        return OfflineAiResult(
            markdown = localizeIfNeeded(fallbackMd, langTag),
            sourceBadge = "Offline Study AI",
            foundSpecificMatch = false
        )
    }

    private fun scoreTokens(
        tokens: List<String>,
        fullQuery: String,
        haystack: String,
        titleText: String = ""
    ): Int {
        if (tokens.isEmpty()) return 0
        var score = 0
        val hayWords = haystack.split(Regex("[^a-z0-9]+")).filter { it.length >= 2 }.toSet()
        for (token in tokens) {
            when {
                token in hayWords -> score += if (token.length >= 5) 5 else 3
                token.length >= 4 && haystack.contains(token) -> score += 3
                token.length >= 5 && hayWords.any { it.startsWith(token.take(5)) || token.startsWith(it.take(5)) } -> score += 2
            }
            if (titleText.isNotEmpty() && titleText.contains(token)) {
                score += 2
            }
        }
        if (fullQuery.length >= 4 && haystack.contains(fullQuery)) {
            score += 6
        }
        return score
    }

    private fun localizeIfNeeded(markdown: String, langTag: String): String {
        if (langTag == "en") return markdown
        return OfflineContentTranslator.translateStudyText(markdown, langTag)
    }

    private fun gcd(a: Int, b: Int): Int {
        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)
        while (y != 0) {
            val t = x % y
            x = y
            y = t
        }
        return if (x == 0) 1 else x
    }
}
