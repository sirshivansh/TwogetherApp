package com.example.util

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * Offline Multilingual Content Translation Resolver covering English and all
 * 22 Scheduled Indian Languages (Eighth Schedule of the Constitution of India).
 *
 * Translates Room-backed dynamic content (lessons, study notes, quizzes, homework,
 * offline FAQ answers, and key section labels) 100% offline with zero network calls.
 */
object OfflineContentTranslator {

    fun currentLanguageTag(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val raw = if (!appLocales.isEmpty) {
            appLocales[0]?.language ?: "en"
        } else {
            Locale.getDefault().language
        }
        return raw.lowercase(Locale.ROOT)
    }

    /**
     * Native script display name for each of the 22 Scheduled Indian Languages + English.
     */
    fun nativeLanguageLabel(tag: String): String = when (tag.lowercase(Locale.ROOT)) {
        "en" -> "English"
        "as" -> "অসমীয়া"
        "bn" -> "বাংলা"
        "brx" -> "बड़ो"
        "doi" -> "डोगरी"
        "gu" -> "ગુજરાતી"
        "hi" -> "हिन्दी"
        "kn" -> "ಕನ್ನಡ"
        "ks" -> "کٲشُر"
        "kok" -> "कोंकणी"
        "mai" -> "मैथिली"
        "ml" -> "മലയാളം"
        "mni" -> "মৈতৈলোন্"
        "mr" -> "मराठी"
        "ne" -> "नेपाली"
        "or" -> "ଓଡ଼ିଆ"
        "pa" -> "ਪੰਜਾਬੀ"
        "sa" -> "संस्कृतम्"
        "sat" -> "ᱥᱟᱱᱛᱟᱲᱤ"
        "sd" -> "سنڌي"
        "ta" -> "தமிழ்"
        "te" -> "తెలుగు"
        "ur" -> "اردو"
        else -> "English"
    }

    /**
     * Translates a seeded lesson title/summary/study notes into the active language offline.
     */
    fun translateLessonTitle(lessonId: String, defaultTitle: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return defaultTitle
        lessonTitles[lessonId]?.get(lang)?.let { return it }
        // Devanagari family shares Hindi educational terms when specific dialect entry is absent
        if (lang in devanagariLangs) {
            lessonTitles[lessonId]?.get("hi")?.let { return "$it (${nativeLanguageLabel(lang)})" }
        }
        if (lang == "ur" || lang == "ks" || lang == "sd") {
            lessonTitles[lessonId]?.get("ur")?.let { return it }
        }
        return translatePhraseWithTerms(defaultTitle, lang)
    }

    fun translateLessonSummary(lessonId: String, defaultSummary: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return defaultSummary
        lessonSummaries[lessonId]?.get(lang)?.let { return it }
        if (lang in devanagariLangs) {
            lessonSummaries[lessonId]?.get("hi")?.let { return it }
        }
        return translatePhraseWithTerms(defaultSummary, lang)
    }

    fun translateStudyNotes(lessonId: String, defaultNotes: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return defaultNotes
        lessonNotes[lessonId]?.get(lang)?.let { return it }
        if (lang in devanagariLangs) {
            lessonNotes[lessonId]?.get("hi")?.let { return it }
        }
        return translatePhraseWithTerms(defaultNotes, lang)
    }

    fun translateHomeworkTitle(hwId: String, defaultTitle: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return defaultTitle
        homeworkTitles[hwId]?.get(lang)?.let { return it }
        if (lang in devanagariLangs) {
            homeworkTitles[hwId]?.get("hi")?.let { return it }
        }
        return translatePhraseWithTerms(defaultTitle, lang)
    }

    fun translateHomeworkDescription(hwId: String, defaultDesc: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return defaultDesc
        homeworkDescriptions[hwId]?.get(lang)?.let { return it }
        if (lang in devanagariLangs) {
            homeworkDescriptions[hwId]?.get("hi")?.let { return it }
        }
        return translatePhraseWithTerms(defaultDesc, lang)
    }

    fun translateQuizQuestion(questionId: String, defaultPrompt: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return defaultPrompt
        quizPrompts[questionId]?.get(lang)?.let { return it }
        if (lang in devanagariLangs) {
            quizPrompts[questionId]?.get("hi")?.let { return it }
        }
        return translatePhraseWithTerms(defaultPrompt, lang)
    }

