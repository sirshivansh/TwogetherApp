package com.example.data.local

import com.example.util.PasswordHasher

object SeedData {
    // Pre-computed SHA-256 hashes for demo portal accounts (passwords are never rendered in app UI)
    private const val DEMO_ADMIN_HASH = "bc78e58d55cde1346e68f8e5fe588dedf62fa457aa646a500a53347faff6ee24"
    private const val DEMO_STUDENT_HASH = "3575dd3a8669c76c741131324f4bfa87e69e6af96e051751e1ea113da801f001"
    private const val DEMO_EDUCATOR_HASH = "e357bd7c9ea3ec149b7e2062e2d0eb2a072f45d93e47e782bb34f7578cbb5459"

    fun defaultPortalAccounts(uid: String): List<PortalAccountEntity> = listOf(
        PortalAccountEntity(
            email = "admin@twogether.org",
            ownerUid = uid,
            role = "ADMIN",
            passwordHash = DEMO_ADMIN_HASH,
            displayName = "NGO Coordinator (Admin)",
            linkedEntityId = "admin_root"
        ),
        PortalAccountEntity(
            email = "asha.student@twogether.org",
            ownerUid = uid,
            role = "STUDENT",
            passwordHash = DEMO_STUDENT_HASH,
            displayName = "Asha",
            linkedEntityId = "student_${uid}_asha"
        ),
        PortalAccountEntity(
            email = "priya.teacher@twogether.org",
            ownerUid = uid,
            role = "EDUCATOR",
            passwordHash = DEMO_EDUCATOR_HASH,
            displayName = "Priya Sharma",
            linkedEntityId = "edu_${uid}_priya"
        ),
        PortalAccountEntity(
            email = "ravi.student@twogether.org",
            ownerUid = uid,
            role = "STUDENT",
            passwordHash = PasswordHasher.sha256("Ravi@1234"),
            displayName = "Ravi",
            linkedEntityId = "student_${uid}_ravi"
        ),
        PortalAccountEntity(
            email = "meena.student@twogether.org",
            ownerUid = uid,
            role = "STUDENT",
            passwordHash = PasswordHasher.sha256("Meena@1234"),
            displayName = "Meena",
            linkedEntityId = "student_${uid}_meena"
        )
    )

    fun defaultProfilesForUser(uid: String): List<StudentProfileEntity> = listOf(
        StudentProfileEntity(
            id = "student_${uid}_asha",
            ownerUid = uid,
            name = "Asha",
            email = "asha.student@twogether.org",
            gradeClass = 5,
            avatarId = "lion",
            themeColorKey = "terracotta",
            preferredLanguage = "Bilingual",
            parentalConsentGiven = true,
            centerName = "Asha Kiran NGO Center",
            pinSequence = "0,1,2,3",
            xpPoints = 165,
            modulesCompleted = 2
        ),
        StudentProfileEntity(
            id = "student_${uid}_ravi",
            ownerUid = uid,
            name = "Ravi",
            email = "ravi.student@twogether.org",
            gradeClass = 8,
            avatarId = "astronaut",
            themeColorKey = "sage",
            preferredLanguage = "English",
            parentalConsentGiven = true,
            centerName = "Asha Kiran NGO Center",
            pinSequence = "1,1,2,2",
            xpPoints = 220,
            modulesCompleted = 3
        ),
        StudentProfileEntity(
            id = "student_${uid}_meena",
            ownerUid = uid,
            name = "Meena",
            email = "meena.student@twogether.org",
            gradeClass = 2,
            avatarId = "panda",
            themeColorKey = "marigold",
            preferredLanguage = "Hindi",
            parentalConsentGiven = true,
            centerName = "Gramin Community Hall",
            pinSequence = "0,0,1,1",
            xpPoints = 70,
            modulesCompleted = 1
        )
    )

    fun defaultEducatorsForUser(uid: String): List<EducatorProfileEntity> = listOf(
        EducatorProfileEntity(
            id = "edu_${uid}_priya",
            ownerUid = uid,
            fullName = "Priya Sharma",
            email = "priya.teacher@twogether.org",
            tier = "TIER_1_PAID",
            board12th = "CBSE",
            rollYear = "2022",
            stream = "Science",
            subjectSpecialty = "Mathematics",
            referenceContact = "Dr. Sunita Verma (Coordinator • +91 98100-44210)",
            documentAttached = true,
            docVerificationStatus = "AI_VERIFIED",
            docAiSummary = "CBSE Class XII Senior School Marksheet matches Priya Sharma (PASS • Science/Maths).",
            docConfidence = 96,
            docExtractedName = "Priya Sharma",
            docExtractedBoard = "CBSE",
            isBackupRoster = true,
            adminApproved = true,
            lessonsUploadedCount = 2,
            testScore = 3,
            contractSigned = true,
            trainingCompleted = true,
            onboardingStep = 5,
            hoursTaught = 14.5,
            doubtsResolved = 6,
            studentsMentored = 18
        )
    )

    fun defaultDoubtsForUser(uid: String): List<DoubtQueueEntity> = listOf(
        DoubtQueueEntity(
            id = "dbt_seed_${uid}_1",
            ownerUid = uid,
            studentProfileId = "student_${uid}_asha",
            studentName = "Asha",
            gradeClass = 5,
            subject = "Math",
            questionText = "How do I compare 3/4 and 5/8 without a calculator?",
            syncStatus = "pending",
            createdAtMillis = System.currentTimeMillis() - 900_000L
        ),
        DoubtQueueEntity(
            id = "dbt_seed_${uid}_2",
            ownerUid = uid,
            studentProfileId = "student_${uid}_asha",
            studentName = "Asha",
            gradeClass = 5,
            subject = "Science",
            questionText = "Why do green leaves turn yellow in the dark?",
            syncStatus = "answered",
            educatorReply = "Priya Didi: Leaves need sunlight to keep making green **chlorophyll**. In a dark room, chlorophyll breaks down so the leaf loses its green colour and turns yellow!",
            createdAtMillis = System.currentTimeMillis() - 7_200_000L
        )
    )

