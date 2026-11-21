package com.example.data.curriculum

import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.ConceptMastery
import com.example.data.model.LessonReadiness
import com.example.data.model.MultilingualText
import com.example.data.model.SceneInteractionType
import com.example.data.model.SceneVisualType
import com.example.data.model.StoryChoice
import com.example.data.model.StoryLesson
import com.example.data.model.StoryScene
import com.example.data.model.StoryStyle
import com.example.data.model.StudentProfile
import com.example.data.model.TeachingMethod
import com.example.data.model.WeeklyTestQuestion

object LessonCatalog {

    const val LESSON_LAMPS_ID = "ukg_three_festival_lamps"
    const val LESSON_DROP_ID = "class4_day_drop_came_home"

    val allSubjects = listOf(
        "English", "Hindi", "Odia", "Mathematics", "EVS",
        "Science", "Social Science", "GK", "Painting", "Yoga"
    )

    val allClasses = listOf(
        "LKG", "UKG", "Class 1", "Class 2", "Class 3", "Class 4",
        "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"
    )

    fun starterBarshaProfile(uid: String): StudentProfile = StudentProfile(
        id = "sample_barsha_ukg",
        userId = uid,
        preferredName = "Barsha",
        age = 5,
        school = "Saraswati Shishu Mandir, Cuttack",
        studentClass = "UKG",
        rollNumber = "07",
        board = "CBSE",
        schoolMedium = "English",
        preferredLanguage = AppLanguage.HINDI.name,
        subjects = listOf("English", "Hindi", "Odia", "Mathematics", "EVS", "Painting", "Yoga"),
        subjectDurations = mapOf(
            "English" to 20L,
            "Hindi" to 15L,
            "Odia" to 15L,
            "Mathematics" to 15L,
            "EVS" to 15L,
            "Painting" to 15L,
            "Yoga" to 10L
        ),
        interests = listOf("Festival Diyas", "Drawing", "Animal Friends"),
        avatarColorHex = "#D9531E",
        sampleProfile = true,
        cameraPermissionForChild = false,
        lastLessonId = LESSON_LAMPS_ID,
        lastLessonTitle = "The Three Festival Lamps",
        lastConceptId = "counting_one_to_one",
        lastStepIndex = 0,
        lastTeachingApproach = TeachingMethod.STORY_SCENE.name
    )

    fun starterRaghavProfile(uid: String): StudentProfile = StudentProfile(
        id = "sample_raghav_c4",
        userId = uid,
        preferredName = "Raghav",
        age = 9,
        school = "DAV Public School, Jeypore",
        studentClass = "Class 4",
        rollNumber = "14",
        board = "CBSE",
        schoolMedium = "English",
        preferredLanguage = AppLanguage.ENGLISH.name,
        subjects = allSubjects,
        subjectDurations = mapOf(
            "English" to 20L,
            "Hindi" to 20L,
            "Odia" to 20L,
            "Mathematics" to 25L,
            "EVS" to 20L,
            "Science" to 25L,
            "Social Science" to 20L,
            "GK" to 15L,
            "Painting" to 15L,
            "Yoga" to 15L
        ),
        interests = listOf("Rain & Clouds", "Space", "Cricket"),
        avatarColorHex = "#0E7490",
        sampleProfile = true,
        cameraPermissionForChild = false,
        lastLessonId = LESSON_DROP_ID,
        lastLessonTitle = "The Day a Drop Came Home",
        lastConceptId = "water_cycle_evaporation",
        lastStepIndex = 0,
        lastTeachingApproach = TeachingMethod.STORY_SCENE.name
    )

    fun starterBarshaMastery(uid: String): ConceptMastery = ConceptMastery(
        id = "sample_barsha_ukg__counting_one_to_one",
        userId = uid,
        profileId = "sample_barsha_ukg",
        conceptId = "counting_one_to_one",
        conceptTitle = "One-to-One Counting (1 to 5)",
        subject = "Mathematics",
        lessonId = LESSON_LAMPS_ID,
        independentCorrect = 2,
        hintAssistedCorrect = 1,
        attempts = 3,
        misconceptions = emptyList(),
        needsPractice = false,
        nextDayRecallDue = false,
        nextDayRecallPassed = false,
        lastMethodUsed = TeachingMethod.STORY_SCENE.name,
        demonstratedEvidence = "2 of 3 answered independently"
    )

    fun starterRaghavMastery(uid: String): ConceptMastery = ConceptMastery(
        id = "sample_raghav_c4__water_cycle_evaporation",
        userId = uid,
        profileId = "sample_raghav_c4",
        conceptId = "water_cycle_evaporation",
        conceptTitle = "Evaporation, Condensation & Precipitation",
        subject = "Science",
        lessonId = LESSON_DROP_ID,
        independentCorrect = 3,
        hintAssistedCorrect = 1,
        attempts = 4,
        misconceptions = listOf("Thought ground drank the whole puddle"),
        needsPractice = true,
        nextDayRecallDue = true,
        nextDayRecallPassed = false,
        lastMethodUsed = TeachingMethod.DRAW_DIAGRAM.name,
        demonstratedEvidence = "3 of 4 answered independently"
    )