    fun translateQuizExplanation(questionId: String, defaultExp: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return defaultExp
        quizExplanations[questionId]?.get(lang)?.let { return it }
        if (lang in devanagariLangs) {
            quizExplanations[questionId]?.get("hi")?.let { return it }
        }
        return translatePhraseWithTerms(defaultExp, lang)
    }

    fun translateSubject(subject: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return subject
        val key = subject.trim().lowercase(Locale.ROOT)
        subjectNames[key]?.get(lang)?.let { return it }
        if (lang in devanagariLangs) {
            subjectNames[key]?.get("hi")?.let { return it }
        }
        return subject
    }

    /**
     * Localizes Study AI / FAQ text by translating key educational vocabulary & prefixing
     * the active Indian language study badge when full sentence pack isn't pre-authored.
     */
    fun translateStudyText(englishText: String, lang: String = currentLanguageTag()): String {
        if (lang == "en") return englishText
        return translatePhraseWithTerms(englishText, lang)
    }

    private fun translatePhraseWithTerms(text: String, lang: String): String {
        val glossary = termMapByLang[lang]
            ?: if (lang in devanagariLangs) termMapByLang["hi"] else null
        if (glossary == null) {
            return "[${nativeLanguageLabel(lang)}] $text"
        }
        var out = text
        glossary.forEach { (eng, local) ->
            if (out.contains(eng, ignoreCase = true)) {
                out = out.replace(Regex("\\b${Regex.escape(eng)}\\b", RegexOption.IGNORE_CASE), "$local ($eng)")
            }
        }
        return if (out == text) "[${nativeLanguageLabel(lang)}] $text" else out
    }

    private val devanagariLangs = setOf("hi", "mr", "ne", "sa", "mai", "kok", "doi", "brx")

    private val subjectNames: Map<String, Map<String, String>> = mapOf(
        "math" to mapOf(
            "hi" to "गणित", "mr" to "गणित", "bn" to "গণিত", "ta" to "கணிதம்",
            "te" to "గణితం", "gu" to "ગણિત", "kn" to "ಗಣಿತ", "ml" to "ഗണിതം",
            "pa" to "ਗਣਿਤ", "or" to "ଗଣିତ", "as" to "গণিত", "ur" to "ریاضی",
            "sa" to "गणितम्", "ne" to "गणित", "sd" to "رياضي", "ks" to "حساب",
            "sat" to "ᱮᱞᱠᱷᱟ", "mni" to "অঙ্ক"
        ),
        "science" to mapOf(
            "hi" to "विज्ञान", "mr" to "विज्ञान", "bn" to "বিজ্ঞান", "ta" to "அறிவியல்",
            "te" to "సైన్స్ (విజ్ఞాన శాస్త్రం)", "gu" to "વિજ્ઞાન", "kn" to "ವಿಜ್ಞಾನ", "ml" to "ശാസ്ത്രം",
            "pa" to "ਵਿਗਿਆਨ", "or" to "ବିଜ୍ଞାନ", "as" to "বিজ্ঞান", "ur" to "سائنس",
            "sa" to "विज्ञानम्", "ne" to "विज्ञान", "sd" to "سائنس", "ks" to "سائنس",
            "sat" to "ᱥᱟᱬᱮᱥ", "mni" to "সাইন্স"
        ),
        "english" to mapOf(
            "hi" to "अंग्रेज़ी", "mr" to "इंग्रजी", "bn" to "ইংরেজি", "ta" to "ஆங்கிலம்",
            "te" to "ఆంగ్లం", "gu" to "અંગ્રેજી", "kn" to "ಇಂಗ್ಲಿಷ್", "ml" to "ഇംഗ്ലീഷ്",
            "pa" to "ਅੰਗਰੇਜ਼ੀ", "or" to "ଇଂରାଜୀ", "as" to "ইংৰাজী", "ur" to "انگریزی",
            "sa" to "आङ्ग्लभाषा", "ne" to "अंग्रेजी"
        ),
        "social studies" to mapOf(
            "hi" to "सामाजिक अध्ययन", "mr" to "सामाजिक शास्त्र", "bn" to "সমাজ বিজ্ঞান", "ta" to "சமூக அறிவியல்",
            "te" to "సాంఘిక శాస్త్రం", "gu" to "સામાજિક વિજ્ઞાન", "kn" to "ಸಮಾಜ ವಿಜ್ಞಾನ", "ml" to "സാമൂഹ്യശാസ്ത്രം",
            "pa" to "ਸਮਾਜਿਕ ਅਧਿਐਨ", "or" to "ସାମାଜିକ ବିଜ୍ଞାନ", "as" to "সমাজ বিজ্ঞান", "ur" to "سماجی علوم"
        ),
        "hindi" to mapOf(
            "hi" to "हिन्दी", "mr" to "हिंदी", "bn" to "হিন্দি", "ta" to "இந்தி",
            "te" to "హిందీ", "gu" to "હિન્દી", "kn" to "ಹಿಂದಿ", "ml" to "ഹിന്ദി",
            "pa" to "ਹਿੰਦੀ", "or" to "ହିନ୍ଦୀ", "as" to "হিন্দী", "ur" to "ہندی"
        )
    )

