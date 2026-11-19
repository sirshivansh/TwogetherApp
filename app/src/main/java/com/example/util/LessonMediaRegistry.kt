package com.example.util

import androidx.annotation.RawRes
import com.example.R
import com.example.data.local.LessonEntity

/**
 * Central registry mapping every NCERT syllabus topic to its unique, bundled animated
 * explanatory MP4 packaged inside the APK (`res/raw/`), plus a teacher-style offline
 * voiceover script for Android TextToSpeech narration while watching.
 */
object LessonMediaRegistry {

    val allBundledRawNames: Set<String> = setOf(
        "lesson_c1_place_value",
        "lesson_c2_nouns",
        "lesson_c3_shapes",
        "lesson_c4_water_cycle",
        "lesson_fractions",
        "lesson_photosynthesis",
        "lesson_grammar",
        "lesson_c7_acids_bases",
        "lesson_c8_linear_equations",
        "lesson_c9_newtons_laws",
        "lesson_c10_pythagoras",
        "lesson_c10_constitution"
    )

    @RawRes
    fun resolveRawResId(rawResName: String): Int = when (rawResName) {
        "lesson_c1_place_value" -> R.raw.lesson_c1_place_value
        "lesson_c2_nouns" -> R.raw.lesson_c2_nouns
        "lesson_c3_shapes" -> R.raw.lesson_c3_shapes
        "lesson_c4_water_cycle" -> R.raw.lesson_c4_water_cycle
        "lesson_fractions" -> R.raw.lesson_fractions
        "lesson_photosynthesis" -> R.raw.lesson_photosynthesis
        "lesson_grammar" -> R.raw.lesson_grammar
        "lesson_c7_acids_bases" -> R.raw.lesson_c7_acids_bases
        "lesson_c8_linear_equations" -> R.raw.lesson_c8_linear_equations
        "lesson_c9_newtons_laws" -> R.raw.lesson_c9_newtons_laws
        "lesson_c10_pythagoras" -> R.raw.lesson_c10_pythagoras
        "lesson_c10_constitution" -> R.raw.lesson_c10_constitution
        else -> R.raw.lesson_fractions
    }

    fun isBundledCurriculumVideo(lesson: LessonEntity): Boolean =
        !lesson.id.startsWith("lesson_edu_") && allBundledRawNames.contains(lesson.rawResName)

    fun buildVoiceoverScript(lesson: LessonEntity, localizedTitle: String, localizedSummary: String): String {
        val cleanNotes = lesson.studyNotes
            .replace("**", "")
            .replace("###", "")
            .replace("•", ". ")
            .replace("\n", " ")
            .trim()
        return "$localizedTitle. $localizedSummary. $cleanNotes"
    }
}