    val lessonThreeFestivalLamps = StoryLesson(
        id = LESSON_LAMPS_ID,
        title = MultilingualText(
            english = "The Three Festival Lamps",
            hindi = "तीन त्योहार के दीये",
            odia = "ତିନୋଟି ପର୍ବ ଦୀପ"
        ),
        subtitle = MultilingualText(
            english = "UKG Mathematics • Counting & One-to-One Matching",
            hindi = "यूकेजी गणित • एक-एक करके गिनती",
            odia = "UKG ଗଣିତ • ଗୋଟିଏ ଗୋଟିଏ ଗଣନା"
        ),
        targetClass = "UKG",
        subject = "Mathematics",
        conceptId = "counting_one_to_one",
        conceptTitle = "One-to-One Counting (1 to 3+)",
        recommendedStyle = StoryStyle.HUMAN_ADVENTURE,
        readiness = LessonReadiness.FULL_STORY_READY,
        heroDrawableResId = R.drawable.img_lesson_lamps_courtyard,
        offlineAvailable = true,
        scenes = listOf(
            StoryScene(
                id = "lamps_hook",
                beatTag = "1. Courtyard Hook",
                visualType = SceneVisualType.LAMPS_COURTYARD_TWO_PLUS_ONE,
                interactionType = SceneInteractionType.NUMERIC_OR_SPEECH_ANSWER,
                drawableResId = R.drawable.img_lesson_lamps_courtyard,
                characterLine = MultilingualText(
                    english = "Dadi: \"Look how bright our veranda step looks! Let us place one more diya beside these two.\"",
                    hindi = "दादी: \"देखो आँगन की सीढ़ी कितनी चमक रही है! इन दो दीयों के पास एक और दीया रखो।\"",
                    odia = "ଜେଜେମା: \"ଦେଖ ଆମ ବାରଣ୍ଡା ପାହାଚ କେତେ ସୁନ୍ଦର ଦିଶୁଛି! ଏହି ଦୁଇଟି ଦୀପ ପାଖରେ ଆଉ ଗୋଟିଏ ଦୀପ ରଖ।\""
                ),
                teacherNarration = MultilingualText(
                    english = "Sit close with me! Dadi and her granddaughter are decorating their courtyard for the festival. Two warm clay lamps glow on the stone step. Now she gently places one more lamp beside them.",
                    hindi = "मेरे पास बैठो! दादी और उनकी पोती त्योहार के लिए आँगन सजा रही हैं। पत्थर की सीढ़ी पर दो मिट्टी के दीये चमक रहे हैं। अब वह उनके पास एक और दीया रखती है।",
                    odia = "ମୋ ପାଖରେ ବସ! ଜେଜେମା ଏବଂ ତାଙ୍କ ନାତୁଣୀ ପର୍ବ ପାଇଁ ଅଗଣା ସଜାଉଛନ୍ତି। ପଥର ପାହାଚରେ ଦୁଇଟି ମାଟି ଦୀପ ଜଳୁଛି। ଏବେ ସେ ତାଙ୍କ ପାଖରେ ଆଉ ଗୋଟିଏ ଦୀପ ରଖିଲେ।"
                ),
                pauseQuestion = MultilingualText(
                    english = "{name}, how many lamps are glowing now?",
                    hindi = "{name}, अब कुल कितने दीये चमक रहे हैं?",
                    odia = "{name}, ଏବେ ମୋଟ କେତୋଟି ଦୀପ ଜଳୁଛି?"
                ),
                expectedKeywords = listOf("3", "three", "तीन", "teen", "ତିନି", "tini"),
                expectedNumber = 3,
                choices = listOf(
                    StoryChoice("c2", MultilingualText("2 lamps", "2 दीये", "୨ଟି ଦୀପ"), false, "Missed the newly added lamp"),
                    StoryChoice("c3", MultilingualText("3 lamps", "3 दीये", "୩ଟି ଦୀପ"), true),
                    StoryChoice("c4", MultilingualText("4 lamps", "4 दीये", "୪ଟି ଦୀପ"), false, "Counted one lamp twice")
                ),
                adaptiveRetryPrompt = MultilingualText(
                    english = "That is okay! Let us slow down and touch each lamp one by one with music.",
                    hindi = "कोई बात नहीं! चलो धीरे-धीरे हर दीये को एक बार छूकर संगीत के साथ गिनते हैं।",
                    odia = "କିଛି କଥା ନାହିଁ! ଚାଲ ଧୀରେ ଧୀରେ ପ୍ରତି ଦୀପକୁ ଥରେ ଛୁଇଁ ସଙ୍ଗୀତ ସହିତ ଗଣିବା।"
                ),
                successPraiseTemplate = MultilingualText(
                    english = "Lovely, {name}! Two glowing lamps plus one more makes three bright lamps!",
                    hindi = "शाबाश, {name}! दो चमकते दीयों में एक और मिलाया तो तीन दीये हो गए!",
                    odia = "ବାଃ {name}! ଦୁଇଟି ଜଳୁଥିବା ଦୀପ ସହିତ ଆଉ ଗୋଟିଏ ମିଶି ତିନୋଟି ଦୀପ ହେଲା!"
                ),
                alternativeMethod = TeachingMethod.REAL_OBJECT
            ),
            StoryScene(
                id = "lamps_guided_tap",
                beatTag = "2. Guided Tap & Musical Note",
                visualType = SceneVisualType.LAMPS_TAP_COUNTING_GUIDED,
                interactionType = SceneInteractionType.TAP_EACH_LAMP_GUIDED,
                characterLine = MultilingualText(
                    english = "Dadi: \"Tap each lamp gently once, and listen to its little chime!\"",
                    hindi = "दादी: \"हर दीये को प्यार से सिर्फ एक बार छुओ और उसकी मीठी धुन सुनो!\"",
                    odia = "ଜେଜେମା: \"ପ୍ରତି ଦୀପକୁ ଧୀରେ ଥରେ ଛୁଅଁ ଏବଂ ତାହାର ମିଠା ସ୍ୱର ଶୁଣ!\""
                ),
                teacherNarration = MultilingualText(
                    english = "Whenever you touch one lamp, it sings one soft note. One note for one lamp—never count the same lamp twice!",
                    hindi = "जब तुम एक दीये को छुओगी, तो एक मीठा सुर बजेगा। एक दीया, एक सुर—किसी दीये को दो बार मत गिनना!",
                    odia = "ତୁମେ ଗୋଟିଏ ଦୀପ ଛୁଇଁଲେ ଗୋଟିଏ ମିଠା ସ୍ୱର ବାଜିବ। ଗୋଟିଏ ଦୀପ, ଗୋଟିଏ ସ୍ୱର—କୌଣସି ଦୀପକୁ ଦୁଇଥର ଗଣିବ ନାହିଁ!"
                ),
                pauseQuestion = MultilingualText(
                    english = "{name}, tap each of the lamps on the step once to hear all 3 notes!",
                    hindi = "{name}, सीढ़ी के हर दीये को एक-एक बार दबाओ और तीनों सुर सुनो!",
                    odia = "{name}, ପାହାଚର ପ୍ରତି ଦୀପକୁ ଥରେ ଲେଖାଏଁ ଦବାଇ ୩ଟି ସ୍ୱର ଶୁଣ!"
                ),
                expectedNumber = 3,
                successPraiseTemplate = MultilingualText(
                    english = "Wonderful! Every lamp sang once: 1, 2, 3!",
                    hindi = "बहुत सुंदर! हर दीया एक बार बोला: 1, 2, 3!",
                    odia = "ବହୁତ ସୁନ୍ଦର! ପ୍ରତି ଦୀପ ଥରେ ବାଜିଲା: ୧, ୨, ୩!"
                ),
                alternativeMethod = TeachingMethod.ACT_IT_OUT
            ),
            StoryScene(
                id = "lamps_rearranged_check",
                beatTag = "3. Rearranged Independent Check",
                visualType = SceneVisualType.LAMPS_REARRANGED_CHECK,
                interactionType = SceneInteractionType.NUMERIC_OR_SPEECH_ANSWER,
                characterLine = MultilingualText(
                    english = "Granddaughter: \"Ooh! Dadi moved the lamps into a triangle! Did the number change?\"",
                    hindi = "पोती: \"अरे! दादी ने दीयों को तिकोने में सजा दिया! क्या गिनती बदल गई?\"",
                    odia = "ନାତୁଣୀ: \"ଆରେ! ଜେଜେମା ଦୀପଗୁଡ଼ିକୁ ତ୍ରିକୋଣ ଆକାରରେ ସଜାଇ ଦେଲେ! ସଂଖ୍ୟା ବଦଳିଗଲା କି?\""
                ),
                teacherNarration = MultilingualText(
                    english = "Look closely, {name}. Dadi moved the exact same lamps into a brand-new shape—nobody added or took away any lamp. Count carefully on your own!",
                    hindi = "ध्यान से देखो, {name}। दादी ने उन्हीं दीयों को नए आकार में रखा है—न कोई जोड़ा, न कोई हटाया। अब खुद गिनकर बताओ!",
                    odia = "ଭଲ ଭାବେ ଦେଖ, {name}। ଜେଜେମା ସେହି ଦୀପଗୁଡ଼ିକୁ ନୂଆ ଆକାରରେ ରଖିଛନ୍ତି—କୌଣସି ନୂଆ ଯୋଡ଼ା ହୋଇନି କି କମା ହୋଇନି। ଏବେ ନିଜେ ଗଣି କୁହ!"
                ),
                pauseQuestion = MultilingualText(
                    english = "Without any hints, how many lamps are glowing in this new arrangement?",
                    hindi = "बिना मदद के बताओ, इस नई सजावट में कितने दीये जल रहे हैं?",
                    odia = "ବିନା ସାହାଯ୍ୟରେ କୁହ, ଏହି ନୂଆ ସଜାରେ କେତୋଟି ଦୀପ ଜଳୁଛି?"
                ),
                expectedKeywords = listOf("3", "three", "तीन", "teen", "ତିନି", "tini"),
                expectedNumber = 3,
                choices = listOf(
                    StoryChoice("r2", MultilingualText("2 lamps", "2 दीये", "୨ଟି ଦୀପ"), false, "Thought moving lamps changed the count"),
                    StoryChoice("r3", MultilingualText("Still 3 lamps!", "अभी भी 3 दीये!", "ଏବେ ବି ୩ଟି ଦୀପ!"), true),
                    StoryChoice("r5", MultilingualText("5 lamps", "5 दीये", "୫ଟି ଦୀପ"), false, "Overcounted rearranged shape")
                ),
                memoryLine = MultilingualText(
                    english = "One lamp, one tap, count each once—moving them keeps the same three!",
                    hindi = "एक-एक दीप गिनें, हर दीप को एक बार गिनें।",
                    odia = "ଗୋଟିଏ ଗୋଟିଏ ଦୀପ ଗଣିବା, ପ୍ରତି ଦୀପକୁ ଥରେ ଗଣିବା।"
                ),
                successPraiseTemplate = MultilingualText(
                    english = "Wow, {name}! You counted each lamp once, even after I moved them.",
                    hindi = "वाह, {name}! तुमने जगह बदलने के बाद भी हर दीये को ठीक एक बार गिना!",
                    odia = "ବାଃ {name}! ସ୍ଥାନ ବଦଳିଲା ପରେ ବି ତୁମେ ପ୍ରତି ଦୀପକୁ ଠିକ୍ ଥରେ ଗଣିଲ!"
                ),
                alternativeMethod = TeachingMethod.COMPARE_EXAMPLES
            ),
            StoryScene(
                id = "lamps_next_day_recall",
                beatTag = "4. Next-Day Fresh Object Check",
                visualType = SceneVisualType.LAMPS_NEXT_DAY_MARIGOLDS,
                interactionType = SceneInteractionType.NUMERIC_OR_SPEECH_ANSWER,
                isNextDayRecallScene = true,
                characterLine = MultilingualText(
                    english = "Next Morning • Courtyard Basket: \"Here are fresh red marigold flowers—no lamps today!\"",
                    hindi = "अगली सुबह • आँगन की टोकरी: \"आज दीये नहीं, ताजे लाल गेंदे के फूल हैं!\"",
                    odia = "ଆର ଦିନ ସକାଳ • ଅଗଣା ଟୋକେଇ: \"ଆଜି ଦୀପ ନୁହେଁ, ତାଜା ନାଲି ଗେଣ୍ଡୁ ଫୁଲ ଅଛି!\""
                ),
                teacherNarration = MultilingualText(
                    english = "Good morning, {name}! Remember yesterday? Today we have fresh marigold flowers scattered in the basket. No rhyme cue this time—show me you still remember how to count!",
                    hindi = "सुप्रभात, {name}! कल की बात याद है? आज टोकरी में गेंदे के फूल रखे हैं। बिना गाने के खुद गिनकर दिखाओ!",
                    odia = "ଶୁଭ ସକାଳ, {name}! ଗତକାଲିର କଥା ମନେ ଅଛି? ଆଜି ଟୋକେଇରେ ଗେଣ୍ଡୁ ଫୁଲ ଅଛି। ବିନା ଗୀତରେ ନିଜେ ଗଣି ଦେଖାଅ!"
                ),
                pauseQuestion = MultilingualText(
                    english = "How many red marigold flowers are on the mat?",
                    hindi = "चटाई पर कुल कितने लाल गेंदे के फूल हैं?",
                    odia = "ମସିଣାରେ କେତୋଟି ନାଲି ଗେଣ୍ଡୁ ଫୁଲ ଅଛି?"
                ),
                expectedKeywords = listOf("3", "three", "तीन", "teen", "ତିନି", "tini"),
                expectedNumber = 3,
                choices = listOf(
                    StoryChoice("m2", MultilingualText("2 flowers", "2 फूल", "୨ଟି ଫୁଲ"), false, "Missed one flower"),
                    StoryChoice("m3", MultilingualText("3 flowers", "3 फूल", "୩ଟି ଫୁଲ"), true),
                    StoryChoice("m4", MultilingualText("4 flowers", "4 फूल", "୪ଟି ଫୁଲ"), false, "Double counted a flower")
                ),
                successPraiseTemplate = MultilingualText(
                    english = "Bravo, {name}! Yesterday's lamps, today's flowers—you mastered one-to-one counting!",
                    hindi = "शाबाश, {name}! कल दीये और आज फूल—तुमने एक-एक गिनती पक्की सीख ली!",
                    odia = "ଶାବାସ୍ {name}! ଗତକାଲି ଦୀପ ଆଉ ଆଜି ଫୁଲ—ତୁମେ ଗଣନା ପକ୍କା ଶିଖିଗଲ!"
                ),
                alternativeMethod = TeachingMethod.REAL_OBJECT
            )
        )
    )