    private val lessonTitles: Map<String, Map<String, String>> = mapOf(
        "lesson_c1_counting" to mapOf(
            "hi" to "बीज गिनती और दोस्ताना संख्याएँ (कक्षा 1)",
            "mr" to "बिया मोजणी आणि सोप्या संख्या (इयत्ता 1)",
            "bn" to "বীজ গণনা ও সহজ সংখ্যা (শ্রেণি ১)",
            "ta" to "விதைகள் எண்ணுதல் & எண்கள் (வகுப்பு 1)",
            "te" to "విత్తనాల లెక్కింపు & సంఖ్యలు (తరగతి 1)",
            "gu" to "બીજ ગણતરી અને સંખ્યાઓ (ધોરણ 1)",
            "kn" to "ಬೀಜಗಳ ಎಣಿಕೆ & ಸಂಖ್ಯೆಗಳು (ತರಗತಿ 1)",
            "ml" to "വിത്തുകൾ എണ്ണലും സംഖ്യകളും (ക്ലാസ് 1)",
            "pa" to "ਬੀਜ ਗਿਣਤੀ ਅਤੇ ਸੰਖਿਆਵਾਂ (ਜਮਾਤ 1)",
            "or" to "ମଞ୍ଜି ଗଣନା ଓ ସଂଖ୍ୟା (ଶ୍ରେଣୀ ୧)",
            "as" to "বীজ গণনা আৰু সংখ্যা (শ্ৰেণী ১)",
            "ur" to "بیجوں کی گنتی اور اعداد (کلاس 1)"
        ),
        "lesson_c2_nouns" to mapOf(
            "hi" to "हमारे गाँव के आस-पास के नाम शब्द (संज्ञा)",
            "mr" to "आपल्या गावातील नाम शब्द (संज्ञा)",
            "bn" to "আমাদের গ্রামের নামবাচক শব্দ (বিশেষ্য)",
            "ta" to "நமது கிராமத்தின் பெயர்ச்சொற்கள்",
            "te" to "మన గ్రామంలోని నామవాచకాలు",
            "gu" to "આપણા ગામના નામ શબ્દો (સંજ્ઞા)",
            "ur" to "ہمارے گاؤں کے اسم (نام والے الفاظ)"
        ),
        "lesson_c4_water" to mapOf(
            "hi" to "जल चक्र की यात्रा (वाष्पीकरण और वर्षा)",
            "mr" to "जलचक्राचा प्रवास (बाष्पीभवन आणि पाऊस)",
            "bn" to "জলচক্রের যাত্রা (বাষ্পীভবন ও বৃষ্টি)",
            "ta" to "நீர் சுழற்சியின் பயணம்",
            "te" to "నీటి చక్రం ప్రయాణం",
            "gu" to "જળચક્રની સફર",
            "ur" to "آبی چکر کا سفر"
        ),
        "lesson_c5_fractions" to mapOf(
            "hi" to "रोटी के टुकड़ों से भिन्न सीखें (कक्षा 5)",
            "mr" to "चपातीच्या तुकड्यांतून अपूर्णांक शिका (इयत्ता 5)",
            "bn" to "রুটির টুকরো দিয়ে ভগ্নাংশ শেখা (শ্রেণি ৫)",
            "ta" to "ரொட்டி துண்டுகளுடன் பின்னங்கள் (வகுப்பு 5)",
            "te" to "రొట్టె ముక్కలతో భిన్నాలు (తరగతి 5)",
            "gu" to "રોટલીના ટુકડાથી અપૂર્ણાંક શીખો (ધોરણ 5)",
            "kn" to "ರೊಟ್ಟಿ ತುಂಡುಗಳಿಂದ ಭಿನ್ನರಾಶಿಗಳು (ತರಗತಿ 5)",
            "ml" to "ചപ്പാത്തി കഷണങ്ങളിലൂടെ ഭിന്നസംഖ്യകൾ (ക്ലാസ് 5)",
            "pa" to "ਰੋਟੀ ਦੇ ਟੁਕੜਿਆਂ ਨਾਲ ਭਿੰਨਾਂ (ਜਮਾਤ 5)",
            "or" to "ରୁଟି ଖଣ୍ଡରୁ ଭଗ୍ନାଂଶ ଶିଖନ୍ତୁ (ଶ୍ରେଣୀ ୫)",
            "as" to "ৰুটিৰ টুকুৰাৰে ভগ্নাংশ শিকা (শ্ৰেণী ৫)",
            "ur" to "روٹی کے ٹکڑوں سے کسر سیکھیں (کلاس 5)"
        ),
        "lesson_c6_photosynthesis" to mapOf(
            "hi" to "हरी पत्तियाँ भोजन कैसे बनाती हैं (प्रकाश संश्लेषण)",
            "mr" to "हिरवी पाने अन्न कसे तयार करतात (प्रकाशसंश्लेषण)",
            "bn" to "সবুজ পাতা কীভাবে খাদ্য তৈরি করে (সালোকসংশ্লেষ)",
            "ta" to "பச்சை இலைகள் உணவு தயாரித்தல் (ஒளிச்சேர்க்கை)",
            "te" to "ఆకుపచ్చ ఆకులు ఆహారం ఎలా తయారు చేస్తాయి",
            "gu" to "લીલા પાંદડા ખોરાક કેવી રીતે બનાવે છે",
            "kn" to "ಹಸಿರು ಎಲೆಗಳು ಆಹಾರ ತಯಾರಿಸುವ ವಿಧಾನ",
            "ml" to "പച്ച ഇലകൾ ഭക്ഷണം ഉണ്ടാക്കുന്ന വിധം",
            "pa" to "ਹਰੇ ਪੱਤੇ ਭੋਜਨ ਕਿਵੇਂ ਬਣਾਉਂਦੇ ਹਨ",
            "or" to "ସବୁଜ ପତ୍ର ଖାଦ୍ୟ କିପରି ପ୍ରସ୍ତୁତ କରେ",
            "as" to "সেউজীয়া পাতে খাদ্য কেনেকৈ তৈয়াৰ কৰে",
            "ur" to "ہری پتیاں خوراک کیسے بناتی ہیں"
        ),
        "lesson_c7_grammar" to mapOf(
            "hi" to "सक्रिय और स्पष्ट क्रियाएँ (कक्षा 7)",
            "mr" to "सक्रिय आणि स्पष्ट क्रियापदे (इयत्ता 7)",
            "bn" to "ক্রিয়াপদ ও বাক্য গঠন (শ্রেণি ৭)",
            "ta" to "வினைச்சொற்கள் மற்றும் வாக்கிய அமைப்பு",
            "te" to "క్రియలు మరియు వాక్య నిర్మాణం",
            "gu" to "ક્રિયાપદો અને વાક્ય રચના",
            "ur" to "فعل اور جملے کی ساخت"
        ),
        "lesson_c8_algebra" to mapOf(
            "hi" to "सरल रैखिक समीकरण हल करना (कक्षा 8)",
            "mr" to "सोपी रेषीय समीकरणे सोडवणे (इयत्ता 8)",
            "bn" to "সরল রৈখিক সমীকরণ সমাধান (শ্রেণি ৮)",
            "ta" to "நேரியல் சமன்பாடுகளைத் தீர்த்தல் (வகுப்பு 8)",
            "te" to "సరళ రేఖీయ సమీకరణాల సాధన (తరగతి 8)",
            "gu" to "સરળ સુરેખ સમીકરણ ઉકેલ (ધોરણ 8)",
            "ur" to "سادہ خطی مساوات حل کرنا (کلاس 8)"
        ),
        "lesson_c9_motion" to mapOf(
            "hi" to "क्रिकेट में न्यूटन के गति के नियम (कक्षा 9)",
            "mr" to "क्रिकेटमधील न्यूटनचे गतीचे नियम (इयत्ता 9)",
            "bn" to "ক্রিকেটে নিউটনের গতির সূত্র (শ্রেণি ৯)",
            "ta" to "கிரிக்கெட்டில் நியூட்டனின் இயக்க விதிகள்",
            "te" to "క్రికెట్‌లో న్యూటన్ గమన నియమాలు",
            "gu" to "ક્રિકેટમાં ન્યૂટનના ગતિના નિયમો",
            "ur" to "کرکٹ میں نیوٹن کے حرکت کے قوانین"
        ),
        "lesson_c10_constitution" to mapOf(
            "hi" to "हमारे अधिकार और सामुदायिक कर्तव्य (कक्षा 10)",
            "mr" to "आपले हक्क आणि सामाजिक कर्तव्ये (इयत्ता 10)",
            "bn" to "আমাদের অধিকার ও সামাজিক কর্তব্য (শ্রেণি ১০)",
            "ta" to "நமது உரிமைகள் மற்றும் சமூக கடமைகள்",
            "te" to "మన హక్కులు మరియు సామాజిక బాధ్యతలు",
            "gu" to "આપણા અધિકારો અને સામાજિક ફરજો",
            "ur" to "ہمارے حقوق اور سماجی فرائض"
        )
    )