    /**
     * Complete NCERT/CBSE-aligned Class 1–10 Offline Syllabus.
     * Every lesson ships with its own unique animated explanatory MP4 bundled directly
     * inside the APK (`res/raw/`), structured study notes, worked examples, and 3 quizzes.
     */
    val initialLessons: List<LessonEntity> = listOf(
        LessonEntity(
            id = "lesson_c1_counting",
            title = "Place Value: Bundles of Tens & Ones",
            subject = "Math",
            gradeClass = 1,
            durationLabel = "Animated • NCERT Ch. 8",
            summary = "Group loose sticks and mangoes into bundles of 10 to read any 2-digit number fast.",
            studyNotes = """
                **NCERT Class 1 Math — Tens & Ones (Base-10 System)**
                • **Bundle Rule:** 10 loose ones always tie together to form **1 Ten**.
                • **Place Value House:** In a 2-digit number, the left digit shows **Tens (×10)** and the right digit shows **Ones (×1)**.
                • **Worked Examples:**
                  - `14` = 1 Ten + 4 Ones = `10 + 4`
                  - `46` = 4 Tens + 6 Ones = `40 + 6`
                • **Mental Addition Tip:** Keep the bigger number in your head (`4`) and count forward (`5, 6, 7`) so `4 + 3 = 7`.
                • **Number Line:** Moving 1 step right adds `+1` (after `14` comes `15`).
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_c1_place_value",
            downloaded = true,
            uploadedByEducatorEmail = "priya.teacher@twogether.org",
            uploadedByEducatorName = "Priya Sharma"
        ),
        LessonEntity(
            id = "lesson_c2_nouns",
            title = "Nouns: Common, Proper & Plurals",
            subject = "English",
            gradeClass = 2,
            durationLabel = "Animated • NCERT Grammar",
            summary = "Spot naming words for people, places, animals, and things, plus -s/-es plural rules.",
            studyNotes = """
                **NCERT Primary English — Nouns (Naming Words)**
                • **Definition:** A Noun is the name of a **person** (Asha, teacher), **place** (school, village), **animal** (goat, sparrow), or **thing** (mango, book).
                • **Common vs. Proper Nouns:**
                  - Common Noun = general name (`girl`, `river`, `city`)
                  - Proper Noun = special name that **always starts with a CAPITAL letter** (`Asha`, `Ganga`, `Pune`).
                • **Singular & Plural:** Add **-s** for most nouns (`book → books`). Add **-es** when a word ends in `s, sh, ch, x, o` (`bus → buses`, `mango → mangoes`).
                • **Bonus (Classes 4–5):** Collective nouns name a group (*a herd of cows*); Abstract nouns name feelings (*honesty, kindness*).
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_c2_nouns",
            downloaded = true,
            uploadedByEducatorName = "Twogether NCERT Team"
        ),
        LessonEntity(
            id = "lesson_c3_shapes",
            title = "2D & 3D Shapes: Sides, Corners & Faces",
            subject = "Math",
            gradeClass = 3,
            durationLabel = "Animated • NCERT Ch. 5",
            summary = "Compare flat 2D shapes with solid 3D objects by counting faces, edges, and vertices.",
            studyNotes = """
                **NCERT Class 3 Math — Shapes & Designs**
                • **2D (Flat) Shapes:**
                  - **Square:** 4 equal straight sides and 4 corners (carrom board).
                  - **Rectangle:** 4 straight sides with opposite sides equal and 4 corners (blackboard).
                  - **Triangle:** 3 straight sides and 3 corners (samosa).
                  - **Circle:** 1 curved boundary, **0 straight sides and 0 corners** (roti, wheel).
                • **3D (Solid) Shapes:** Have Length, Breadth, and Height:
                  - **Cube (Dice):** 6 flat square faces, 12 edges, 8 vertices (corners).
                  - **Cylinder (Steel glass):** 2 flat circular faces + 1 curved face (rolls AND slides).
                  - **Sphere (Cricket ball):** 0 flat faces, 1 curved surface (only rolls).
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_c3_shapes",
            downloaded = true,
            uploadedByEducatorEmail = "priya.teacher@twogether.org",
            uploadedByEducatorName = "Priya Sharma"
        ),
        LessonEntity(
            id = "lesson_c4_water",
            title = "The Water Cycle: Evaporation to Rain",
            subject = "Science",
            gradeClass = 4,
            durationLabel = "Animated • NCERT EVS",
            summary = "Trace nature's endless water loop: Evaporation, Transpiration, Condensation, and Precipitation.",
            studyNotes = """
                **NCERT Environmental Studies — The Water Cycle**
                • **1. Evaporation:** Heat from the sun warms water in rivers, lakes, and oceans, turning liquid water into invisible **water vapour**.
                • **Transpiration:** Green plants also release water vapour through tiny leaf pores (stomata).
                • **2. Condensation:** As warm vapour rises high into the cool sky, it cools around dust particles to form tiny water droplets that gather into **clouds** (same reason cold steel glasses get droplets outside!).
                • **3. Precipitation:** When cloud droplets merge and become heavy, gravity pulls them down as **rain, snow, or hail**.
                • **4. Collection & Recharge:** Rainwater flows back into rivers and seeps into the soil to refill **groundwater wells & handpumps**.
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_c4_water_cycle",
            downloaded = true,
            uploadedByEducatorName = "Twogether NCERT Team"
        ),
        LessonEntity(
            id = "lesson_c5_fractions",
            title = "Fractions: Parts, Equivalents & Adding",
            subject = "Math",
            gradeClass = 5,
            durationLabel = "Animated • NCERT Ch. 4",
            summary = "Master numerator, denominator, equivalent fractions, and comparing unlike fractions using LCM.",
            studyNotes = """
                **NCERT Class 5–6 Math — Fractions**
                • **Definition:** A fraction represents equal parts of a whole: `Numerator (Top) / Denominator (Bottom)`. All parts must be equal in size.
                • **Types:** Proper (`3/4`, top < bottom), Improper (`7/4`, top > bottom), Mixed (`1 3/4`).
                • **Equivalent Fractions:** Multiply or divide top and bottom by the same non-zero number: `1/2 = 2/4 = 4/8`.
                • **Adding Like Fractions:** Keep the denominator unchanged and add only numerators: `1/4 + 2/4 = 3/4` (never `3/8`!).
                • **Comparing Unlike Fractions (`3/4` vs `5/8`):** LCM of 4 and 8 is `8`. Convert `3/4 = (3×2)/(4×2) = 6/8`. Since `6/8 > 5/8`, **`3/4` is larger**.
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_fractions",
            downloaded = true,
            uploadedByEducatorEmail = "priya.teacher@twogether.org",
            uploadedByEducatorName = "Priya Sharma"
        ),
        LessonEntity(
            id = "lesson_c6_photosynthesis",
            title = "Photosynthesis: Nutrition in Green Plants",
            subject = "Science",
            gradeClass = 6,
            durationLabel = "Animated • NCERT Ch. 1",
            summary = "Discover chlorophyll, stomata, the balanced chemical equation, and why plants produce oxygen.",
            studyNotes = """
                **NCERT Science — Nutrition in Plants (Photosynthesis)**
                • **Word Meaning:** *Photo* = Light, *Synthesis* = Putting together. Green plants are **autotrophs** (self-feeders).
                • **Raw Materials:** Water & minerals (absorbed by roots), Carbon Dioxide `CO₂` (enters through **stomata** pores guarded by guard cells), and **Sunlight** (trapped by green **chlorophyll**).
                • **NCERT Equation:**
                  `6CO₂ + 6H₂O --(Sunlight + Chlorophyll)--> C₆H₁₂O₆ (Glucose) + 6O₂ (Oxygen)`
                • **Starch Test:** Extra glucose is stored as **starch**, which turns **blue-black** with iodine solution.
                • **Exam Fact:** Without sunlight in a dark room, chlorophyll breaks down and leaves turn yellow.
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_photosynthesis",
            downloaded = true,
            uploadedByEducatorName = "Twogether NCERT Team"
        ),
        LessonEntity(
            id = "lesson_c7_grammar",
            title = "Verbs, Tenses & Subject-Verb Concord",
            subject = "English",
            gradeClass = 7,
            durationLabel = "Animated • NCERT Grammar",
            summary = "Build error-free sentences using Action/Helping verbs, Past-Present-Future tenses, and modals.",
            studyNotes = """
                **NCERT Middle School English — Verbs & Tenses**
                • **Main vs. Helping Verbs:** Main verbs show action (`plant, solve, teach`); Helping/Auxiliary verbs set time & voice (`is, am, are, was, has, have, will`).
                • **Three Time Frames:**
                  - **Past:** `Rani planted a sapling yesterday.`
                  - **Present:** `Rani plants a sapling every Sunday.`
                  - **Future:** `Rani will plant a sapling tomorrow.`
                • **Continuous Form (`-ing`):** Use Helping Verb + `V-ing` for actions in progress (`is studying`, `was reading`).
                • **Subject-Verb Concord:** Singular subject (`He/She/Rani`) takes `-s/-es` in Simple Present (`Rani plants`, NOT `Rani plant`); Plural subjects (`They/Students`) take the base verb (`They plant`).
                • **Modals:** `can` (ability), `may` (polite permission), `must` (strong rule), `should` (advice).
            """.trimIndent(),
            language = "English",
            rawResName = "lesson_grammar",
            downloaded = true,
            uploadedByEducatorName = "Twogether NCERT Team"
        ),
        LessonEntity(
            id = "lesson_c7_acids_bases",
            title = "Acids, Bases & Natural Indicators",
            subject = "Science",
            gradeClass = 7,
            durationLabel = "Animated • NCERT Ch. 4",
            summary = "Test everyday substances with Litmus, Turmeric, and China Rose, and master Neutralization.",
            studyNotes = """
                **NCERT Class 7 Science — Acids, Bases & Salts**
                • **Acids (pH < 7):** Sour in taste (lemon juice = citric acid, curd = lactic acid, vinegar = acetic acid). Turn **Blue litmus → RED** (*Memory trick: A-B-R*).
                • **Bases (pH > 7):** Bitter & soapy to touch (baking soda, soap solution, lime water). Turn **Red litmus → BLUE** (*Memory trick: B-R-B*).
                • **Natural Indicators:**
                  - **Turmeric (Haldi):** Stays yellow in acid, turns **reddish-brown in base** (why curry stains turn red with soap!).
                  - **China Rose (Gudhal):** Turns **dark pink (magenta)** in acid and **green** in base.
                • **Neutralization:** `Acid + Base → Salt + Water + Heat` (e.g., `HCl + NaOH → NaCl + H₂O`).
                • **Daily Life Uses:** Antacid (Milk of Magnesia) calms stomach acidity; moist baking soda relieves acidic ant bites; farmers add quicklime to acidic soil.
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_c7_acids_bases",
            downloaded = true,
            uploadedByEducatorName = "Twogether NCERT Team"
        ),
        LessonEntity(
            id = "lesson_c8_algebra",
            title = "Linear Equations in One Variable",
            subject = "Math",
            gradeClass = 8,
            durationLabel = "Animated • NCERT Ch. 2",
            summary = "Solve equations using the balance scale rule and sign-flipping transposition method.",
            studyNotes = """
                **NCERT Class 8 Math — Linear Equations in One Variable**
                • **Definition:** An algebraic equation with an `=` sign where the highest power of the variable (`x`) is `1`. It has a Left-Hand Side (**LHS**) and Right-Hand Side (**RHS**).
                • **Balance Scale Rule:** Performing the exact same operation on both LHS and RHS keeps the equation balanced.
                • **Transposition Sign-Flip Rule:** Moving a term across `=` reverses its operation: `+ ↔ -` and `× ↔ ÷`.
                • **Worked NCERT Example (`2x + 4 = 14`):**
                  1. Transpose `+4` to RHS: `2x = 14 - 4` → `2x = 10`
                  2. Divide both sides by `2`: `x = 10 ÷ 2` → **`x = 5`**
                  3. **Verify:** `LHS = 2(5) + 4 = 14 = RHS ✓`
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_c8_linear_equations",
            downloaded = true,
            uploadedByEducatorEmail = "priya.teacher@twogether.org",
            uploadedByEducatorName = "Priya Sharma"
        ),
        LessonEntity(
            id = "lesson_c9_motion",
            title = "Newton's Three Laws of Motion",
            subject = "Science",
            gradeClass = 9,
            durationLabel = "Animated • NCERT Ch. 8",
            summary = "Understand Inertia, F = m × a, Momentum, and Action-Reaction using cricket and rockets.",
            studyNotes = """
                **NCERT Class 9 Science — Force and Laws of Motion**
                • **1st Law (Law of Inertia):** An object remains at rest or in uniform straight-line motion unless acted upon by an external unbalanced force. **Greater mass = greater inertia** (a stone resists motion more than a paper ball).
                • **2nd Law (`F = m × a`):** Rate of change of momentum (`p = m × v`) is proportional to applied unbalanced force. SI unit of force is **1 Newton (N) = 1 kg·m/s²**.
                • **Worked Numerical:** A `2 kg` ball accelerates at `5 m/s²` → `F = 2 × 5 = 10 N`.
                • **Cricket Catch Insight:** Pulling hands backward increases stopping time `t`, reducing impact force on palms!
                • **3rd Law (Action-Reaction):** Every action has an equal and opposite reaction acting on **two different bodies** (rocket exhaust pushes gas down; gas lifts rocket up).
            """.trimIndent(),
            language = "English",
            rawResName = "lesson_c9_newtons_laws",
            downloaded = true,
            uploadedByEducatorName = "Twogether NCERT Team"
        ),
        LessonEntity(
            id = "lesson_c10_pythagoras",
            title = "Pythagoras Theorem & Right Triangles",
            subject = "Math",
            gradeClass = 10,
            durationLabel = "Animated • NCERT Ch. 6",
            summary = "Prove a² + b² = c² visually, memorize board-exam triplets, and solve ladder & distance problems.",
            studyNotes = """
                **NCERT Class 10 Math — Triangles (Pythagoras Theorem)**
                • **Theorem:** In any **right-angled triangle (90°)**, the square of the hypotenuse (`c`, longest side opposite 90°) equals the sum of the squares of the other two sides: **`a² + b² = c²`**.
                • **Visual Proof:** Area of square on side `3` (`9`) + area of square on side `4` (`16`) = area of square on hypotenuse `5` (`25`).
                • **Worked NCERT Ladder Problem:** A ladder leans against an `8 m` wall with its foot `6 m` away. `c² = 8² + 6² = 64 + 36 = 100` → **`c = √100 = 10 m`**.
                • **Finding a Shorter Leg:** Subtract squares! If `c = 13` and `a = 5`, then `b² = 13² - 5² = 169 - 25 = 144` → `b = 12`.
                • **Famous Triplets:** `(3, 4, 5)`, `(6, 8, 10)`, `(5, 12, 13)`, `(8, 15, 17)`.
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_c10_pythagoras",
            downloaded = true,
            uploadedByEducatorEmail = "priya.teacher@twogether.org",
            uploadedByEducatorName = "Priya Sharma"
        ),
        LessonEntity(
            id = "lesson_c10_constitution",
            title = "Indian Constitution: Article 21A & Duties",
            subject = "Social Studies",
            gradeClass = 10,
            durationLabel = "Animated • NCERT Civics",
            summary = "Learn India's 3-pillar democracy, 6 Fundamental Rights, RTE Article 21A, and 11 Fundamental Duties.",
            studyNotes = """
                **NCERT Civics — The Indian Constitution, Rights & Duties**
                • **Adoption:** Adopted on **26 November 1949**; came into effect on **26 January 1950** (Republic Day).
                • **Separation of Powers:** Power is balanced across **Legislature** (makes laws), **Executive** (implements laws), and **Judiciary** (protects rights & interprets laws).
                • **Article 21A (Right to Education):** Added by the **86th Constitutional Amendment Act, 2002**, guaranteeing **free and compulsory education for all children aged 6 to 14 years** (implemented via the RTE Act, 2009).
                • **6 Fundamental Rights (Articles 14–32):** Legally enforceable in High Courts & Supreme Court (Equality, Freedom, Against Exploitation, Freedom of Religion, Cultural/Educational Rights, Constitutional Remedies).
                • **11 Fundamental Duties (Part IV-A, Article 51A):** Added by the **42nd Amendment (1976)** on Swaran Singh Committee recommendations; the 11th duty (2002) obligates parents/guardians to provide education opportunities for children aged 6–14.
            """.trimIndent(),
            language = "Bilingual",
            rawResName = "lesson_c10_constitution",
            downloaded = true,
            uploadedByEducatorName = "Twogether NCERT Team"
        )
    )

    val initialQuizzes: List<QuizQuestionEntity> = listOf(
        // Class 1 Math — Place Value
        QuizQuestionEntity(
            id = "q_c1_1",
            lessonId = "lesson_c1_counting",
            questionText = "How many tens and ones make the number 46?",
            optionA = "6 tens and 4 ones",
            optionB = "4 tens and 6 ones",
            optionC = "40 tens and 6 ones",
            optionD = "10 tens and 4 ones",
            correctIndex = 1,
            explanation = "In 46, the digit 4 is in the tens place (40) and 6 is in the ones place (6): 40 + 6 = 46."
        ),
        QuizQuestionEntity(
            id = "q_c1_2",
            lessonId = "lesson_c1_counting",
            questionText = "If you have 4 mangoes and a friend gives you 3 more, how many mangoes do you have?",
            optionA = "6 mangoes",
            optionB = "7 mangoes",
            optionC = "8 mangoes",
            optionD = "5 mangoes",
            correctIndex = 1,
            explanation = "Start at 4 and count forward 3 steps: 5, 6, 7. So 4 + 3 = 7 mangoes!"
        ),
        QuizQuestionEntity(
            id = "q_c1_3",
            lessonId = "lesson_c1_counting",
            questionText = "10 loose sticks tied together make how many Tens?",
            optionA = "1 Ten",
            optionB = "10 Tens",
            optionC = "2 Tens",
            optionD = "0 Tens",
            correctIndex = 0,
            explanation = "In the base-10 system, 10 ones always bundle into 1 Ten."
        ),

        // Class 2 English — Nouns
        QuizQuestionEntity(
            id = "q_c2_1",
            lessonId = "lesson_c2_nouns",
            questionText = "Which word in 'The happy goat jumped' is a Noun?",
            optionA = "happy",
            optionB = "jumped",
            optionC = "goat",
            optionD = "The",
            correctIndex = 2,
            explanation = "'Goat' is the name of an animal, so it is a Noun!"
        ),
        QuizQuestionEntity(
            id = "q_c2_2",
            lessonId = "lesson_c2_nouns",
            questionText = "Which of the following is a Proper Noun (always starts with a capital letter)?",
            optionA = "river",
            optionB = "village",
            optionC = "Ganga",
            optionD = "mango",
            correctIndex = 2,
            explanation = "'Ganga' is the special name of a specific river, so it is a Proper Noun."
        ),
        QuizQuestionEntity(
            id = "q_c2_3",
            lessonId = "lesson_c2_nouns",
            questionText = "What is the correct plural of 'mango'?",
            optionA = "mangos",
            optionB = "mangoes",
            optionC = "mangoies",
            optionD = "mango",
            correctIndex = 1,
            explanation = "Words like mango, box, and bus take '-es' in the plural: mango → mangoes."
        ),

        // Class 3 Math — 2D & 3D Shapes
        QuizQuestionEntity(
            id = "q_c3_1",
            lessonId = "lesson_c3_shapes",
            questionText = "How many straight sides and corners does a Circle have?",
            optionA = "4 sides, 4 corners",
            optionB = "1 side, 1 corner",
            optionC = "0 straight sides, 0 corners",
            optionD = "3 sides, 3 corners",
            correctIndex = 2,
            explanation = "A circle has one smooth curved boundary with 0 straight sides and 0 corners."
        ),
        QuizQuestionEntity(
            id = "q_c3_2",
            lessonId = "lesson_c3_shapes",
            questionText = "A Cube (like a dice) has how many flat square faces?",
            optionA = "4 faces",
            optionB = "6 faces",
            optionC = "8 faces",
            optionD = "12 faces",
            correctIndex = 1,
            explanation = "A cube has 6 flat square faces, 12 edges, and 8 vertices (corners)."
        ),
        QuizQuestionEntity(
            id = "q_c3_3",
            lessonId = "lesson_c3_shapes",
            questionText = "Which 3D solid shape can BOTH roll AND slide smoothly?",
            optionA = "Sphere (cricket ball)",
            optionB = "Cube (dice)",
            optionC = "Cylinder (steel glass)",
            optionD = "Cuboid (brick)",
            correctIndex = 2,
            explanation = "A cylinder has 1 curved face (so it rolls) and 2 flat circular faces (so it slides)!"
        ),

        // Class 4 Science — Water Cycle
        QuizQuestionEntity(
            id = "q_c4_1",
            lessonId = "lesson_c4_water",
            questionText = "When the sun warms river water and turns it into invisible vapour, it is called:",
            optionA = "Freezing",
            optionB = "Evaporation",
            optionC = "Condensation",
            optionD = "Precipitation",
            correctIndex = 1,
            explanation = "Heat from the sun turns liquid water into water vapour through Evaporation."
        ),
        QuizQuestionEntity(
            id = "q_c4_2",
            lessonId = "lesson_c4_water",
            questionText = "Why do tiny water droplets appear on the outside of an ice-cold steel glass?",
            optionA = "Water leaks through steel",
            optionB = "Evaporation from the glass",
            optionC = "Condensation of water vapour in warm air",
            optionD = "Transpiration",
            correctIndex = 2,
            explanation = "Water vapour in the air cools on the cold glass surface and condenses into liquid droplets."
        ),
        QuizQuestionEntity(
            id = "q_c4_3",
            lessonId = "lesson_c4_water",
            questionText = "Plants also add water vapour to the air through their leaves in a process called:",
            optionA = "Transpiration",
            optionB = "Precipitation",
            optionC = "Melting",
            optionD = "Filtration",
            correctIndex = 0,
            explanation = "Plants release water vapour through tiny leaf stomata during Transpiration."
        ),

        // Class 5 Math — Fractions
        QuizQuestionEntity(
            id = "q_c5_1",
            lessonId = "lesson_c5_fractions",
            questionText = "You cut 1 roti into 4 equal pieces and eat 1 piece. What fraction did you eat?",
            optionA = "1/2",
            optionB = "1/3",
            optionC = "1/4",
            optionD = "3/4",
            correctIndex = 2,
            explanation = "1 piece eaten (numerator) out of 4 equal pieces (denominator) is 1/4."
        ),
        QuizQuestionEntity(
            id = "q_c5_2",
            lessonId = "lesson_c5_fractions",
            questionText = "What is 1/4 + 2/4?",
            optionA = "3/4",
            optionB = "3/8",
            optionC = "1/2",
            optionD = "2/8",
            correctIndex = 0,
            explanation = "For like fractions, keep denominator 4 and add numerators: 1 + 2 = 3 → 3/4!"
        ),
        QuizQuestionEntity(
            id = "q_c5_3",
            lessonId = "lesson_c5_fractions",
            questionText = "Which fraction is larger: 3/4 or 5/8?",
            optionA = "5/8 is larger",
            optionB = "Both are equal",
            optionC = "3/4 is larger",
            optionD = "Cannot be compared",
            correctIndex = 2,
            explanation = "Convert 3/4 to eighths: (3×2)/(4×2) = 6/8. Since 6/8 > 5/8, 3/4 is larger!"
        ),

        // Class 6 Science — Photosynthesis
        QuizQuestionEntity(
            id = "q_c6_1",
            lessonId = "lesson_c6_photosynthesis",
            questionText = "Which green pigment inside leaves captures sunlight energy?",
            optionA = "Hemoglobin",
            optionB = "Chlorophyll",
            optionC = "Glucose",
            optionD = "Iodine",
            correctIndex = 1,
            explanation = "Chlorophyll gives leaves their green colour and absorbs sunlight energy."
        ),
        QuizQuestionEntity(
            id = "q_c6_2",
            lessonId = "lesson_c6_photosynthesis",
            questionText = "Through which tiny pores on the leaf surface does Carbon Dioxide (CO₂) enter?",
            optionA = "Roots",
            optionB = "Stomata",
            optionC = "Petals",
            optionD = "Stem bark",
            correctIndex = 1,
            explanation = "Stomata are microscopic pores surrounded by guard cells that take in CO₂ and release O₂."
        ),
        QuizQuestionEntity(
            id = "q_c6_3",
            lessonId = "lesson_c6_photosynthesis",
            questionText = "Which gas do green plants release during photosynthesis that humans breathe?",
            optionA = "Carbon dioxide",
            optionB = "Nitrogen",
            optionC = "Oxygen",
            optionD = "Argon",
            correctIndex = 2,
            explanation = "Photosynthesis converts CO₂ and water into Glucose and releases Oxygen (O₂)."
        ),

        // Class 7 English — Verbs & Tenses
        QuizQuestionEntity(
            id = "q_c7_1",
            lessonId = "lesson_c7_grammar",
            questionText = "Identify the action verb in: 'Rani plants a sapling every Sunday.'",
            optionA = "Rani",
            optionB = "sapling",
            optionC = "plants",
            optionD = "Sunday",
            correctIndex = 2,
            explanation = "'Plants' is the action verb showing what Rani does."
        ),
        QuizQuestionEntity(
            id = "q_c7_2",
            lessonId = "lesson_c7_grammar",
            questionText = "Choose the grammatically correct sentence (Subject-Verb Concord):",
            optionA = "He go to school every morning.",
            optionB = "He goes to school every morning.",
            optionC = "They goes to school every morning.",
            optionD = "Asha and Ravi is reading.",
            correctIndex = 1,
            explanation = "Singular third-person subjects (He/She/It) take '-s/-es' in Simple Present: 'He goes'."
        ),
        QuizQuestionEntity(
            id = "q_c7_3",
            lessonId = "lesson_c7_grammar",
            questionText = "Which tense is used in 'Ravi is solving a math problem right now'?",
            optionA = "Simple Past",
            optionB = "Simple Future",
            optionC = "Present Continuous",
            optionD = "Past Perfect",
            correctIndex = 2,
            explanation = "'is' (present helping verb) + 'solving' (V-ing) forms Present Continuous tense."
        ),

        // Class 7 Science — Acids, Bases & Indicators
        QuizQuestionEntity(
            id = "q_c7ab_1",
            lessonId = "lesson_c7_acids_bases",
            questionText = "What colour does Blue Litmus paper turn when dipped in lemon juice (acid)?",
            optionA = "Stays Blue",
            optionB = "Turns Red",
            optionC = "Turns Green",
            optionD = "Turns Yellow",
            correctIndex = 1,
            explanation = "Remember A-B-R: Acids turn Blue litmus paper RED!"
        ),
        QuizQuestionEntity(
            id = "q_c7ab_2",
            lessonId = "lesson_c7_acids_bases",
            questionText = "Why does a yellow turmeric curry stain turn reddish-brown when washed with soap?",
            optionA = "Soap is acidic",
            optionB = "Soap is basic, and turmeric turns red in a base",
            optionC = "Water reacts with cotton",
            optionD = "Turmeric evaporates",
            correctIndex = 1,
            explanation = "Soap solution is basic (alkaline), and natural turmeric indicator turns red in bases."
        ),
        QuizQuestionEntity(
            id = "q_c7ab_3",
            lessonId = "lesson_c7_acids_bases",
            questionText = "In a neutralization reaction, Acid + Base produces:",
            optionA = "Only Oxygen gas",
            optionB = "Salt + Water + Heat",
            optionC = "Pure Acid",
            optionD = "Starch + Glucose",
            correctIndex = 1,
            explanation = "Neutralization cancels acid and base to produce Salt, Water, and Heat."
        ),

        // Class 8 Math — Linear Equations
        QuizQuestionEntity(
            id = "q_c8_1",
            lessonId = "lesson_c8_algebra",
            questionText = "Solve for x: 2x + 4 = 14",
            optionA = "x = 4",
            optionB = "x = 5",
            optionC = "x = 6",
            optionD = "x = 9",
            correctIndex = 1,
            explanation = "Transpose +4: 2x = 14 - 4 = 10, then divide by 2: x = 5. Check: 2(5)+4 = 14 ✓"
        ),
        QuizQuestionEntity(
            id = "q_c8_2",
            lessonId = "lesson_c8_algebra",
            questionText = "When you transpose '-7' from the LHS to the RHS across '=', it becomes:",
            optionA = "-7",
            optionB = "+7",
            optionC = "÷7",
            optionD = "×7",
            correctIndex = 1,
            explanation = "In transposition, moving a term across '=' reverses its sign: -7 becomes +7."
        ),
        QuizQuestionEntity(
            id = "q_c8_3",
            lessonId = "lesson_c8_algebra",
            questionText = "Solve for y: 3y - 5 = 16",
            optionA = "y = 5",
            optionB = "y = 6",
            optionC = "y = 7",
            optionD = "y = 8",
            correctIndex = 2,
            explanation = "Add 5 to both sides: 3y = 21. Divide by 3: y = 7. Check: 3(7) - 5 = 16 ✓"
        ),

        // Class 9 Science — Newton's Laws
        QuizQuestionEntity(
            id = "q_c9_1",
            lessonId = "lesson_c9_motion",
            questionText = "According to Newton's Second Law, Force equals:",
            optionA = "Mass + Velocity",
            optionB = "Mass × Acceleration",
            optionC = "Distance ÷ Time",
            optionD = "Weight - Friction",
            correctIndex = 1,
            explanation = "Newton's Second Law gives the formula F = m × a (measured in Newtons)."
        ),
        QuizQuestionEntity(
            id = "q_c9_2",
            lessonId = "lesson_c9_motion",
            questionText = "A 2 kg cricket ball accelerates at 5 m/s². What is the force acting on it?",
            optionA = "7 N",
            optionB = "10 N",
            optionC = "2.5 N",
            optionD = "25 N",
            correctIndex = 1,
            explanation = "Using F = m × a: F = 2 kg × 5 m/s² = 10 Newtons (10 N)."
        ),
        QuizQuestionEntity(
            id = "q_c9_3",
            lessonId = "lesson_c9_motion",
            questionText = "Why does a cricket fielder pull their hands backward while catching a fast ball?",
            optionA = "To increase stopping time and reduce impact force",
            optionB = "To make the ball heavier",
            optionC = "To increase acceleration",
            optionD = "To stop gravity",
            correctIndex = 0,
            explanation = "Increasing the time to bring momentum to zero decreases the rate of change of momentum, lowering force on the hands."
        ),

        // Class 10 Math — Pythagoras Theorem
        QuizQuestionEntity(
            id = "q_c10p_1",
            lessonId = "lesson_c10_pythagoras",
            questionText = "A right-angled triangle has legs a = 3 cm and b = 4 cm. What is the hypotenuse c?",
            optionA = "5 cm",
            optionB = "6 cm",
            optionC = "7 cm",
            optionD = "12 cm",
            correctIndex = 0,
            explanation = "c² = 3² + 4² = 9 + 16 = 25, so c = √25 = 5 cm (the famous 3-4-5 triplet!)."
        ),
        QuizQuestionEntity(
            id = "q_c10p_2",
            lessonId = "lesson_c10_pythagoras",
            questionText = "A ladder reaches 8 m up a wall, and its foot is 6 m from the wall. How long is the ladder?",
            optionA = "9 m",
            optionB = "10 m",
            optionC = "12 m",
            optionD = "14 m",
            correctIndex = 1,
            explanation = "c² = 8² + 6² = 64 + 36 = 100, so ladder length c = √100 = 10 m."
        ),
        QuizQuestionEntity(
            id = "q_c10p_3",
            lessonId = "lesson_c10_pythagoras",
            questionText = "In a right triangle with hypotenuse c = 13 and one leg a = 5, find the other leg b:",
            optionA = "8",
            optionB = "10",
            optionC = "12",
            optionD = "14",
            correctIndex = 2,
            explanation = "Subtract squares for a shorter leg: b² = 13² - 5² = 169 - 25 = 144, so b = 12."
        ),

        // Class 10 Social Studies — Indian Constitution
        QuizQuestionEntity(
            id = "q_c10_1",
            lessonId = "lesson_c10_constitution",
            questionText = "The Right to Free and Compulsory Education (Article 21A) applies to children aged:",
            optionA = "3 to 8 years",
            optionB = "6 to 14 years",
            optionC = "10 to 18 years",
            optionD = "5 to 12 years",
            correctIndex = 1,
            explanation = "Article 21A (86th Amendment, 2002) guarantees free and compulsory education for ages 6 to 14."
        ),
        QuizQuestionEntity(
            id = "q_c10_2",
            lessonId = "lesson_c10_constitution",
            questionText = "Which Constitutional Amendment originally added Fundamental Duties (Article 51A)?",
            optionA = "1st Amendment (1951)",
            optionB = "42nd Amendment (1976)",
            optionC = "73rd Amendment (1992)",
            optionD = "101st Amendment (2016)",
            correctIndex = 1,
            explanation = "Fundamental Duties were added to Part IV-A (Article 51A) by the 42nd Amendment Act, 1976."
        ),
        QuizQuestionEntity(
            id = "q_c10_3",
            lessonId = "lesson_c10_constitution",
            questionText = "Which branch of India's Separation of Powers protects Fundamental Rights and resolves disputes?",
            optionA = "Judiciary (Courts)",
            optionB = "Legislature (Parliament)",
            optionC = "Executive (Ministers)",
            optionD = "Election Commission",
            correctIndex = 0,
            explanation = "The Judiciary (Supreme Court & High Courts) guards Fundamental Rights and interprets the Constitution."
        )
    )

    val initialNotifications: List<AppNotificationEntity> = listOf(
        AppNotificationEntity(
            id = "notif_seed_maths_hw",
            studentProfileId = "all",
            title = "Pending Homework Reminder",
            message = "Your Maths homework is still incomplete. Please complete it today!",
            category = "homework",
            targetTab = "backpack",
            isRead = false,
            createdAtMillis = System.currentTimeMillis()
        ),
        AppNotificationEntity(
            id = "notif_seed_science_quiz",
            studentProfileId = "all",
            title = "12 Animated NCERT Videos Ready Offline",
            message = "All Class 1–10 animated lessons are bundled in your Backpack — watch anytime without internet!",
            category = "tree",
            targetTab = "backpack",
            isRead = false,
            createdAtMillis = System.currentTimeMillis() - 1_800_000L
        ),
        AppNotificationEntity(
            id = "notif_seed_doubt_reply",
            studentProfileId = "all",
            title = "Mentor Replied to Science Doubt",
            message = "Priya Didi answered your photosynthesis leaf question.",
            category = "doubt",
            targetTab = "doubts",
            isRead = true,
            createdAtMillis = System.currentTimeMillis() - 3_600_000L
        )
    )

    val initialHomeworks: List<HomeworkEntity> = listOf(
        HomeworkEntity(
            id = "hw_math_5_fractions",
            title = "NCERT Fractions Sheet: Equivalent & Unlike Fractions",
            description = "1) Draw 3 circles showing 1/2 = 2/4 = 4/8. 2) Use LCM to compare 3/4 and 5/8. 3) Solve 1/4 + 2/4 and explain why the denominator stays 4.",
            subject = "Math",
            gradeClass = 5,
            dueDateLabel = "Due Today • 6:00 PM",
            assignedByName = "Priya Sharma",
            completedByProfileIds = "",
            syncStatus = "synced",
            createdAtMillis = System.currentTimeMillis() - 1_200_000L
        ),
        HomeworkEntity(
            id = "hw_sci_6_leaf",
            title = "Photosynthesis Leaf & Equation Diagram",
            description = "Sketch a green leaf labelling Sunlight, Chlorophyll, Stomata, Water (H₂O), CO₂, Glucose, and Oxygen (O₂). Write the balanced NCERT equation below it.",
            subject = "Science",
            gradeClass = 6,
            dueDateLabel = "Due Tomorrow",
            assignedByName = "Priya Sharma",
            completedByProfileIds = "",
            syncStatus = "synced",
            createdAtMillis = System.currentTimeMillis() - 2_400_000L
        ),
        HomeworkEntity(
            id = "hw_sci_7_turmeric",
            title = "Kitchen Indicator Lab: Haldi + Lemon vs. Soap",
            description = "Put 2 drops of turmeric water on white paper. Test 1 drop of lemon juice and 1 drop of soap water. Record both colour changes in your notebook!",
            subject = "Science",
            gradeClass = 7,
            dueDateLabel = "Due Thursday",
            assignedByName = "Twogether NCERT Team",
            completedByProfileIds = "",
            syncStatus = "synced",
            createdAtMillis = System.currentTimeMillis() - 3_000_000L
        ),
        HomeworkEntity(
            id = "hw_math_10_pythagoras",
            title = "Pythagoras Board Practice: 3 Real-World Triangles",
            description = "Solve: (a) Hypotenuse for legs 5 & 12, (b) Ladder length for 8 m wall & 6 m base, (c) Missing leg when c = 13 & a = 5.",
            subject = "Math",
            gradeClass = 10,
            dueDateLabel = "Due Saturday",
            assignedByName = "Priya Sharma",
            completedByProfileIds = "",
            syncStatus = "synced",
            createdAtMillis = System.currentTimeMillis() - 5_000_000L
        )
    )

    fun defaultQuizAttemptsForUser(uid: String): List<QuizAttemptEntity> = listOf(
        QuizAttemptEntity(
            id = "attempt_seed_${uid}_c1",
            studentProfileId = "student_${uid}_asha",
            lessonId = "lesson_c1_counting",
            lessonTitle = "Place Value: Bundles of Tens & Ones",
            subject = "Math",
            gradeClass = 1,
            score = 3,
            total = 3,
            passed = true,
            attemptedAtMillis = System.currentTimeMillis() - 86_400_000L
        )
    )
}