    val lessonDayDropCameHome = StoryLesson(
        id = LESSON_DROP_ID,
        title = MultilingualText(
            english = "The Day a Drop Came Home",
            hindi = "जिस दिन एक बूँद घर लौटी",
            odia = "ଯେଉଁ ଦିନ ଗୋଟିଏ ବୁନ୍ଦା ଘରକୁ ଫେରିଲା"
        ),
        subtitle = MultilingualText(
            english = "Class 4 Science • Evaporation, Condensation & Water Cycle",
            hindi = "कक्षा 4 विज्ञान • वाष्पीकरण, संघनन और जल चक्र",
            odia = "ଶ୍ରେଣୀ ୪ ବିଜ୍ଞାନ • ବାଷ୍ପୀଭବନ, ଘନୀଭବନ ଓ ଜଳଚକ୍ର"
        ),
        targetClass = "Class 4",
        subject = "Science",
        conceptId = "water_cycle_evaporation",
        conceptTitle = "Evaporation, Condensation & Precipitation",
        recommendedStyle = StoryStyle.FUTURE_CLASSROOM,
        readiness = LessonReadiness.FULL_STORY_READY,
        heroDrawableResId = R.drawable.img_lesson_drop_jeypore,
        offlineAvailable = true,
        scenes = listOf(
            StoryScene(
                id = "drop_jeypore_hook",
                beatTag = "1. Jeypore Puddle Mystery",
                visualType = SceneVisualType.JEYPORE_PUDDLE_HOOK,
                interactionType = SceneInteractionType.CHOICE_OR_EXPLAIN,
                drawableResId = R.drawable.img_lesson_drop_jeypore,
                characterLine = MultilingualText(
                    english = "Ananya: \"Look Kabir! Our morning rain puddle on the stone courtyard is almost gone!\"  Kabir: \"Maybe the ground drank all the water!\"",
                    hindi = "अनन्या: \"देखो कबीर! पत्थर के आँगन वाला बारिश का गड्ढा लगभग सूख गया!\"  कबीर: \"शायद ज़मीन ने सारा पानी पी लिया!\"",
                    odia = "ଅନନ୍ୟା: \"ଦେଖ କବିର! ପଥର ଅଗଣାର ବର୍ଷା ପାଣି ଜମିଥିବା ଗାତ ପ୍ରାୟ ଶୁଖିଗଲାଣି!\"  କବିର: \"ବୋଧହୁଏ ମାଟି ସବୁ ପାଣି ପିଇଦେଲା!\""
                ),
                teacherNarration = MultilingualText(
                    english = "Welcome to Jeypore right after a morning shower! Two Class 4 friends see their puddle shrinking under the warm sun. Kabir guesses the stone ground drank it all. Let us test that!",
                    hindi = "बारिश के बाद जयपुर की सुबह में स्वागत है! कक्षा 4 के दो दोस्त आँगन का पानी घटते देखते हैं। कबीर सोचता है कि ज़मीन सारा पानी पी गई। चलो इसे जाँचते हैं!",
                    odia = "ବର୍ଷା ପରେ ଜୟପୁରର ସୁନ୍ଦର ସକାଳକୁ ସ୍ୱାଗତ! ଚତୁର୍ଥ ଶ୍ରେଣୀର ଦୁଇ ବନ୍ଧୁ ଅଗଣାର ପାଣି କମିଯାଉଥିବା ଦେଖୁଛନ୍ତି। କବିର ଭାବୁଛି ମାଟି ସବୁ ପାଣି ପିଇଦେଲା। ଚାଲ ପରୀକ୍ଷା କରିବା!"
                ),
                pauseQuestion = MultilingualText(
                    english = "{name}, how could you test Kabir's idea using water in a metal plate?",
                    hindi = "{name}, एक धातु की थाली में पानी लेकर तुम कबीर की बात कैसे जाँचोगे?",
                    odia = "{name}, ଗୋଟିଏ ଧାତୁ ଥାଳିରେ ପାଣି ନେଇ ତୁମେ କବିରଙ୍କ କଥା କିପରି ପରୀକ୍ଷା କରିବ?"
                ),
                expectedKeywords = listOf("plate", "sun", "air", "evaporat", "thali", "metal", "शोष", "थाली", "धूप", "ଥାଳି", "ଖରା", "ବାଷ୍ପ"),
                choices = listOf(
                    StoryChoice(
                        "p_drink",
                        MultilingualText("Metal plates also drink water from underneath", "धातु की थाली भी पानी पी लेती है", "ଧାତୁ ଥାଳି ମଧ୍ୟ ତଳୁ ପାଣି ପିଇଯାଏ"),
                        false,
                        "Believed non-porous metal absorbs water"
                    ),
                    StoryChoice(
                        "p_sun",
                        MultilingualText("Put water in a metal plate in warm sunlight—if it disappears without leaking, it went into the air!", "धूप में धातु की थाली में पानी रखें—बिना रिसे पानी घटे तो वह हवा में गया!", "ଖରାରେ ଧାତୁ ଥାଳିରେ ପାଣି ରଖିବା—ନ ଗଳି କମିଗଲେ ପାଣି ବାୟୁକୁ ଉଡ଼ିଗଲା!"),
                        true
                    ),
                    StoryChoice(
                        "p_hide",
                        MultilingualText("Pour the water out so we don't see it", "पानी बाहर फेंक दें", "ପାଣିକୁ ବାହାରେ ଢାଳି ଦେବା"),
                        false,
                        "Did not design a fair test"
                    )
                ),
                adaptiveRetryPrompt = MultilingualText(
                    english = "Let us compare two plates side by side: a metal plate cannot drink water like soil does! Compare a covered plate and an open sunny plate.",
                    hindi = "चलो दो थालियों की तुलना करते हैं: धातु की थाली मिट्टी की तरह पानी नहीं सोखती! ढकी थाली और खुली धूप वाली थाली देखो।",
                    odia = "ଚାଲ ଦୁଇଟି ଥାଳିକୁ ତୁଳନା କରିବା: ଧାତୁ ଥାଳି ମାଟି ପରି ପାଣି ଶୋଷେ ନାହିଁ! ଘୋଡ଼ା ଥାଳି ଓ ଖୋଲା ଖରା ଥାଳି ଦେଖ।"
                ),
                successPraiseTemplate = MultilingualText(
                    english = "Sharp thinking, {name}! Metal cannot absorb water, so the vanishing water proved it rose into the air!",
                    hindi = "शानदार सोच, {name}! धातु पानी नहीं सोखती, यानी पानी हवा में ऊपर उठा है!",
                    odia = "ଚମତ୍କାର ଚିନ୍ତାଧାରା, {name}! ଧାତୁ ପାଣି ଶୋଷେ ନାହିଁ, ତେଣୁ ପାଣି ବାୟୁକୁ ଉଠିଗଲା!"
                ),
                alternativeMethod = TeachingMethod.COMPARE_EXAMPLES
            ),
            StoryScene(
                id = "drop_plate_experiment",
                beatTag = "2. Metal Plate Comparison",
                visualType = SceneVisualType.METAL_PLATE_EXPERIMENT,
                interactionType = SceneInteractionType.CONTINUE_ONLY,
                characterLine = MultilingualText(
                    english = "Kabir: \"Aha! The covered steel plate still has all its water, while the sunny open plate shrank—not a single drop leaked through the metal!\"",
                    hindi = "कबीर: \"अरे वाह! ढकी स्टील की थाली में पूरा पानी है, पर खुली धूप वाली थाली का पानी कम हो गया—नीचे एक बूँद भी नहीं टपकी!\"",
                    odia = "କବିର: \"ଆହା! ଘୋଡ଼ା ଷ୍ଟିଲ ଥାଳିରେ ସବୁ ପାଣି ଅଛି, କିନ୍ତୁ ଖୋଲା ଖରା ଥାଳିର ପାଣି କମିଗଲା—ଧାତୁ ଭିତରୁ ଟୋପାଏ ବି ଗଳିନି!\""
                ),
                teacherNarration = MultilingualText(
                    english = "What a clean experiment! Warmth from the sun turns liquid water into invisible water vapour that floats upward into the air. Remember: real water vapour is completely invisible!",
                    hindi = "कितना बढ़िया प्रयोग! सूरज की गर्मी पानी को अदृश्य जलवाष्प (भाप) में बदलकर हवा में ऊपर उठाती है। याद रखना: असली जलवाष्प बिल्कुल अदृश्य होती है!",
                    odia = "କେତେ ସୁନ୍ଦର ପରୀକ୍ଷା! ସୂର୍ଯ୍ୟଙ୍କ ତାପ ପାଣିକୁ ଅଦୃଶ୍ୟ ଜଳବାଷ୍ପରେ ପରିଣତ କରି ବାୟୁରେ ଉପରକୁ ଉଠାଏ। ମନେରଖ: ପ୍ରକୃତ ଜଳବାଷ୍ପ ସମ୍ପୂର୍ଣ୍ଣ ଅଦୃଶ୍ୟ!"
                ),
                alternativeMethod = TeachingMethod.REAL_OBJECT
            ),
            StoryScene(
                id = "drop_hologram_cycle",
                beatTag = "3. Holographic Sky Journey",
                visualType = SceneVisualType.HOLOGRAPHIC_WATER_CYCLE,
                interactionType = SceneInteractionType.CHOICE_OR_EXPLAIN,
                drawableResId = R.drawable.img_hero_classroom,
                characterLine = MultilingualText(
                    english = "Holographic Board: \"Evaporation (invisible vapour) -> Condensation (visible cloud droplets) -> Precipitation (rain returns home!)\"",
                    hindi = "होलोग्राफिक बोर्ड: \"वाष्पीकरण (अदृश्य वाष्प) -> संघनन (दिखने वाली बादल बूँदें) -> वर्षण (बारिश की घर वापसी!)\"",
                    odia = "ହୋଲୋଗ୍ରାଫିକ ବୋର୍ଡ: \"ବାଷ୍ପୀଭବନ (ଅଦୃଶ୍ୟ ବାଷ୍ପ) -> ଘନୀଭବନ (ଦୃଶ୍ୟମାନ ମେଘ ବୁନ୍ଦା) -> ବର୍ଷଣ (ବର୍ଷା ହୋଇ ଘରକୁ ଫେରିବା!)\""
                ),
                teacherNarration = MultilingualText(
                    english = "Step inside our Future Classroom hologram! Follow the giant droplet upward as invisible vapour. High in the chilly sky, it cools and condenses into millions of tiny visible water droplets—that is a cloud! When droplets grow heavy, gravity brings them home as rain.",
                    hindi = "हमारे फ्यूचर क्लासरूम होलोग्राम में देखो! बूँद अदृश्य वाष्प बनकर ऊपर जाती है। ठंडे आसमान में वह संघनित होकर करोड़ों छोटी दिखने वाली बूँदों का बादल बनाती है! भारी होने पर बारिश बनकर घर लौटती है।",
                    odia = "ଆମ ଫ୍ୟୁଚର କ୍ଲାସରୁମ ହୋଲୋଗ୍ରାମ ଭିତରକୁ ଆସ! ବୁନ୍ଦା ଅଦୃଶ୍ୟ ବାଷ୍ପ ହୋଇ ଉପରକୁ ଉଠେ। ଥଣ୍ଡା ଆକାଶରେ ତାହା ଘନୀଭୂତ ହୋଇ ଲକ୍ଷ ଲକ୍ଷ ଦୃଶ୍ୟମାନ କ୍ଷୁଦ୍ର ଜଳକଣାର ମେଘ ଗଢ଼େ! ଭାରି ହେଲେ ବର୍ଷା ହୋଇ ଘରକୁ ଫେରେ।"
                ),
                pauseQuestion = MultilingualText(
                    english = "Quick check, {name}: Are clouds made of invisible gas vapour, or tiny visible liquid droplets?",
                    hindi = "जल्दी बताओ {name}: क्या बादल अदृश्य गैस वाष्प के बने होते हैं, या दिखने वाली नन्ही पानी की बूँदों के?",
                    odia = "ଶୀଘ୍ର କୁହ {name}: ମେଘ କ’ଣ ଅଦୃଶ୍ୟ ଗ୍ୟାସ ବାଷ୍ପରେ ଗଢ଼ା, ନା ଦୃଶ୍ୟମାନ କ୍ଷୁଦ୍ର ପାଣି ବୁନ୍ଦାରେ?"
                ),
                expectedKeywords = listOf("droplet", "visible", "liquid", "condens", "बूँद", "दिख", "बूंद", "ବୁନ୍ଦା", "ଦୃଶ୍ୟ"),
                choices = listOf(
                    StoryChoice("v_gas", MultilingualText("Invisible gas vapour only", "सिर्फ अदृश्य गैस भाप", "କେବଳ ଅଦୃଶ୍ୟ ଗ୍ୟାସ ବାଷ୍ପ"), false, "Confused invisible water vapour with visible cloud droplets"),
                    StoryChoice("v_drop", MultilingualText("Tiny visible water droplets formed by cooling (condensation)", "ठंडा होकर बनी नन्ही दिखने वाली पानी की बूँदें (संघनन)", "ଥଣ୍ଡା ହୋଇ ଗଢ଼ି ଉଠିଥିବା ଦୃଶ୍ୟମାନ କ୍ଷୁଦ୍ର ଜଳକଣା (ଘନୀଭବନ)"), true)
                ),
                memoryLine = MultilingualText(
                    english = "Warmth lifts up, Coolness gathers, Gravity brings the drop back home!",
                    hindi = "गर्मी से भाप उड़े, ठंडक से बादल बने, बूँद फिर घर लौटे!",
                    odia = "ତାପରେ ବାଷ୍ପ ଉଠେ, ଶୀତଳରେ ମେଘ ଗଢ଼େ, ବର୍ଷା ହୋଇ ଘରକୁ ଫେରେ!"
                ),
                successPraiseTemplate = MultilingualText(
                    english = "Spot on, {name}! Water vapour is invisible, while clouds are visible tiny droplets cooled in the sky!",
                    hindi = "बिल्कुल सही, {name}! जलवाष्प अदृश्य होती है, और बादल ठंडी होकर बनी दिखने वाली बूँदें हैं!",
                    odia = "ଏକଦମ ଠିକ୍, {name}! ଜଳବାଷ୍ପ ଅଦୃଶ୍ୟ, ଆଉ ମେଘ ହେଉଛି ଥଣ୍ଡା ହୋଇ ଗଢ଼ା ଦୃଶ୍ୟମାନ କ୍ଷୁଦ୍ର ଜଳକଣା!"
                ),
                alternativeMethod = TeachingMethod.DRAW_DIAGRAM
            ),
            StoryScene(
                id = "drop_wet_clothes_fresh",
                beatTag = "4. Fresh Problem: Wet Clothes",
                visualType = SceneVisualType.SUNNY_TERRACE_CLOTHES_CHECK,
                interactionType = SceneInteractionType.CHOICE_OR_EXPLAIN,
                characterLine = MultilingualText(
                    english = "Ananya: \"My school shirt dried in one hour on the sunny terrace, while Kabir's shirt inside the closed room is still damp!\"",
                    hindi = "अनन्या: \"धूप और हवा वाली छत पर मेरी स्कूल शर्ट एक घंटे में सूख गई, पर बंद कमरे में कबीर की शर्ट अभी भी गीली है!\"",
                    odia = "ଅନନ୍ୟା: \"ଖରା ଓ ପବନ ଥିବା ଛାତରେ ମୋ ସ୍କୁଲ ସାର୍ଟ ଗୋଟିଏ ଘଣ୍ଟାରେ ଶୁଖିଗଲା, କିନ୍ତୁ ବନ୍ଦ ଘରେ କବିରଙ୍କ ସାର୍ଟ ଏବେ ବି ଓଦା ଅଛି!\""
                ),
                teacherNarration = MultilingualText(
                    english = "Fresh everyday puzzle, {name}! Why do wet clothes dry much faster on a sunny, breezy terrace than inside a closed room?",
                    hindi = "नई रोज़मर्रा की पहेली, {name}! धूप और हवा वाली छत पर गीले कपड़े बंद कमरे से जल्दी क्यों सूखते हैं?",
                    odia = "ନୂଆ ଦୈନନ୍ଦିନ ପ୍ରଶ୍ନ, {name}! ଖରା ଓ ପବନ ଥିବା ଛାତରେ ଓଦା କପଡ଼ା ବନ୍ଦ ଘର ଅପେକ୍ଷା ଶୀଘ୍ର କାହିଁକି ଶୁଖେ?"
                ),
                pauseQuestion = MultilingualText(
                    english = "Explain in your own words or pick the best science reason!",
                    hindi = "अपने शब्दों में बताओ या सही वैज्ञानिक कारण चुनो!",
                    odia = "ନିଜ ଶବ୍ଦରେ କୁହ କିମ୍ବା ସଠିକ୍ ବିଜ୍ଞାନ କାରଣ ବାଛ!"
                ),
                expectedKeywords = listOf("heat", "sun", "wind", "evaporat", "air", "गर्मी", "हवा", "वाष्प", "ତାପ", "ପବନ", "ବାଷ୍ପ"),
                choices = listOf(
                    StoryChoice("w_sun", MultilingualText("Sunlight warmth & moving breeze speed up evaporation into vapour", "धूप की गर्मी और चलती हवा वाष्पीकरण को तेज़ करती है", "ସୂର୍ଯ୍ୟ ତାପ ଓ ପବନ ବାଷ୍ପୀଭବନକୁ ଦ୍ରୁତ କରେ"), true),
                    StoryChoice("w_room", MultilingualText("Closed rooms create extra rain inside", "बंद कमरे में बारिश होने लगती है", "ବନ୍ଦ ଘରେ ଅଧିକ ବର୍ଷା ହୁଏ"), false, "Misunderstood indoor humidity")
                ),
                successPraiseTemplate = MultilingualText(
                    english = "Brilliant, {name}! Heat + moving air speed up evaporation—just like the Jeypore puddle!",
                    hindi = "कमाल कर दिया, {name}! गर्मी और हवा मिलकर वाष्पीकरण को तेज़ करते हैं!",
                    odia = "ଚମତ୍କାର, {name}! ତାପ ଓ ପବନ ମିଶି ବାଷ୍ପୀଭବନକୁ ଶୀଘ୍ର କରନ୍ତି!"
                ),
                alternativeMethod = TeachingMethod.SOLVE_TOGETHER
            ),
            StoryScene(
                id = "drop_next_day_recall",
                beatTag = "5. Next-Day Recall (No Cue)",
                visualType = SceneVisualType.NEXT_DAY_BOWL_RECALL,
                interactionType = SceneInteractionType.FREE_SPEECH_OR_TYPE,
                isNextDayRecallScene = true,
                characterLine = MultilingualText(
                    english = "Next Day Check: \"A steel bowl of water sits on a sunny balcony all afternoon.\"",
                    hindi = "अगले दिन की जाँच: \"धूप वाली बालकनी में एक स्टील की कटोरी में पानी रखा है।\"",
                    odia = "ଆର ଦିନର ପରୀକ୍ଷା: \"ଖରା ପଡ଼ୁଥିବା ବାଲକୋନିରେ ଗୋଟିଏ ଷ୍ଟିଲ ଗିନାରେ ପାଣି ରଖାଯାଇଛି।\""
                ),
                teacherNarration = MultilingualText(
                    english = "Welcome back, {name}! Without our three-beat rhyme, explain in your own words: what happens to the water in that steel bowl on the sunny balcony, and how could it ever return to the ground later?",
                    hindi = "वापसी पर स्वागत है, {name}! बिना हमारी तीन-पंक्ति वाली कविता के, अपने शब्दों में बताओ: धूप वाली बालकनी में रखी स्टील की कटोरी के पानी का क्या होगा और वह वापस धरती पर कैसे आ सकता है?",
                    odia = "ସ୍ୱାଗତ {name}! ଆମ ଗୀତ ନ ଗାଇ ନିଜ ଶବ୍ଦରେ କୁହ: ଖରା ବାଲକୋନିର ଷ୍ଟିଲ ଗିନା ପାଣିର କ’ଣ ହେବ ଏବଂ ତାହା ପରେ ପୁଣି ମାଟିକୁ କିପରି ଫେରିପାରିବ?"
                ),
                pauseQuestion = MultilingualText(
                    english = "Explain evaporation & return as rain in your own words!",
                    hindi = "वाष्पीकरण और बारिश बनकर लौटने की कहानी अपने शब्दों में समझाओ!",
                    odia = "ବାଷ୍ପୀଭବନ ଏବଂ ବର୍ଷା ହୋଇ ଫେରିବା ନିଜ ଶବ୍ଦରେ ବୁଝାଅ!"
                ),
                expectedKeywords = listOf("evaporat", "vapour", "vapor", "cloud", "condens", "rain", "heat", "वाष्प", "भाप", "बादल", "बारिश", "ବାଷ୍ପ", "ମେଘ", "ବର୍ଷା"),
                choices = listOf(
                    StoryChoice("n_full", MultilingualText("Heat turns water into invisible vapour, cools into cloud droplets, and falls back as rain", "गर्मी से अदृश्य भाप बनती है, ठंडी होकर बादल की बूँदें और फिर बारिश बनती है", "ତାପରେ ଅଦୃଶ୍ୟ ବାଷ୍ପ ହୁଏ, ଥଣ୍ଡାରେ ମେଘ ବୁନ୍ଦା ହୋଇ ବର୍ଷା ରୂପେ ଫେରେ"), true),
                    StoryChoice("n_steel", MultilingualText("The steel bowl drinks half the water", "स्टील की कटोरी पानी सोख लेती है", "ଷ୍ଟିଲ ଗିନା ପାଣି ଶୋଷି ନିଏ"), false, "Reverted to absorption misconception")
                ),
                successPraiseTemplate = MultilingualText(
                    english = "Outstanding recall, {name}! You explained the whole water cycle without any hints!",
                    hindi = "लाजवाब याददाश्त, {name}! तुमने बिना किसी संकेत के पूरा जल-चक्र समझा दिया!",
                    odia = "ଅସାଧାରଣ ସ୍ମରଣଶକ୍ତି, {name}! ବିନା କୌଣସି ସଙ୍କେତରେ ତୁମେ ସମ୍ପୂର୍ଣ୍ଣ ଜଳଚକ୍ର ବୁଝାଇ ଦେଲ!"
                ),
                alternativeMethod = TeachingMethod.SHORTER_STORY
            )
        )
    )