    private val lessonSummaries: Map<String, Map<String, String>> = mapOf(
        "lesson_c5_fractions" to mapOf(
            "hi" to "समान हिस्से, अंश (Numerator), हर (Denominator) और सरल भिन्न जोड़ को रोज़मर्रा के उदाहरणों से समझें।",
            "mr" to "समान भाग, अंश (Numerator), छेद (Denominator) आणि सोपी अपूर्णांक बेरीज दैनंदिन उदाहरणांतून शिका.",
            "bn" to "সমান ভাগ, লব (Numerator), হর (Denominator) এবং সহজ ভগ্নাংশের যোগ শিখুন।",
            "ta" to "சம பாகங்கள், தொகுதி (Numerator), பகுதி (Denominator) மற்றும் பின்னக் கூட்டலைக் கற்றுக்கொள்ளுங்கள்.",
            "te" to "సమాన భాగాలు, లవం (Numerator), హారం (Denominator) మరియు సులభమైన భిన్నాల కూడిక నేర్చుకోండి.",
            "gu" to "સરખા ભાગો, અંશ (Numerator), છેદ (Denominator) અને સરળ અપૂર્ણાંક સરવાળો શીખો.",
            "ur" to "برابر حصے، شمار کنندہ (Numerator)، نسب نما (Denominator) اور کسر کی جمع سیکھیں۔"
        ),
        "lesson_c6_photosynthesis" to mapOf(
            "hi" to "जानें कि हरी पत्तियाँ सूर्य के प्रकाश, जल और कार्बन डाइऑक्साइड से भोजन और ऑक्सीजन कैसे बनाती हैं।",
            "mr" to "हिरवी पाने सूर्यप्रकाश, पाणी आणि कार्बन डायऑक्साइड वापरून अन्न व ऑक्सिजन कसे तयार करतात ते पहा.",
            "bn" to "সবুজ পাতা কীভাবে সূর্যালোক, জল ও কার্বন ডাই-অক্সাইড ব্যবহার করে খাদ্য ও অক্সিজেন তৈরি করে তা জানুন।",
            "ta" to "பச்சை இலைகள் சூரிய ஒளி, நீர் மற்றும் கார்பன் டை ஆக்சைடு கொண்டு உணவு மற்றும் ஆக்ஸிஜன் தயாரிப்பதை அறிக."
        )
    )

