package com.example.data

import com.example.model.BreathingExercise
import com.example.model.BreathingModuleType
import com.example.model.BreathingPattern
import com.example.model.BreathingStage
import com.example.model.NostrilSide
import com.example.model.StageType

object ExerciseCatalog {

    val allExercises: List<BreathingExercise> = listOf(
        BreathingExercise(
            id = "three_stage_clock",
            moduleType = BreathingModuleType.THREE_STAGE,
            title = "Three-Stage Breathing Clock",
            subtitle = "Inhale • Hold • Exhale",
            focusTag = "Calm & Rhythm",
            difficultyTag = "Beginner Friendly",
            description = "A foundational rhythmic breath cycle with configurable inhale, gentle retention, and unhurried exhalation stages.",
            stepByStepGuide = listOf(
                "Sit comfortably with your spine tall and shoulders relaxed away from your ears.",
                "Inhale smoothly through your nose as the breathing ring expands.",
                "Pause gently at the top of the breath without clenching your throat or chest.",
                "Exhale slowly and evenly as the circle contracts."
            ),
            benefits = listOf("Calms the nervous system", "Builds breath awareness", "Supports steady focus"),
            safetyNote = "Never strain or force breath retention. Reduce the hold duration or set it to 0s if you feel any tightness or dizziness.",
            accentHex = "#3E7B6D",
            defaultPattern = BreathingPattern(
                id = "default_three_stage",
                name = "Balanced 5-5-5",
                description = "Equal inhale, gentle hold, and slow exhale",
                moduleType = BreathingModuleType.THREE_STAGE,
                repetitions = 0,
                stages = listOf(
                    BreathingStage(
                        id = "ts_inhale",
                        label = "Inhale",
                        durationSeconds = 5,
                        type = StageType.INHALE,
                        colorHex = "#3E7B6D",
                        instruction = "Breathe in smoothly through your nose"
                    ),
                    BreathingStage(
                        id = "ts_hold",
                        label = "Hold",
                        durationSeconds = 5,
                        type = StageType.HOLD_IN,
                        colorHex = "#547AA5",
                        instruction = "Hold gently without tension",
                        isOptional = true,
                        isEnabled = true
                    ),
                    BreathingStage(
                        id = "ts_exhale",
                        label = "Exhale",
                        durationSeconds = 5,
                        type = StageType.EXHALE,
                        colorHex = "#8B6D9C",
                        instruction = "Release the breath slowly and completely"
                    )
                )
            )
        ),
        BreathingExercise(
            id = "anulom_vilom",
            moduleType = BreathingModuleType.ANULOM_VILOM,
            title = "Guided Anulom Vilom",
            subtitle = "Alternate Nostril Breathing",
            focusTag = "Balance & Clarity",
            difficultyTag = "Beginner Friendly",
            description = "Traditional Nadi Shodhana alternate-nostril breathing to harmonize left and right energy pathways and quiet mental chatter.",
            stepByStepGuide = listOf(
                "Sit comfortably with a straight, relaxed spine. Rest your left hand on your knee.",
                "Gently close your right nostril with your right thumb and inhale softly through your left nostril.",
                "Switch nostrils—close the left nostril with your ring finger and exhale smoothly through the right.",
                "Inhale through the right nostril, then switch and exhale through the left to complete one round."
            ),
            benefits = listOf("Balances focus and calm", "Slows breathing cadence", "Eases mental tension"),
            safetyNote = "Practice without breath retention by default. Keep airflow silent and effortless; stop immediately if you feel lightheaded or congested.",
            accentHex = "#4D6A6E",
            defaultPattern = BreathingPattern(
                id = "default_anulom_vilom",
                name = "Gentle Alternate Nostril",
                description = "Left-to-right and right-to-left cycle without retention by default",
                moduleType = BreathingModuleType.ANULOM_VILOM,
                repetitions = 6,
                stages = listOf(
                    BreathingStage(
                        id = "av_in_left",
                        label = "Inhale Left",
                        durationSeconds = 4,
                        type = StageType.INHALE,
                        colorHex = "#3E7B6D",
                        nostrilSide = NostrilSide.LEFT,
                        instruction = "Close right nostril, inhale gently through left"
                    ),
                    BreathingStage(
                        id = "av_hold_1",
                        label = "Hold Gently",
                        durationSeconds = 4,
                        type = StageType.HOLD_IN,
                        colorHex = "#547AA5",
                        nostrilSide = NostrilSide.BOTH,
                        instruction = "Optional gentle pause with both nostrils closed",
                        isOptional = true,
                        isEnabled = false
                    ),
                    BreathingStage(
                        id = "av_ex_right",
                        label = "Exhale Right",
                        durationSeconds = 4,
                        type = StageType.EXHALE,
                        colorHex = "#8B6D9C",
                        nostrilSide = NostrilSide.RIGHT,
                        instruction = "Switch nostrils, exhale smoothly through right"
                    ),
                    BreathingStage(
                        id = "av_in_right",
                        label = "Inhale Right",
                        durationSeconds = 4,
                        type = StageType.INHALE,
                        colorHex = "#3E7B6D",
                        nostrilSide = NostrilSide.RIGHT,
                        instruction = "Inhale calmly through the right nostril"
                    ),
                    BreathingStage(
                        id = "av_hold_2",
                        label = "Hold Gently",
                        durationSeconds = 4,
                        type = StageType.HOLD_IN,
                        colorHex = "#547AA5",
                        nostrilSide = NostrilSide.BOTH,
                        instruction = "Optional gentle pause with both nostrils closed",
                        isOptional = true,
                        isEnabled = false
                    ),
                    BreathingStage(
                        id = "av_ex_left",
                        label = "Exhale Left",
                        durationSeconds = 4,
                        type = StageType.EXHALE,
                        colorHex = "#8B6D9C",
                        nostrilSide = NostrilSide.LEFT,
                        instruction = "Switch nostrils, exhale smoothly through left"
                    )
                )
            )
        ),
        BreathingExercise(
            id = "bhramari",
            moduleType = BreathingModuleType.BHRAMARI,
            title = "Bhramari Pranayama",
            subtitle = "Humming Bee Breath",
            focusTag = "Deep Soothing",
            difficultyTag = "All Levels",
            description = "Gentle nasal inhalation followed by a soft, steady humming exhalation that creates soothing resonance in the head and chest.",
            stepByStepGuide = listOf(
                "Sit comfortably and soften your jaw, keeping your lips gently closed and teeth slightly apart.",
                "Optionally rest your index fingers lightly on the cartilage of your ears or keep hands relaxed in your lap.",
                "Inhale slowly through both nostrils.",
                "Exhale slowly while making a low, steady humming sound like a calm bee."
            ),
            benefits = listOf("Soothes agitation", "Encourages longer exhalations", "Quietens sensory overload"),
            safetyNote = "Keep the hum soft and effortless without straining your vocal cords. Pause and breathe naturally if you feel any discomfort.",
            accentHex = "#2E7D87",
            defaultPattern = BreathingPattern(
                id = "default_bhramari",
                name = "Humming Bee Calm",
                description = "Slow nasal inhale followed by resonant humming exhale",
                moduleType = BreathingModuleType.BHRAMARI,
                repetitions = 6,
                stages = listOf(
                    BreathingStage(
                        id = "bh_inhale",
                        label = "Inhale Slowly",
                        durationSeconds = 4,
                        type = StageType.INHALE,
                        colorHex = "#3E7B6D",
                        instruction = "Inhale calmly through your nose"
                    ),
                    BreathingStage(
                        id = "bh_hum",
                        label = "Humming Exhale",
                        durationSeconds = 8,
                        type = StageType.HUMMING,
                        colorHex = "#2E7D87",
                        instruction = "Exhale slowly with a gentle, steady hum"
                    )
                )
            )
        ),
        BreathingExercise(
            id = "kapalbhati",
            moduleType = BreathingModuleType.KAPALBHATI,
            title = "Kapalbhati Timer",
            subtitle = "Gentle Breath Cleanses & Rest",
            focusTag = "Mindful Energy",
            difficultyTag = "Guided Pace",
            description = "Structured rounds of gentle, active abdominal exhalations paired with passive inhalations and restorative rest intervals.",
            stepByStepGuide = listOf(
                "Sit upright with a comfortable posture and relax your belly.",
                "Take a natural breath in, then begin gentle, soft exhalations using a light inward draw of the lower abdomen.",
                "Allow each inhalation to happen automatically and passively as the belly relaxes.",
                "Rest quietly between rounds and observe the stillness of your breath."
            ),
            benefits = listOf("Encourages mindful core awareness", "Refreshes alertness", "Pairs activity with rest"),
            safetyNote = "Choose a comfortable, unhurried pace—never force rapid breathing. Avoid or seek qualified medical/yoga guidance if pregnant, recovering from surgery, or managing high blood pressure, dizziness, or respiratory conditions.",
            accentHex = "#C0734F",
            defaultPattern = BreathingPattern(
                id = "default_kapalbhati",
                name = "Gentle Morning Rounds",
                description = "Comfortable active exhalations followed by calm rest",
                moduleType = BreathingModuleType.KAPALBHATI,
                repetitions = 3,
                stages = listOf(
                    BreathingStage(
                        id = "kp_active",
                        label = "Active Exhalations",
                        durationSeconds = 30, // Computed from targetExhalations * paceMillis
                        type = StageType.KAPALBHATI_ACTIVE,
                        colorHex = "#C0734F",
                        instruction = "Gentle active exhalations, passive relaxed inhales"
                    ),
                    BreathingStage(
                        id = "kp_rest",
                        label = "Rest & Observe",
                        durationSeconds = 20,
                        type = StageType.REST,
                        colorHex = "#3E7B6D",
                        instruction = "Breathe naturally and let your body settle"
                    )
                )
            )
        ),
        BreathingExercise(
            id = "box_breathing",
            moduleType = BreathingModuleType.BOX_BREATHING,
            title = "Box Breathing (4-4-4-4)",
            subtitle = "Four-Stage Square Cadence",
            focusTag = "Focus & Poise",
            difficultyTag = "Beginner Friendly",
            description = "A balanced four-sided breathing pattern—Inhale, Hold, Exhale, Hold—used to cultivate steady composure and clear attention.",
            stepByStepGuide = listOf(
                "Trace the top edge of the square while inhaling steadily for 4 seconds.",
                "Follow the right edge while holding the breath softly.",
                "Trace the bottom edge while exhaling smoothly.",
                "Follow the left edge while resting gently before the next breath."
            ),
            benefits = listOf("Steadies heart-rate rhythm", "Improves composure under stress", "Easy to visualize"),
            safetyNote = "Keep holds light and comfortable. Shorten all four sides to 3 seconds if 4 seconds feels demanding.",
            accentHex = "#547AA5",
            defaultPattern = BreathingPattern(
                id = "default_box",
                name = "Classic Box 4-4-4-4",
                description = "Equal four-stage box cadence",
                moduleType = BreathingModuleType.BOX_BREATHING,
                repetitions = 0,
                stages = listOf(
                    BreathingStage(
                        id = "bx_in",
                        label = "Inhale",
                        durationSeconds = 4,
                        type = StageType.INHALE,
                        colorHex = "#3E7B6D",
                        instruction = "Inhale evenly along the top edge"
                    ),
                    BreathingStage(
                        id = "bx_hold_1",
                        label = "Hold",
                        durationSeconds = 4,
                        type = StageType.HOLD_IN,
                        colorHex = "#547AA5",
                        instruction = "Hold calmly along the right edge"
                    ),
                    BreathingStage(
                        id = "bx_ex",
                        label = "Exhale",
                        durationSeconds = 4,
                        type = StageType.EXHALE,
                        colorHex = "#8B6D9C",
                        instruction = "Exhale smoothly along the bottom edge"
                    ),
                    BreathingStage(
                        id = "bx_hold_2",
                        label = "Hold Empty",
                        durationSeconds = 4,
                        type = StageType.HOLD_OUT,
                        colorHex = "#B38867",
                        instruction = "Rest softly along the left edge"
                    )
                )
            )
        ),
        BreathingExercise(
            id = "custom_patterns",
            moduleType = BreathingModuleType.CUSTOM,
            title = "Custom Breathing Patterns",
            subtitle = "Build Your Own Cadence",
            focusTag = "Personalized",
            difficultyTag = "Flexible",
            description = "Design personalized multi-stage breathing sequences with custom names, durations, stage colors, repetitions, and audio cues.",
            stepByStepGuide = listOf(
                "Create a new breathing pattern or pick one from your library.",
                "Customize stages, durations, colors, and repetitions to match your comfort.",
                "Practice with the same calming timer, audio cues, and progress tracking."
            ),
            benefits = listOf("Tailored to your lung capacity", "Supports 4-7-8, Coherent 6-6, and more", "Saved locally for quick launch"),
            safetyNote = "Design ratios that feel sustainable and soothing. Avoid excessive breath holds.",
            accentHex = "#8B6D9C",
            defaultPattern = BreathingPattern(
                id = "sample_4_4_6_2",
                name = "Restorative 4-4-6-2",
                description = "Extended exhale pattern for evening wind-down",
                moduleType = BreathingModuleType.CUSTOM,
                repetitions = 6,
                isCustom = true,
                stages = listOf(
                    BreathingStage(
                        id = "c_in",
                        label = "Inhale",
                        durationSeconds = 4,
                        type = StageType.INHALE,
                        colorHex = "#3E7B6D",
                        instruction = "Inhale gently through your nose"
                    ),
                    BreathingStage(
                        id = "c_h1",
                        label = "Hold",
                        durationSeconds = 4,
                        type = StageType.HOLD_IN,
                        colorHex = "#547AA5",
                        instruction = "Softly hold your breath"
                    ),
                    BreathingStage(
                        id = "c_ex",
                        label = "Exhale",
                        durationSeconds = 6,
                        type = StageType.EXHALE,
                        colorHex = "#8B6D9C",
                        instruction = "Long, soothing exhalation"
                    ),
                    BreathingStage(
                        id = "c_h2",
                        label = "Pause",
                        durationSeconds = 2,
                        type = StageType.HOLD_OUT,
                        colorHex = "#B38867",
                        instruction = "Brief rest before next inhale"
                    )
                )
            )
        )
    )

    fun byModuleType(type: BreathingModuleType): BreathingExercise =
        allExercises.first { it.moduleType == type }
}