    val additionalLessons: List<StoryLesson> = listOf(
        StoryLesson(
            id = "lkg_english_morning_greeting",
            title = MultilingualText("Sunny Village Morning", "सुनहरी सुबह और शब्द", "ସୁନେଲି ସକାଳ ଓ ଶବ୍ଦ"),
            subtitle = MultilingualText("LKG • English Phonics & Greetings", "एलकेजी • अंग्रेज़ी बोलचाल", "LKG • ଇଂରାଜୀ ଶବ୍ଦ ଓ ଶୁଭେଚ୍ଛା"),
            targetClass = "LKG",
            subject = "English",
            conceptId = "english_greetings_phonics",
            conceptTitle = "Polite Greetings & Initial Sounds",
            recommendedStyle = StoryStyle.EMOTIONAL_FAMILY,
            readiness = LessonReadiness.AI_GUIDED_READY,
            heroDrawableResId = R.drawable.img_hero_classroom,
            offlineAvailable = false,
            scenes = emptyList()
        ),
        StoryLesson(
            id = "class2_comic_sharing_laddoos",
            title = MultilingualText("Bhiku & Chintu Share Laddoos", "भीकू और चिंटू के लड्डू", "ଭୀକୁ ଓ ଚିଣ୍ଟୁଙ୍କ ଲଡ଼ୁ ଭାଗ"),
            subtitle = MultilingualText("Class 2 Mathematics • Equal Sharing", "कक्षा 2 गणित • बराबर बाँटना", "ଶ୍ରେଣୀ ୨ ଗଣିତ • ସମାନ ଭାଗ"),
            targetClass = "Class 2",
            subject = "Mathematics",
            conceptId = "equal_sharing_division",
            conceptTitle = "Equal Sharing & Fair Division",
            recommendedStyle = StoryStyle.COMIC_FRIENDSHIP,
            readiness = LessonReadiness.AI_GUIDED_READY,
            heroDrawableResId = R.drawable.img_hero_classroom,
            offlineAvailable = false,
            scenes = emptyList()
        ),
        StoryLesson(
            id = "class5_chase_friction_puzzle",
            title = MultilingualText("Mithu & Sheru on the Slippery Floor", "मिठू और शेरू की फिसलन पहेली", "ମିଠୁ ଓ ଶେରୁଙ୍କ ଘର୍ଷଣ ଖେଳ"),
            subtitle = MultilingualText("Class 5 Science • Friction & Force", "कक्षा 5 विज्ञान • घर्षण बल", "ଶ୍ରେଣୀ ୫ ବିଜ୍ଞାନ • ଘର୍ଷଣ ବଳ"),
            targetClass = "Class 5",
            subject = "Science",
            conceptId = "friction_cause_effect",
            conceptTitle = "Friction Slows Motion",
            recommendedStyle = StoryStyle.PLAYFUL_CHASE,
            readiness = LessonReadiness.AI_GUIDED_READY,
            heroDrawableResId = R.drawable.img_hero_classroom,
            offlineAvailable = false,
            scenes = emptyList()
        ),
        StoryLesson(
            id = "class6_epic_hanuman_leap",
            title = MultilingualText("Young Hanuman's Leap of Courage", "बाल हनुमान की साहसी छलाँग", "ବାଳ ହନୁମାନଙ୍କ ସାହସିକ ଲମ୍ଫ"),
            subtitle = MultilingualText("Class 6 Hindi/Odia • Courage & Vocabulary", "कक्षा 6 हिन्दी • साहस और शब्द-ज्ञान", "ଶ୍ରେଣୀ ୬ ଓଡ଼ିଆ • ସାହସ ଓ ଶବ୍ଦ ଜ୍ଞାନ"),
            targetClass = "Class 6",
            subject = "Hindi",
            conceptId = "moral_courage_metaphors",
            conceptTitle = "Courage & Expressive Metaphors",
            recommendedStyle = StoryStyle.INDIAN_EPIC,
            readiness = LessonReadiness.AI_GUIDED_READY,
            heroDrawableResId = R.drawable.img_hero_classroom,
            offlineAvailable = false,
            scenes = emptyList()
        ),
        StoryLesson(
            id = "class8_math_fractions_market",
            title = MultilingualText("Village Harvest Market Ratios", "गाँव की मंडी में अनुपात", "ଗାଁ ହାଟରେ ଅନୁପାତ ହିସାବ"),
            subtitle = MultilingualText("Class 8 Mathematics • Ratios & Proportions", "कक्षा 8 गणित • अनुपात और समानुपात", "ଶ୍ରେଣୀ ୮ ଗଣିତ • ଅନୁପାତ ଓ ସମାନୁପାତ"),
            targetClass = "Class 8",
            subject = "Mathematics",
            conceptId = "ratios_proportions",
            conceptTitle = "Ratios in Everyday Weighing",
            recommendedStyle = StoryStyle.HUMAN_ADVENTURE,
            readiness = LessonReadiness.AI_GUIDED_READY,
            heroDrawableResId = R.drawable.img_hero_classroom,
            offlineAvailable = false,
            scenes = emptyList()
        ),
        StoryLesson(
            id = "class10_science_circuits",
            title = MultilingualText("Holographic Village Microgrid", "होलोग्राफिक गाँव माइक्रोग्रिड", "ହୋଲୋଗ୍ରାଫିକ୍ ଗାଁ ବିଦ୍ୟୁତ୍ ଜ୍ଞାନ"),
            subtitle = MultilingualText("Class 10 Science • Ohm's Law & Circuits", "कक्षा 10 विज्ञान • ओम का नियम", "ଶ୍ରେଣୀ ୧୦ ବିଜ୍ଞାନ • ଓମ୍‌ଙ୍କ ନିୟମ"),
            targetClass = "Class 10",
            subject = "Science",
            conceptId = "ohms_law_circuits",
            conceptTitle = "Voltage, Current & Resistance",
            recommendedStyle = StoryStyle.FUTURE_CLASSROOM,
            readiness = LessonReadiness.AI_GUIDED_READY,
            heroDrawableResId = R.drawable.img_hero_classroom,
            offlineAvailable = false,
            scenes = emptyList()
        )
    )