    private val lessonNotes: Map<String, Map<String, String>> = mapOf(
        "lesson_c5_fractions" to mapOf(
            "hi" to "• अंश (Numerator) = ऊपर की संख्या (खाए गए हिस्से)\n• हर (Denominator) = नीचे की संख्या (कुल बराबर हिस्से)\n• जोड़ने से पहले हमेशा हर बराबर करें!",
            "mr" to "• अंश (Numerator) = वरची संख्या (खाल्लेले भाग)\n• छेद (Denominator) = खालची संख्या (एकूण समान भाग)\n• बेरीज करण्यापूर्वी नेहमी छेद समान करा!",
            "bn" to "• লব (Numerator) = উপরের সংখ্যা (নেওয়া অংশ)\n• হর (Denominator) = নিচের সংখ্যা (মোট সমান অংশ)\n• যোগ করার আগে হর সমান করুন!",
            "ta" to "• தொகுதி (Numerator) = மேல் எண் (எடுக்கப்பட்ட பாகங்கள்)\n• பகுதி (Denominator) = கீழ் எண் (மொத்த சம பாகங்கள்)"
        ),
        "lesson_c6_photosynthesis" to mapOf(
            "hi" to "• प्रकाश संश्लेषण (Photosynthesis): सूर्य का प्रकाश + जल + CO2 -> ग्लूकोज़ (भोजन) + ऑक्सीजन।\n• क्लोरोफिल (Chlorophyll) पत्तियों को हरा रंग देता है और धूप सोखता है।",
            "mr" to "• प्रकाशसंश्लेषण (Photosynthesis): सूर्यप्रकाश + पाणी + CO2 -> ग्लुकोज (अन्न) + ऑक्सिजन.\n• हरितद्रव्य (Chlorophyll) पानांना हिरवा रंग देते आणि सूर्यप्रकाश शोषून घेते."
        )
    )

