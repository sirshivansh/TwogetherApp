<div align="center">

# Twogether
### *Learn Together, Earn Together, Rise Together*

**An offline-first, multilingual, voice-accessible Android learning ecosystem uniting Students, Educators, and NGO Coordinators — engineered to work reliably even in Airplane Mode.**

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Coroutines%20%26%20StateFlow-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Offline](https://img.shields.io/badge/Architecture-100%25%20Offline--First-4A8245?style=for-the-badge)
![Languages](https://img.shields.io/badge/Languages-23%20Indian%20Languages-C85A3F?style=for-the-badge)

</div>

---

## Why Twogether?

Millions of learners in rural and underserved communities share a single family or NGO smartphone with intermittent or zero internet access. **Twogether** is built from the ground up for **zero-connectivity resilience**:

* **100% Airplane Mode Ready**: 12 bundled NCERT video lessons (Classes 1–10), 36+ interactive practice quizzes, homework tracking, flashcards, an offline AI syllabus engine, and a local doubt outbox all work with zero internet.
* **Shared-Device Friendly**: Multiple children on one device switch workspaces using a visual **4-tap Color/Shape Avatar PIN**—removing literacy barriers while tracking parental consent and NGO centers.
* **Voice-First Accessibility**: Offline Android Text-to-Speech (TTS) and Voice Input are woven into nearly every screen, empowering first-time smartphone users and early readers.
* **Warm Claymorphic UI**: A tactile, low-glare **Warm Claymorphism** design system crafted in Jetpack Compose + Material 3, complete with alive micro-animations and student personalization themes.

---

## Key Features by Portal

### 1. Student Portal (4 Offline-First Tabs)

| Tab | Capabilities |
| :--- | :--- |
| **My Backpack** | • **12 Bundled MP4 Lessons** (`res/raw/`, H.264 + AAC) covering NCERT Classes 1–10 with hardware-accelerated `TextureView` playback, offline TTS narration, and Markdown study notes.<br>• **Sequential Mastery Progression**: Lesson $N+1$ unlocks only after watching the video **and** passing the quiz ($\ge 50\%$). Educator-uploaded videos unlock immediately.<br>• **Offline Homework & 36+ NCERT Quizzes**: Instant step-by-step explanations, XP rewards, score history, and 1–5 star lesson ratings.<br>• **Claymorphic Notification Center**: Local reminders, educator replies, and admin broadcasts with one-tap TTS. |
| **Study AI** | • **3-Tier Fail-Safe Hybrid AI**: Automatically cascades from **Cloud Gemini** (streaming) $\rightarrow$ **On-Device Gemini Nano** $\rightarrow$ **100% Offline Syllabus Engine** so the tutor never crashes or stalls.<br>• Voice question input, Markdown rendering, quick FAQ chips, one-tap Listen (TTS), and persistent per-student chat history in Room. |
| **Doubts & Live** | • **Offline Doubt Outbox**: Type or speak doubts, attach photos via zero-permission Android Photo Picker (`PickVisualMedia`), and queue offline with a built-in *"Simulate Offline Mode"* switch for demos.<br>• Automatic background sync via `NetworkMonitor` + `WorkManager` (`DoubtSyncWorker`).<br>• **Live Classrooms**: Real-time Firestore sessions when signed in online, with graceful offline guards. |
| **My Tree & Toolkit** | • **Growing Knowledge Tree**: Animated tree evolving across 5 stages (*Seedling* $\rightarrow$ *Sprout* $\rightarrow$ *Sapling* $\rightarrow$ *Flowering Tree* $\rightarrow$ *Golden Fruit Banyan*) from $0$ to $400+$ XP.<br>• **2x2 Offline Study Toolkit**:<br>&nbsp;&nbsp;1. **3D-Flip NCERT Flashcards** (15 curated cards with memory tips & TTS)<br>&nbsp;&nbsp;2. **Daily 5-Question Brain Sprint** (rapid-fire quiz with streak bonus XP)<br>&nbsp;&nbsp;3. **Chalkboard Scratchpad** (Canvas drawing, 4 chalk colors, math grid, Pocket Formula Strip: $a^2 + b^2 = c^2$, $F = m \times a$, etc.)<br>&nbsp;&nbsp;4. **Progress Report Card Export** via Android Share Sheet.<br>• **Subject Mastery Analytics** & **Pomodoro Focus Timer** (10m / 15m / 25m). |

---

### 2. Educator Portal

* **5-Step Structured Onboarding Wizard**:
  1. **Tier & Profile**: Tier 1 Paid Rural Educator, Tier 2 Youth Volunteer, or Tier 3 Retired Senior Mentor.
  2. **AI Document Verification (Gemini Vision)**: Zero-permission Photo Picker + `gemini-3.5-flash-lite` structured JSON multimodal analysis extracting Document Type, Candidate Name, Board, Pass Status, Confidence Score, and Decision (`AI_VERIFIED` / `NEEDS_REVIEW` / `REJECTED`), with offline queue fallback.
  3. **Pedagogy Readiness Check**: 3-question aptitude assessment.
  4. **Child Safety Contract**: Digital safeguarding pledge.
  5. **Training Completion**: Unlocks the full Teaching Dashboard.
* **Permanent Offline Video Uploads**: Gallery videos (`PickVisualMedia.VideoOnly`) are immediately copied into internal storage (`filesDir/offline_videos/`) so students can watch them forever—even in Airplane Mode and across account switches.
* **Homework & Doubt Resolution**: Assign offline homework directly to student backpacks, resolve queued doubts with instant student notifications, and toggle **Backup Roster** availability.
* **Recognition & Impact**: Shimmering metallic badges (**Bronze 10h**, **Silver 25h**, **Gold 50h**) and one-tap shareable **Verified Impact Certificates**.

---

### 3. Admin Command Center (100% Control)

* **Impact Metrics**: Live KPIs across Student Reach (Class 1–5 vs. 6–10), Learning Outcomes (XP & completions), Offline Equity (downloads & queued doubts), and Educator Force, plus **Broadcast Claymorphic Alerts** and one-tap **Export NGO Impact Report**.
* **Students (Full CRUD)**: Create, edit (Class, Center, Language, Parental Consent, XP), or remove student profiles.
* **Educators (Verification & Roster)**: Inspect Gemini Vision AI verification reports and confidence scores, Approve/Reject applications, manage Tiers, and toggle Backup Roster status.
* **Content & Doubt Moderation**: Review lesson ratings, approve/flag content, and answer pending doubts directly as Coordinator.

---

## Design System, Multilingual & Accessibility

* **Warm Claymorphism**: Soft rounded corners (`20dp–32dp`), layered inner/outer tactile drop shadows, warm low-glare palette (`WarmCreamBg`, `SoftOatSurface`, `TerracottaCoral`, `LeafGreen`, `SunAmber`, `EspressoInk`), and custom student workspace themes.
* **23 Languages**: Animated language selector with live search covering **English + all 22 Scheduled Indian Languages** in native scripts, with full reactive `strings.xml` localization in **English, Hindi, and Marathi**, persisted offline via `AppCompatDelegate.setApplicationLocales(...)`.
* **Adaptive & Accessible**: Full edge-to-edge safe insets, minimum `48dp` touch targets, TalkBack `contentDescription` semantics, interactive `?` Quick-Start Guide with TTS, and responsive `NavigationBar` (phones) $\leftrightarrow$ `NavigationRail` (tablets/foldables).

---

## Technical Architecture

Twogether uses **MVVM + Repository** with **Room Database v4** as the Single Source of Truth, ensuring instant UI responsiveness regardless of network state.

```text
┌──────────────────────────────────────────────────────────────────────┐
│                     UI Layer (Jetpack Compose)                       │
│   Student / Educator / Admin Portals  <──  collectAsStateWithLifecycle│
└──────────────────────────────────▲───────────────────────────────────┘
                                   │ StateFlow<UiState>
┌──────────────────────────────────┴───────────────────────────────────┐
│                    Presentation Layer (ViewModel)                    │
│                         TwogetherViewModel                           │
└──────────────────────────────────▲───────────────────────────────────┘
                                   │
┌──────────────────────────────────┴───────────────────────────────────┐
│                    Data Layer (TwogetherRepository)                  │
├──────────────────────────────┬───────────────────────────────────────┤
│   LOCAL (Always Available)   │    CLOUD (Optional / When Online)     │
│  • Room v4 (10 Entities)     │   • Firebase Firestore (Zero-Trust)   │
│  • Jetpack DataStore         │   • Firebase AI Logic + App Check     │
│  • Internal Storage (MP4s)   │   • Credential Manager (Google Auth)  │
│  • WorkManager Sync Workers  │   • Gemini Vision Doc Verification    │
└──────────────────────────────┴───────────────────────────────────────┘
```

### Tech Stack Summary
* **Language & Concurrency**: Kotlin, Coroutines, `StateFlow`
* **UI**: Jetpack Compose, Material 3, Custom Claymorphic Components & Canvas
* **Local Persistence**: Room Database v4 (10 Entities: `PortalAccount`, `StudentProfile`, `EducatorProfile`, `Lesson`, `QuizQuestion`, `QuizAttempt`, `Homework`, `DoubtQueue`, `ChatMessage`, `AppNotification`) & Jetpack DataStore (`SessionStore`)
* **Auth & Security**: Local SHA-256 password hashing, Compose Autofill semantics, Jetpack Credential Manager (Google Sign-In), zero-trust `firestore.rules` guarded by `canSyncToCloud(uid)`
* **Background Processing**: Android `WorkManager` (`DoubtSyncWorker`, `HomeworkReminderWorker`) & `NetworkMonitor`
* **AI & Media**: Firebase AI Logic (Cloud Gemini + `gemini-3.5-flash-lite`), Gemini Nano probe, Local NCERT Syllabus Engine, `TextureView` hardware-accelerated playback

---

## Pre-Seeded Demo Accounts

Jump straight into any portal immediately after installation (works 100% offline):

| Role | Name | Email | Password | Extra Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Student** | Asha (Class 5) | `asha.student@twogether.org` | `Asha@1234` | Avatar PIN: `0,1,2,3` (165 XP) |
| **Student** | Ravi (Class 8) | `ravi.student@twogether.org` | `Ravi@1234` | Avatar PIN: `1,1,2,2` (220 XP) |
| **Student** | Meena (Class 2) | `meena.student@twogether.org` | `Meena@1234` | Avatar PIN: `0,0,1,1` (70 XP) |
| **Educator** | Priya Sharma | `priya.teacher@twogether.org` | `Teach@1234` | Verified Tier 1 Educator (14.5h taught) |
| **Admin** | NGO Coordinator | `admin@twogether.org` | `Admin@1234` | Full 100% Control Center access |

---

## Getting Started

### Prerequisites
* **Android Studio** (Ladybug / Meerkat or newer)
* **JDK 17+**
* **Android SDK 34+** (Minimum SDK 24+)

### Installation & Setup
1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/twogether-android.git
   cd twogether-android
   ```
2. **Optional — Enable Cloud Sync & Online Gemini AI**
   * Place your Firebase `google-services.json` inside `app/`.
   * Enable **Firestore**, **Firebase Auth (Google)**, **Firebase AI Logic**, and **App Check** in your Firebase Console.
   * *(Note: Skipping this step is completely fine—the app runs 100% locally with pre-seeded accounts, bundled videos, and the offline AI Syllabus Engine.)*
3. **Build & Run**
   * Open the project in Android Studio, let Gradle sync, and run on an emulator or physical Android device.

### Testing Offline Resilience
* Turn on **Airplane Mode** on your test device (or flip the built-in **"Simulate Offline Mode"** toggle inside the *Doubts* tab).
* Watch any of the 12 bundled MP4 lessons, complete a quiz, ask the Study AI a syllabus question, upload a gallery video as an Educator, and queue a student doubt—then reconnect to observe automatic background synchronization.

---

## Automated Verification Suite

Twogether includes a comprehensive **Unit & Robolectric test suite** validating:
* Offline NCERT curriculum and flashcard catalog integrity.
* Duolingo-style sequential lesson unlocking (video watched + $\ge 50\%$ quiz score).
* End-to-end Educator gallery video copy (`filesDir`) $\rightarrow$ Airplane Mode $\rightarrow$ Student playback.
* Offline Progress Report Card and Verified Impact Certificate generation.

Run the full test suite via Gradle:
```bash
./gradlew testDebugUnitTest
```

---

## Contributing

Contributions that improve offline performance, expand Indian language translations (`strings.xml`), or add open NCERT curriculum modules are warmly welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

<div align="center">

**Twogether** — *Built with care for every learner, online or offline.*

</div>