    val allLessons: List<StoryLesson> =
        listOf(lessonThreeFestivalLamps, lessonDayDropCameHome) + additionalLessons

    fun lessonById(id: String): StoryLesson =
        allLessons.firstOrNull { it.id == id } ?: lessonThreeFestivalLamps

    fun firstAssessmentQuestion(profile: StudentProfile, language: AppLanguage): String {
        val name = profile.preferredName.ifBlank { "Friend" }
        return when {
            profile.studentClass.contains("LKG", true) || profile.studentClass.contains("UKG", true) ->
                when (language) {
                    AppLanguage.ENGLISH -> "$name, look around your room—can you find two red things and tell me what they are?"
                    AppLanguage.HINDI -> "$name, अपने आस-पास देखो—क्या तुम दो लाल चीज़ें ढूँढकर मुझे उनका नाम बता सकती हो?"
                    AppLanguage.ODIA -> "$name, ତୁମ ଆଖପାଖରେ ଦେଖ—ଦୁଇଟି ନାଲି ରଙ୍ଗର ଜିନିଷ ଖୋଜି ମୋତେ କହିପାରିବ କି?"
                }
            else ->
                when (language) {
                    AppLanguage.ENGLISH -> "$name, imagine you leave a wet steel plate of water in bright afternoon sun. Where do you think the water goes?"
                    AppLanguage.HINDI -> "$name, मान लो तुमने दोपहर की धूप में एक स्टील की प्लेट में पानी रखा। तुम्हें क्या लगता है पानी कहाँ जाएगा?"
                    AppLanguage.ODIA -> "$name, ଭାବ ତୁମେ ଖରା ବେଳେ ଗୋଟିଏ ଷ୍ଟିଲ ପ୍ଲେଟରେ ପାଣି ରଖିଲ। ସେହି ପାଣି କୁଆଡ଼େ ଯାଏ ବୋଲି ତୁମେ ଭାବୁଛ?"
                }
        }
    }