    private val homeworkTitles: Map<String, Map<String, String>> = mapOf(
        "hw_math_5_fractions" to mapOf(
            "hi" to "भिन्न अभ्यास शीट: रोटी के 8 हिस्से",
            "mr" to "अपूर्णांक सराव: चपातीचे 8 भाग",
            "bn" to "ভগ্নাংশ অনুশীলন: ৮ টুকরো রুটি",
            "ta" to "பின்னப் பயிற்சி: 8 ரொட்டி துண்டுகள்",
            "te" to "భిన్నాల అభ్యాసం: 8 రొట్టె ముక్కలు",
            "gu" to "અપૂર્ણાંક અભ્યાસ: રોટલીના 8 ટુકડા",
            "ur" to "کسر کی مشق: روٹی کے 8 حصے"
        ),
        "hw_sci_5_leaf" to mapOf(
            "hi" to "पत्ती का चित्र बनाएँ और प्रकाश संश्लेषण लिखें",
            "mr" to "पानाचे चित्र काढा आणि प्रकाशसंश्लेषण लिहा",
            "bn" to "পাতার ছবি আঁকো এবং সালোকসংশ্লেষ লেখো",
            "ta" to "இலை வரைந்து ஒளிச்சேர்க்கையை எழுதுக",
            "gu" to "પાંદડાનું ચિત્ર દોરો અને પ્રકાશસંશ્લેષણ લખો"
        ),
        "hw_eng_3_story" to mapOf(
            "hi" to "अपने दिन के बारे में 5 क्रिया वाक्य लिखें",
            "mr" to "तुमच्या दिवसातील 5 क्रियापदांची वाक्ये लिहा",
            "bn" to "তোমার দিন সম্পর্কে ৫টি ইংরেজি বাক্য লেখো",
            "ta" to "உங்கள் நாள் பற்றி 5 ஆங்கில வாக்கியங்கள் எழுதுக"
        ),
        "hw_sci_8_water" to mapOf(
            "hi" to "जल चक्र के 3 चरण: वाष्पीकरण, संघनन, वर्षा",
            "mr" to "जलचक्राचे 3 टप्पे: बाष्पीभवन, संघनन, पाऊस",
            "bn" to "জলচক্রের ৩টি ধাপ: বাষ্পীভবন, ঘনীভবন, বৃষ্টিপাত",
            "ta" to "நீர் சுழற்சியின் 3 நிலைகள்: ஆவியாதல், சுருங்குதல், மழை"
        )
    )