    fun weeklyTestForSubject(subject: String, studentClass: String): List<WeeklyTestQuestion> {
        return when (subject) {
            "Mathematics" -> listOf(
                WeeklyTestQuestion(
                    id = "wt_math_1",
                    subject = "Mathematics",
                    conceptId = "counting_one_to_one",
                    conceptTitle = "One-to-One Counting",
                    prompt = MultilingualText(
                        "Grandma placed 2 clay lamps on the step and added 1 more. Then she moved them into a circle. How many lamps are glowing?",
                        "दादी ने सीढ़ी पर 2 दीये रखे और 1 और जोड़ा, फिर उन्हें गोल सजा दिया। अब कितने दीये हैं?",
                        "ଜେଜେମା ପାହାଚରେ ୨ଟି ଦୀପ ରଖି ଆଉ ୧ଟି ଯୋଡ଼ିଲେ ଏବଂ ଗୋଲ ଆକାରରେ ସଜାଇଲେ। କେତୋଟି ଦୀପ ଅଛି?"
                    ),
                    options = listOf(
                        MultilingualText("2 lamps", "2 दीये", "୨ଟି ଦୀପ"),
                        MultilingualText("3 lamps", "3 दीये", "୩ଟି ଦୀପ"),
                        MultilingualText("4 lamps", "4 दीये", "୪ଟି ଦୀପ")
                    ),
                    correctIndex = 1,
                    hint = MultilingualText(
                        "Moving objects does not add or remove any!",
                        "जगह बदलने से गिनती नहीं बदलती!",
                        "ସ୍ଥାନ ବଦଳିଲେ ସଂଖ୍ୟା ବଦଳେ ନାହିଁ!"
                    )
                ),
                WeeklyTestQuestion(
                    id = "wt_math_2",
                    subject = "Mathematics",
                    conceptId = "counting_one_to_one",
                    conceptTitle = "Count Each Object Once",
                    prompt = MultilingualText(
                        "You count 4 red flowers on a mat. Which rule keeps your count accurate?",
                        "चटाई पर 4 लाल फूल गिनते समय कौन-सा नियम सही है?",
                        "ମସିଣାରେ ୪ଟି ନାଲି ଫୁଲ ଗଣିଲା ବେଳେ କେଉଁ ନିୟମ ଠିକ୍?"
                    ),
                    options = listOf(
                        MultilingualText("Touch and count each flower exactly once", "हर फूल को ठीक एक बार छूकर गिनें", "ପ୍ରତି ଫୁଲକୁ ଠିକ୍ ଥରେ ଛୁଇଁ ଗଣିବା"),
                        MultilingualText("Count the biggest flower twice", "सबसे बड़े फूल को दो बार गिनें", "ସବୁଠାରୁ ବଡ଼ ଫୁଲକୁ ଦୁଇଥର ଗଣିବା"),
                        MultilingualText("Guess without looking", "बिना देखे अंदाज़ा लगाएँ", "ନ ଦେଖି ଅନ୍ଦାଜ କରିବା")
                    ),
                    correctIndex = 0,
                    hint = MultilingualText(
                        "One object = one count!",
                        "एक वस्तु = एक गिनती!",
                        "ଗୋଟିଏ ଜିନିଷ = ଗୋଟିଏ ଗଣନା!"
                    )
                )
            )
            "Science", "EVS" -> listOf(
                WeeklyTestQuestion(
                    id = "wt_sci_1",
                    subject = subject,
                    conceptId = "water_cycle_evaporation",
                    conceptTitle = "Evaporation Proof",
                    prompt = MultilingualText(
                        "Water in a sunny metal plate shrank while water in a covered metal plate stayed full. Why?",
                        "धूप वाली खुली थाली का पानी कम हो गया, पर ढकी थाली का पूरा रहा। क्यों?",
                        "ଖୋଲା ଖରା ଥାଳିର ପାଣି କମିଗଲା, କିନ୍ତୁ ଘୋଡ଼ା ଥାଳିର ପାଣି ରହିଲା। କାହିଁକି?"
                    ),
                    options = listOf(
                        MultilingualText("The metal plate drank the water", "धातु की थाली ने पानी पी लिया", "ଧାତୁ ଥାଳି ପାଣି ପିଇଦେଲା"),
                        MultilingualText("Sun's warmth turned water into invisible water vapour in the air", "सूरज की गर्मी से पानी अदृश्य जलवाष्प बनकर हवा में गया", "ସୂର୍ଯ୍ୟ ତାପରେ ପାଣି ଅଦୃଶ୍ୟ ଜଳବାଷ୍ପ ହୋଇ ବାୟୁକୁ ଉଠିଗଲା"),
                        MultilingualText("The water turned into stone", "पानी पत्थर बन गया", "ପାଣି ପଥର ହୋଇଗଲା")
                    ),
                    correctIndex = 1,
                    hint = MultilingualText(
                        "Metal cannot absorb water—warmth lifts water as vapour!",
                        "धातु पानी नहीं सोखती—गर्मी पानी को भाप बनाती है!",
                        "ଧାତୁ ପାଣି ଶୋଷେ ନାହିଁ—ତାପ ପାଣିକୁ ବାଷ୍ପ କରେ!"
                    )
                ),
                WeeklyTestQuestion(
                    id = "wt_sci_2",
                    subject = subject,
                    conceptId = "water_cycle_evaporation",
                    conceptTitle = "Vapour vs Cloud Droplets",
                    prompt = MultilingualText(
                        "Which statement correctly tells water vapour apart from a cloud?",
                        "जलवाष्प और बादल के बीच सही अंतर कौन-सा है?",
                        "ଜଳବାଷ୍ପ ଓ ମେଘ ମଧ୍ୟରେ ସଠିକ୍ ପାର୍ଥକ୍ୟ କେଉଁଟି?"
                    ),
                    options = listOf(
                        MultilingualText("Water vapour is invisible gas; clouds are visible tiny liquid droplets", "जलवाष्प अदृश्य गैस है; बादल दिखने वाली नन्ही पानी की बूँदें हैं", "ଜଳବାଷ୍ପ ଅଦୃଶ୍ୟ ଗ୍ୟାସ; ମେଘ ଦୃଶ୍ୟମାନ କ୍ଷୁଦ୍ର ଜଳକଣା"),
                        MultilingualText("Clouds are dry smoke", "बादल सूखा धुआँ हैं", "ମେଘ ଶୁଖିଲା ଧୂଆଁ"),
                        MultilingualText("Water vapour is blue ice", "जलवाष्प नीली बर्फ है", "ଜଳବାଷ୍ପ ନୀଳ ବରଫ")
                    ),
                    correctIndex = 0,
                    hint = MultilingualText(
                        "You cannot see water vapour rising from a puddle, but you can see clouds!",
                        "गड्ढे से उठती भाप नहीं दिखती, पर बादल दिखते हैं!",
                        "ଗାତରୁ ଉଠୁଥିବା ବାଷ୍ପ ଦିଶେ ନାହିଁ, କିନ୍ତୁ ମେଘ ଦିଶେ!"
                    )
                )
            )
            else -> listOf(
                WeeklyTestQuestion(
                    id = "wt_gen_${subject.lowercase().replace(" ", "_")}_1",
                    subject = subject,
                    conceptId = "${subject.lowercase()}_core_check",
                    conceptTitle = "$subject Core Understanding ($studentClass)",
                    prompt = MultilingualText(
                        "In $subject ($studentClass), what helps us remember a new idea best for tomorrow?",
                        "$subject ($studentClass) में कोई नई बात कल तक याद रखने का सबसे अच्छा तरीका क्या है?",
                        "$subject ($studentClass) ରେ ନୂଆ କଥା ଆର ଦିନ ପର୍ଯ୍ୟନ୍ତ ମନେ ରଖିବାର ସବୁଠାରୁ ଭଲ ଉପାୟ କ’ଣ?"
                    ),
                    options = listOf(
                        MultilingualText("Explain it in our own words and try a fresh example", "अपने शब्दों में समझाना और एक नया उदाहरण हल करना", "ନିଜ ଶବ୍ଦରେ ବୁଝାଇବା ଏବଂ ନୂଆ ଉଦାହରଣ କରିବା"),
                        MultilingualText("Repeat one sentence louder without thinking", "बिना सोचे एक ही वाक्य ज़ोर से दोहराना", "ନ ଭାବି ଗୋଟିଏ ବାକ୍ୟ ଜୋରରେ କହିବା"),
                        MultilingualText("Skip checking our work", "जाँच करना छोड़ देना", "ଯାଞ୍ଚ ନ କରି ଛାଡ଼ିଦେବା")
                    ),
                    correctIndex = 0,
                    hint = MultilingualText(
                        "Retelling in your own words builds lasting understanding!",
                        "अपने शब्दों में दोहराने से समझ पक्की होती है!",
                        "ନିଜ ଶବ୍ଦରେ ବୁଝାଇଲେ ବୁଝାମଣା ପକ୍କା ହୁଏ!"
                    )
                )
            )
        }
    }
}