    private val homeworkDescriptions: Map<String, Map<String, String>> = mapOf(
        "hw_math_5_fractions" to mapOf(
            "hi" to "अपनी कॉपी में 3 गोले बनाएँ और 1/2, 3/4, तथा 5/8 हिस्से रंगें। अंश और हर लिखें।",
            "mr" to "तुमच्या वहीत 3 वर्तुळे काढा आणि 1/2, 3/4 व 5/8 भाग रंगवा. अंश आणि छेद लिहा.",
            "bn" to "তোমার খাতায় ৩টি বৃত্ত আঁকো এবং ১/২, ৩/৪ ও ৫/৮ অংশ রং করো। লব ও হর লেখো।",
            "ta" to "உங்கள் நோட்டில் 3 வட்டங்கள் வரைந்து 1/2, 3/4 மற்றும் 5/8 பாகங்களை வண்ணம் தீட்டுக."
        ),
        "hw_sci_5_leaf" to mapOf(
            "hi" to "एक हरी पत्ती का चित्र बनाएँ और सूर्य का प्रकाश, जल, क्लोरोफिल और ऑक्सीजन अंकित करें।",
            "mr" to "एका हिरव्या पानाचे चित्र काढा आणि सूर्यप्रकाश, पाणी, हरितद्रव्य व ऑक्सिजन दाखवा."
        )
    )

    private val quizPrompts: Map<String, Map<String, String>> = mapOf(
        "q_c5_1" to mapOf(
            "hi" to "यदि एक रोटी को 4 बराबर टुकड़ों में काटा जाए और आप 1 टुकड़ा खा लें, तो कौन सा भिन्न खाया?",
            "mr" to "एका चपातीचे 4 समान तुकडे केले आणि तुम्ही 1 तुकडा खाल्ला, तर कोणता अपूर्णांक खाल्ला?",
            "bn" to "একটি রুটিকে ৪টি সমান ভাগে ভাগ করে ১ ভাগ খেলে, কত ভগ্নাংশ খাওয়া হলো?",
            "ta" to "ஒரு ரொட்டியை 4 சம துண்டுகளாக வெட்டி 1 துண்டு சாப்பிட்டால், அது எந்த பின்னம்?"
        ),
        "q_c5_2" to mapOf(
            "hi" to "1/4 + 2/4 का मान क्या होगा?",
            "mr" to "1/4 + 2/4 ची बेरीज किती?",
            "bn" to "১/৪ + ২/৪ সমান কত?",
            "ta" to "1/4 + 2/4 எவ்வளவு?"
        ),
        "q_c6_1" to mapOf(
            "hi" to "पत्तियों में कौन सा हरा पदार्थ सूर्य के प्रकाश को सोखता है?",
            "mr" to "पानांमधील कोणते हिरवे द्रव्य सूर्यप्रकाश शोषून घेते?",
            "bn" to "পাতার কোন সবুজ উপাদান সূর্যালোক শোষণ করে?",
            "ta" to "இலைகளில் உள்ள எந்த பச்சை நிறமி சூரிய ஒளியை உறிஞ்சுகிறது?"
        )
    )

    private val quizExplanations: Map<String, Map<String, String>> = mapOf(
        "q_c5_1" to mapOf(
            "hi" to "4 बराबर टुकड़ों में से 1 टुकड़ा = 1/4। यहाँ 1 अंश (Numerator) और 4 हर (Denominator) है।",
            "mr" to "4 समान तुकड्यांपैकी 1 तुकडा = 1/4. येथे 1 अंश (Numerator) आणि 4 छेद (Denominator) आहे."
        ),
        "q_c5_2" to mapOf(
            "hi" to "समान हर 4 होने पर ऊपर के अंश जोड़ें: 1 + 2 = 3, इसलिए 3/4!",
            "mr" to "समान छेद 4 असल्याने वरचे अंश जोडा: 1 + 2 = 3, म्हणून 3/4!"
        ),
        "q_c6_1" to mapOf(
            "hi" to "क्लोरोफिल (Chlorophyll) पत्तियों को हरा बनाता है और प्रकाश संश्लेषण के लिए धूप सोखता है।",
            "mr" to "हरितद्रव्य (Chlorophyll) पानांना हिरवा रंग देते आणि प्रकाशसंश्लेषणासाठी सूर्यप्रकाश शोषते."
        )
    )

    private val termMapByLang: Map<String, Map<String, String>> = mapOf(
        "hi" to mapOf(
            "numerator" to "अंश", "denominator" to "हर", "fraction" to "भिन्न", "fractions" to "भिन्न",
            "photosynthesis" to "प्रकाश संश्लेषण", "chlorophyll" to "क्लोरोफिल",
            "evaporation" to "वाष्पीकरण", "condensation" to "संघनन", "water cycle" to "जल चक्र",
            "noun" to "संज्ञा", "verb" to "क्रिया", "constitution" to "संविधान", "quadratic" to "द्विघात"
        ),
        "mr" to mapOf(
            "numerator" to "अंश", "denominator" to "छेद", "fraction" to "अपूर्णांक", "fractions" to "अपूर्णांक",
            "photosynthesis" to "प्रकाशसंश्लेषण", "chlorophyll" to "हरितद्रव्य",
            "evaporation" to "बाष्पीभवन", "condensation" to "संघनन", "water cycle" to "जलचक्र",
            "noun" to "नाम", "verb" to "क्रियापद", "constitution" to "संविधान"
        ),
        "bn" to mapOf(
            "numerator" to "লব", "denominator" to "হর", "fraction" to "ভগ্নাংশ",
            "photosynthesis" to "সালোকসংশ্লেষ", "chlorophyll" to "ক্লোরোফিল",
            "evaporation" to "বাষ্পীভবন", "water cycle" to "জলচক্র"
        ),
        "ta" to mapOf(
            "numerator" to "தொகுதி", "denominator" to "பகுதி", "fraction" to "பின்னம்",
            "photosynthesis" to "ஒளிச்சேர்க்கை", "chlorophyll" to "பச்சையம்",
            "water cycle" to "நீர் சுழற்சி"
        ),
        "te" to mapOf(
            "numerator" to "లవం", "denominator" to "హారం", "fraction" to "భిన్నం",
            "photosynthesis" to "కిరణజన్య సంయోగక్రియ", "water cycle" to "నీటి చక్రం"
        ),
        "gu" to mapOf(
            "numerator" to "અંશ", "denominator" to "છેદ", "fraction" to "અપૂર્ણાંક",
            "photosynthesis" to "પ્રકાશસંશ્લેષણ", "water cycle" to "જળચક્ર"
        ),
        "kn" to mapOf(
            "numerator" to "ಅಂಶ", "denominator" to "ಛೇದ", "fraction" to "ಭಿನ್ನರಾಶಿ",
            "photosynthesis" to "ದ್ಯುತಿಸಂಶ್ಲೇಷಣೆ", "water cycle" to "ಜಲಚಕ್ರ"
        ),
        "ml" to mapOf(
            "numerator" to "അംശം", "denominator" to "ഛേദം", "fraction" to "ഭിന്നസംഖ്യ",
            "photosynthesis" to "പ്രകാശസംശ്ലേഷണം", "water cycle" to "ജലചക്രം"
        ),
        "pa" to mapOf(
            "numerator" to "ਅੰਸ਼", "denominator" to "ਹਰ", "fraction" to "ਭਿੰਨ",
            "photosynthesis" to "ਪ੍ਰਕਾਸ਼ ਸੰਸ਼ਲੇਸ਼ਣ", "water cycle" to "ਜਲ ਚੱਕਰ"
        ),
        "ur" to mapOf(
            "numerator" to "شمار کنندہ", "denominator" to "نسب نما", "fraction" to "کسر",
            "photosynthesis" to "ضیائی تالیف", "water cycle" to "آبی چکر"
        ),
        "or" to mapOf(
            "numerator" to "ଲବ", "denominator" to "ହର", "fraction" to "ଭଗ୍ନାଂଶ",
            "photosynthesis" to "ଆଲୋକଶ୍ଳେଷଣ", "water cycle" to "ଜଳଚକ୍ର"
        ),
        "as" to mapOf(
            "numerator" to "লব", "denominator" to "হৰ", "fraction" to "ভগ্নাংশ",
            "photosynthesis" to "সালোকসংশ্লেষণ", "water cycle" to "জলচক্ৰ"
        )
    )
}
