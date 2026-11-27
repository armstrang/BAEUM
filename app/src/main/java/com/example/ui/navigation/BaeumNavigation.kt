package com.example.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.CurriculumPathway
import com.example.data.local.RomanizationMode
import com.example.data.seed.ExamType
import com.example.data.seed.SpeakingDifficulty
import com.example.domain.curriculum.BeginnerPracticeAction
import com.example.domain.curriculum.BeginnerSyllabusCatalog
import com.example.domain.curriculum.SyllabusTrack
import com.example.domain.exam.ExamSessionMode
import com.example.domain.fsrs.FsrsRating
import com.example.domain.quiz.QuizMode
import com.example.ui.components.GlobalCelebrationToast
import com.example.ui.screens.ActiveExamScreen
import com.example.ui.screens.ActiveQuizScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BeginnerLessonScreen
import com.example.ui.screens.BeginnerSyllabusScreen
import com.example.ui.screens.DifficultySelectionScreen
import com.example.ui.screens.EpsTopikCenterScreen
import com.example.ui.screens.EverydayKoreanScreen
import com.example.ui.screens.ExamCenterScreen
import com.example.ui.screens.ExamReportScreen
import com.example.ui.screens.GrammarStudioScreen
import com.example.ui.screens.HangulStudioScreen
import com.example.ui.screens.HangulWritingScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.LearningHubScreen
import com.example.ui.screens.LiveConversationScreen
import com.example.ui.screens.MistakeRedemptionScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PronunciationLabScreen
import com.example.ui.screens.QuizCenterScreen
import com.example.ui.screens.ReviewPlannerScreen
import com.example.ui.screens.SeoulBlitzScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartFlashcardScreen
import com.example.ui.screens.SpeakingAssessmentScreen
import com.example.ui.screens.SpeakingHistoryScreen
import com.example.ui.screens.SpeakingHubScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StageBossScreen
import com.example.ui.screens.TopikCenterScreen
import com.example.ui.screens.VocabularyLibraryScreen
import com.example.ui.screens.WordDetailScreen
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.LocalBaeumLang
import com.example.ui.theme.SeoulAmbientBackdrop
import com.example.ui.theme.bText
import com.example.ui.theme.tactileClickable
import com.example.viewmodel.BaeumViewModel
import com.example.viewmodel.FlashcardFsrsViewModel

object BaeumRoutes {
    const val HOME = "home_dashboard"
    const val LEARN = "learning_hub"
    const val SPEAK = "speaking_hub"
    const val EXAM = "exam_center"
    const val PROGRESS = "learning_analytics"

    const val BEGINNER_SYLLABUS = "beginner_syllabus?track={track}"
    const val BEGINNER_LESSON = "beginner_lesson/{lessonId}"
    const val STAGE_BOSS = "stage_boss/{stageNumber}"
    const val MISTAKE_REDEMPTION = "mistake_redemption"
    const val HANGUL_STUDIO = "hangul_studio"
    const val HANGUL_WRITING = "hangul_writing"
    const val TOPIK_CENTER = "topik_center"
    const val EPS_CENTER = "eps_center"
    const val EVERYDAY_KOREAN = "everyday_korean"
    const val VOCABULARY = "vocabulary_library?curriculum={curriculum}&sub={sub}"
    const val WORD_DETAIL = "word_detail/{wordId}"
    const val FLASHCARD = "smart_flashcard"
    const val REVIEW_PLANNER = "review_planner"
    const val QUIZ_CENTER = "quiz_center"
    const val ACTIVE_QUIZ = "active_quiz"
    const val SEOUL_BLITZ = "seoul_blitz"
    const val GRAMMAR_STUDIO = "grammar_studio"
    const val SPEAK_DIFFICULTY = "speak_difficulty"
    const val LIVE_CONVERSATION = "live_conversation"
    const val SPEAK_ASSESSMENT = "speak_assessment"
    const val PRONUNCIATION_LAB = "pronunciation_lab"
    const val SPEAK_HISTORY = "speak_history"
    const val ACTIVE_EXAM = "active_exam"
    const val EXAM_REPORT = "exam_report"
    const val SETTINGS = "settings"

    fun syllabusRoute(track: SyllabusTrack? = null): String {
        val t = Uri.encode(track?.name.orEmpty())
        return "beginner_syllabus?track=$t"
    }

    fun beginnerLessonRoute(lessonId: String): String = "beginner_lesson/${Uri.encode(lessonId)}"

    fun stageBossRoute(stageNumber: Int): String = "stage_boss/$stageNumber"

    fun vocabularyRoute(curriculum: String? = null, sub: String? = null): String {
        val c = Uri.encode(curriculum.orEmpty())
        val s = Uri.encode(sub.orEmpty())
        return "vocabulary_library?curriculum=$c&sub=$s"
    }

    fun wordDetailRoute(wordId: String): String = "word_detail/${Uri.encode(wordId)}"
}

private data class TopLevelTab(
    val route: String,
    val tagKey: String,
    val labelEn: String,
    val labelId: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val topLevelTabs = listOf(
    TopLevelTab(BaeumRoutes.HOME, "home", "Home", "Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    TopLevelTab(BaeumRoutes.LEARN, "learn", "Learn", "Belajar", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    TopLevelTab(BaeumRoutes.SPEAK, "speak", "Speak", "Bicara", Icons.Filled.Mic, Icons.Outlined.MicNone),
    TopLevelTab(BaeumRoutes.EXAM, "exam", "Exam", "Ujian", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment),
    TopLevelTab(BaeumRoutes.PROGRESS, "progress", "Progress", "Progres", Icons.Filled.BarChart, Icons.Outlined.BarChart)
)

@Composable
fun BaeumAppRoot(
    viewModel: BaeumViewModel,
    flashcardViewModel: FlashcardFsrsViewModel
) {
    val isInitialized by viewModel.isInitialized.collectAsStateWithLifecycle()
    val initError by viewModel.initError.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val extended = LocalBaeumColors.current

    CompositionLocalProvider(LocalBaeumLang provides settings.translationLanguage) {
        if (!isInitialized) {
            SeoulAmbientBackdrop {
                SplashScreen(
                    initError = initError,
                    onRetry = { viewModel.initializeApp() }
                )
            }
            return@CompositionLocalProvider
        }

        if (!settings.onboardingCompleted) {
            SeoulAmbientBackdrop {
                OnboardingScreen(
                    currentLang = settings.translationLanguage,
                    onSelectLang = { viewModel.setTranslationLanguage(it) },
                    onFinish = { goal, level, daily ->
                        viewModel.completeOnboarding(goal, level, daily)
                    }
                )
            }
            return@CompositionLocalProvider
        }

        BaeumMainShell(
            viewModel = viewModel,
            flashcardViewModel = flashcardViewModel,
            settings = settings,
            extended = extended
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BaeumMainShell(
    viewModel: BaeumViewModel,
    flashcardViewModel: FlashcardFsrsViewModel,
    settings: com.example.data.local.UserSettings,
    extended: com.example.ui.theme.BaeumExtendedColors
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination
    val topRoutes = remember { topLevelTabs.map { it.route }.toSet() }
    val showPrimaryNav = currentDest?.route in topRoutes

    val topSafeInsets = WindowInsets.statusBars
        .union(WindowInsets.statusBarsIgnoringVisibility)
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Top)

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val transitionMs = if (extended.reducedMotion) 0 else 340
    val tabTransitionMs = if (extended.reducedMotion) 0 else 260
    val tabIndexByRoute = remember { topLevelTabs.mapIndexed { idx, tab -> tab.route to idx }.toMap() }

    SeoulAmbientBackdrop {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 720.dp

            val navHostContent: @Composable (Modifier) -> Unit = { contentModifier ->
                Box(
                    modifier = contentModifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = BaeumRoutes.HOME,
                        enterTransition = {
                            val fromIdx = tabIndexByRoute[initialState.destination.route]
                            val toIdx = tabIndexByRoute[targetState.destination.route]
                            if (fromIdx != null && toIdx != null) {
                                val dir = if (toIdx >= fromIdx) 1 else -1
                                slideInHorizontally(
                                    initialOffsetX = { (it * 0.08f * dir).toInt() },
                                    animationSpec = tween(tabTransitionMs, easing = BaeumMotion.SilkEasing)
                                ) + fadeIn(tween(tabTransitionMs, easing = BaeumMotion.SilkEasing)) +
                                    scaleIn(
                                        initialScale = 0.986f,
                                        animationSpec = tween(tabTransitionMs, easing = BaeumMotion.SilkEasing)
                                    )
                            } else {
                                slideInHorizontally(
                                    initialOffsetX = { (it * 0.14f).toInt() },
                                    animationSpec = tween(transitionMs, easing = BaeumMotion.SilkEasing)
                                ) + fadeIn(tween(transitionMs, easing = BaeumMotion.SilkEasing)) +
                                    scaleIn(
                                        initialScale = 0.97f,
                                        animationSpec = tween(transitionMs, easing = BaeumMotion.SilkEasing)
                                    )
                            }
                        },
                        exitTransition = {
                            val fromIdx = tabIndexByRoute[initialState.destination.route]
                            val toIdx = tabIndexByRoute[targetState.destination.route]
                            if (fromIdx != null && toIdx != null) {
                                val dir = if (toIdx >= fromIdx) -1 else 1
                                slideOutHorizontally(
                                    targetOffsetX = { (it * 0.06f * dir).toInt() },
                                    animationSpec = tween((tabTransitionMs * 0.85f).toInt(), easing = BaeumMotion.SilkEasing)
                                ) + fadeOut(tween((tabTransitionMs * 0.65f).toInt()))
                            } else {
                                slideOutHorizontally(
                                    targetOffsetX = { -(it * 0.08f).toInt() },
                                    animationSpec = tween(transitionMs, easing = BaeumMotion.SilkEasing)
                                ) + fadeOut(tween((transitionMs * 0.72f).toInt())) +
                                    scaleOut(
                                        targetScale = 0.96f,
                                        animationSpec = tween(transitionMs, easing = BaeumMotion.SilkEasing)
                                    )
                            }
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -(it * 0.10f).toInt() },
                                animationSpec = tween(transitionMs, easing = BaeumMotion.SilkEasing)
                            ) + fadeIn(tween(transitionMs, easing = BaeumMotion.SilkEasing)) +
                                scaleIn(
                                    initialScale = 0.97f,
                                    animationSpec = tween(transitionMs, easing = BaeumMotion.SilkEasing)
                                )
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { (it * 0.16f).toInt() },
                                animationSpec = tween(transitionMs, easing = BaeumMotion.SilkEasing)
                            ) + fadeOut(tween((transitionMs * 0.72f).toInt())) +
                                scaleOut(
                                    targetScale = 0.96f,
                                    animationSpec = tween(transitionMs, easing = BaeumMotion.SilkEasing)
                                )
                        }
                    ) {
                        composable(BaeumRoutes.HOME) {
                            val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
                            val habitSnapshot by viewModel.habitSnapshot.collectAsStateWithLifecycle()
                            HomeDashboardScreen(
                                summary = summary,
                                habitSnapshot = habitSnapshot,
                                onOpenSettings = { navController.navigate(BaeumRoutes.SETTINGS) },
                                onResumeLearning = {
                                    flashcardViewModel.startSession(curriculumFilter = settings.activePathway, dueOnly = false)
                                    navController.navigate(BaeumRoutes.FLASHCARD)
                                },
                                onReviewDue = {
                                    val dueOnly = summary.dueTodayCount > 0
                                    flashcardViewModel.startSession(dueOnly = dueOnly)
                                    navController.navigate(BaeumRoutes.FLASHCARD)
                                },
                                onOpenReviewPlanner = { navController.navigate(BaeumRoutes.REVIEW_PLANNER) },
                                onOpenVocabulary = { navController.navigate(BaeumRoutes.vocabularyRoute(settings.activePathway, null)) },
                                onOpenFlashcards = {
                                    flashcardViewModel.startSession(curriculumFilter = settings.activePathway, dueOnly = false)
                                    navController.navigate(BaeumRoutes.FLASHCARD)
                                },
                                onOpenQuizCenter = { navController.navigate(BaeumRoutes.QUIZ_CENTER) },
                                onOpenGrammarStudio = { navController.navigate(BaeumRoutes.GRAMMAR_STUDIO) },
                                onStartSeoulBlitz = {
                                    viewModel.startSeoulBlitz()
                                    navController.navigate(BaeumRoutes.SEOUL_BLITZ)
                                },
                                onCompleteDailySpark = { isCorrect -> viewModel.completeDailySpark(isCorrect) },
                                onClaimDailySeal = { viewModel.claimDailySeal() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onQuestAction = { route ->
                                    when (route) {
                                        BaeumRoutes.FLASHCARD -> {
                                            flashcardViewModel.startSession(dueOnly = summary.dueTodayCount > 0)
                                            navController.navigate(BaeumRoutes.FLASHCARD)
                                        }
                                        BaeumRoutes.SEOUL_BLITZ -> {
                                            viewModel.startSeoulBlitz()
                                            navController.navigate(BaeumRoutes.SEOUL_BLITZ)
                                        }
                                        BaeumRoutes.SPEAK -> navigateTopTab(BaeumRoutes.SPEAK)
                                        else -> navController.navigate(BaeumRoutes.QUIZ_CENTER)
                                    }
                                },
                                onOpenBeginnerSyllabus = { navController.navigate(BaeumRoutes.syllabusRoute()) },
                                onOpenSyllabusTrack = { track -> navController.navigate(BaeumRoutes.syllabusRoute(track)) },
                                onOpenMistakeRedemption = { navController.navigate(BaeumRoutes.MISTAKE_REDEMPTION) },
                                onStartNextBeginnerLesson = { lessonId ->
                                    navController.navigate(BaeumRoutes.beginnerLessonRoute(lessonId))
                                }
                            )
                        }

                        composable(BaeumRoutes.LEARN) {
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            val habitSnapshot by viewModel.habitSnapshot.collectAsStateWithLifecycle()
                            LearningHubScreen(
                                allVocab = allVocab,
                                completedBeginnerLessons = habitSnapshot.completedBeginnerLessonIds,
                                onOpenSettings = { navController.navigate(BaeumRoutes.SETTINGS) },
                                onOpenBeginnerSyllabus = { navController.navigate(BaeumRoutes.syllabusRoute()) },
                                onOpenSyllabusTrack = { track -> navController.navigate(BaeumRoutes.syllabusRoute(track)) },
                                onSelectHangul = {
                                    viewModel.setActivePathway(CurriculumPathway.HANGUL)
                                    navController.navigate(BaeumRoutes.HANGUL_STUDIO)
                                },
                                onSelectTopik = {
                                    viewModel.setActivePathway(CurriculumPathway.TOPIK)
                                    navController.navigate(BaeumRoutes.TOPIK_CENTER)
                                },
                                onSelectEpsTopik = {
                                    viewModel.setActivePathway(CurriculumPathway.EPS_TOPIK)
                                    navController.navigate(BaeumRoutes.EPS_CENTER)
                                },
                                onSelectEveryday = {
                                    viewModel.setActivePathway(CurriculumPathway.EVERYDAY)
                                    navController.navigate(BaeumRoutes.EVERYDAY_KOREAN)
                                },
                                onOpenGrammarStudio = { navController.navigate(BaeumRoutes.GRAMMAR_STUDIO) },
                                onOpenQuizCenter = { navController.navigate(BaeumRoutes.QUIZ_CENTER) }
                            )
                        }

                        composable(BaeumRoutes.SPEAK) {
                            val speakingSessions by viewModel.speakingSessions.collectAsStateWithLifecycle()
                            SpeakingHubScreen(
                                historyCount = speakingSessions.size,
                                onOpenSettings = { navController.navigate(BaeumRoutes.SETTINGS) },
                                onStartConversationFlow = { navController.navigate(BaeumRoutes.SPEAK_DIFFICULTY) },
                                onOpenPronunciationLab = { navController.navigate(BaeumRoutes.PRONUNCIATION_LAB) },
                                onStartWorkplaceSpeaking = {
                                    viewModel.startAutoConversation(SpeakingDifficulty.INTERMEDIATE, categoryFilter = "Workplace")
                                    navController.navigate(BaeumRoutes.LIVE_CONVERSATION)
                                },
                                onStartTopikSpeaking = {
                                    viewModel.startAutoConversation(SpeakingDifficulty.INTERMEDIATE, categoryFilter = "TOPIK Speaking")
                                    navController.navigate(BaeumRoutes.LIVE_CONVERSATION)
                                },
                                onOpenSessionHistory = { navController.navigate(BaeumRoutes.SPEAK_HISTORY) }
                            )
                        }

                        composable(BaeumRoutes.EXAM) {
                            val examAttempts by viewModel.examAttempts.collectAsStateWithLifecycle()
                            ExamCenterScreen(
                                attempts = examAttempts,
                                onOpenSettings = { navController.navigate(BaeumRoutes.SETTINGS) },
                                onStartExam = { type, mode, length ->
                                    viewModel.startExam(type, mode, length)
                                    navController.navigate(BaeumRoutes.ACTIVE_EXAM)
                                }
                            )
                        }

                        composable(BaeumRoutes.PROGRESS) {
                            val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
                            val habitSnapshot by viewModel.habitSnapshot.collectAsStateWithLifecycle()
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
                            val speakingSessions by viewModel.speakingSessions.collectAsStateWithLifecycle()
                            val examAttempts by viewModel.examAttempts.collectAsStateWithLifecycle()
                            val readinessProfile by viewModel.readinessProfile.collectAsStateWithLifecycle()
                            AnalyticsScreen(
                                summary = summary,
                                allVocab = allVocab,
                                dailyStats = dailyStats,
                                speakingCount = speakingSessions.size,
                                examCount = examAttempts.size,
                                readinessProfile = readinessProfile,
                                habitSnapshot = habitSnapshot,
                                onOpenSettings = { navController.navigate(BaeumRoutes.SETTINGS) },
                                onRecommendedAction = {
                                    if (summary.dueTodayCount > 0) {
                                        flashcardViewModel.startSession(dueOnly = true)
                                        navController.navigate(BaeumRoutes.FLASHCARD)
                                    } else {
                                        navController.navigate(BaeumRoutes.QUIZ_CENTER)
                                    }
                                }
                            )
                        }

                        // ==================== 3-TRACK GUIDED SYLLABUS ====================
                        composable(
                            route = BaeumRoutes.BEGINNER_SYLLABUS,
                            arguments = listOf(
                                navArgument("track") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            )
                        ) { entry ->
                            val habitSnapshot by viewModel.habitSnapshot.collectAsStateWithLifecycle()
                            val trackArg = SyllabusTrack.fromKey(entry.arguments?.getString("track"))
                            BeginnerSyllabusScreen(
                                completedLessonIds = habitSnapshot.completedBeginnerLessonIds,
                                perfectLessonIds = habitSnapshot.perfectSyllabusLessonIds,
                                defeatedStageBossNumbers = habitSnapshot.defeatedStageBossNumbers,
                                mistakeVaultKeys = habitSnapshot.mistakeVaultKeys,
                                initialTrack = trackArg,
                                onBack = { navController.popBackStack() },
                                onOpenLesson = { lessonId ->
                                    navController.navigate(BaeumRoutes.beginnerLessonRoute(lessonId))
                                },
                                onOpenStageBoss = { stageNum ->
                                    navController.navigate(BaeumRoutes.stageBossRoute(stageNum))
                                },
                                onOpenMistakeRedemption = {
                                    navController.navigate(BaeumRoutes.MISTAKE_REDEMPTION)
                                }
                            )
                        }

                        composable(
                            route = BaeumRoutes.BEGINNER_LESSON,
                            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                        ) { entry ->
                            val habitSnapshot by viewModel.habitSnapshot.collectAsStateWithLifecycle()
                            val lessonId = entry.arguments?.getString("lessonId").orEmpty()
                            val lesson = remember(lessonId) { BeginnerSyllabusCatalog.lessonById(lessonId) }
                            BeginnerLessonScreen(
                                lesson = lesson,
                                isAlreadyCompleted = lessonId in habitSnapshot.completedBeginnerLessonIds,
                                isAlreadyPerfect = lessonId in habitSnapshot.perfectSyllabusLessonIds,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onCheckSound = { ok, step -> viewModel.playBeginnerCheckSound(ok, step) },
                                onLessonCompleted = { id, isPerfect -> viewModel.completeBeginnerLesson(id, isPerfect) },
                                onMistakeRecorded = { key -> viewModel.recordSyllabusMistake(key) },
                                onOpenNextLesson = { nextId ->
                                    navController.navigate(BaeumRoutes.beginnerLessonRoute(nextId)) {
                                        popUpTo(BaeumRoutes.BEGINNER_LESSON) { inclusive = true }
                                    }
                                },
                                onOpenNextTrack = { nextTrack ->
                                    navController.navigate(BaeumRoutes.syllabusRoute(nextTrack)) {
                                        popUpTo(BaeumRoutes.BEGINNER_LESSON) { inclusive = true }
                                    }
                                },
                                onOpenPracticeAction = { action ->
                                    when (action) {
                                        BeginnerPracticeAction.HANGUL_WRITING -> navController.navigate(BaeumRoutes.HANGUL_WRITING)
                                        BeginnerPracticeAction.HANGUL_STUDIO -> navController.navigate(BaeumRoutes.HANGUL_STUDIO)
                                        BeginnerPracticeAction.BEGINNER_FLASHCARDS -> {
                                            flashcardViewModel.startSession(curriculumFilter = CurriculumPathway.TOPIK, subCategoryFilter = "1급", dueOnly = false)
                                            navController.navigate(BaeumRoutes.FLASHCARD)
                                        }
                                        BeginnerPracticeAction.GRAMMAR_STUDIO -> navController.navigate(BaeumRoutes.GRAMMAR_STUDIO)
                                        BeginnerPracticeAction.EVERYDAY_SPEAKING -> {
                                            viewModel.startAutoConversation(SpeakingDifficulty.BEGINNER)
                                            navController.navigate(BaeumRoutes.LIVE_CONVERSATION)
                                        }
                                        BeginnerPracticeAction.TOPIK_I_EXAM -> {
                                            viewModel.startExam(ExamType.TOPIK_I, ExamSessionMode.PRACTICE)
                                            navController.navigate(BaeumRoutes.ACTIVE_EXAM)
                                        }
                                        BeginnerPracticeAction.TOPIK_II_FLASHCARDS -> {
                                            flashcardViewModel.startSession(curriculumFilter = CurriculumPathway.TOPIK, subCategoryFilter = "3급", dueOnly = false)
                                            navController.navigate(BaeumRoutes.FLASHCARD)
                                        }
                                        BeginnerPracticeAction.TOPIK_II_EXAM -> {
                                            viewModel.startExam(ExamType.TOPIK_II, ExamSessionMode.PRACTICE)
                                            navController.navigate(BaeumRoutes.ACTIVE_EXAM)
                                        }
                                    }
                                }
                            )
                        }

                        composable(
                            route = BaeumRoutes.STAGE_BOSS,
                            arguments = listOf(navArgument("stageNumber") { type = NavType.IntType })
                        ) { entry ->
                            val habitSnapshot by viewModel.habitSnapshot.collectAsStateWithLifecycle()
                            val stageNumber = entry.arguments?.getInt("stageNumber") ?: 1
                            StageBossScreen(
                                stageNumber = stageNumber,
                                isAlreadyDefeated = stageNumber in habitSnapshot.defeatedStageBossNumbers,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onCheckSound = { ok, step -> viewModel.playBeginnerCheckSound(ok, step) },
                                onBossFinished = { stg, passed, correct, total ->
                                    viewModel.completeStageBoss(stg, passed, correct, total)
                                }
                            )
                        }

                        composable(BaeumRoutes.MISTAKE_REDEMPTION) {
                            val habitSnapshot by viewModel.habitSnapshot.collectAsStateWithLifecycle()
                            MistakeRedemptionScreen(
                                mistakeKeys = habitSnapshot.mistakeVaultKeys,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onCheckSound = { ok, step -> viewModel.playBeginnerCheckSound(ok, step) },
                                onRedeemMistake = { key -> viewModel.redeemSyllabusMistake(key) }
                            )
                        }

                        // ==================== LEARNING SUB-SCREENS ====================
                        composable(BaeumRoutes.HANGUL_STUDIO) {
                            val ttsStatus by viewModel.ttsStatus.collectAsStateWithLifecycle()
                            HangulStudioScreen(
                                showRomanization = settings.romanizationMode != RomanizationMode.HIDDEN,
                                ttsStatus = ttsStatus,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onStartPractice = {
                                    viewModel.startQuiz(QuizMode.BATCHIM)
                                    navController.navigate(BaeumRoutes.ACTIVE_QUIZ)
                                },
                                onOpenWritingStudio = { navController.navigate(BaeumRoutes.HANGUL_WRITING) }
                            )
                        }

                        composable(BaeumRoutes.HANGUL_WRITING) {
                            HangulWritingScreen(
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onPracticeCompleted = { viewModel.recordWritingPracticeCompleted() }
                            )
                        }

                        composable(BaeumRoutes.TOPIK_CENTER) {
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            val habitSnapshot by viewModel.habitSnapshot.collectAsStateWithLifecycle()
                            TopikCenterScreen(
                                allVocab = allVocab,
                                completedSyllabusLessons = habitSnapshot.completedBeginnerLessonIds,
                                onBack = { navController.popBackStack() },
                                onOpenLevelVocab = { level ->
                                    navController.navigate(BaeumRoutes.vocabularyRoute(CurriculumPathway.TOPIK, level))
                                },
                                onStartLevelFlashcards = { level ->
                                    flashcardViewModel.startSession(CurriculumPathway.TOPIK, level)
                                    navController.navigate(BaeumRoutes.FLASHCARD)
                                },
                                onOpenGrammar = { navController.navigate(BaeumRoutes.GRAMMAR_STUDIO) },
                                onOpenExam = {
                                    viewModel.startExam(ExamType.TOPIK_I, ExamSessionMode.MOCK)
                                    navController.navigate(BaeumRoutes.ACTIVE_EXAM)
                                },
                                onOpenSyllabusTrack = { track ->
                                    navController.navigate(BaeumRoutes.syllabusRoute(track))
                                }
                            )
                        }

                        composable(BaeumRoutes.EPS_CENTER) {
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            EpsTopikCenterScreen(
                                allVocab = allVocab,
                                onBack = { navController.popBackStack() },
                                onOpenCategoryVocab = { cat ->
                                    navController.navigate(BaeumRoutes.vocabularyRoute(CurriculumPathway.EPS_TOPIK, cat))
                                },
                                onStartEpsFlashcards = {
                                    flashcardViewModel.startSession(CurriculumPathway.EPS_TOPIK, null)
                                    navController.navigate(BaeumRoutes.FLASHCARD)
                                },
                                onOpenEpsExam = {
                                    viewModel.startExam(ExamType.EPS_TOPIK, ExamSessionMode.MOCK)
                                    navController.navigate(BaeumRoutes.ACTIVE_EXAM)
                                }
                            )
                        }

                        composable(BaeumRoutes.EVERYDAY_KOREAN) {
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            EverydayKoreanScreen(
                                allVocab = allVocab,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onOpenCategoryVocab = { cat ->
                                    navController.navigate(BaeumRoutes.vocabularyRoute(CurriculumPathway.EVERYDAY, cat))
                                },
                                onStartConversation = { navController.navigate(BaeumRoutes.SPEAK_DIFFICULTY) }
                            )
                        }

                        composable(
                            route = BaeumRoutes.VOCABULARY,
                            arguments = listOf(
                                navArgument("curriculum") { type = NavType.StringType; defaultValue = "" },
                                navArgument("sub") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { entry ->
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            val cur = entry.arguments?.getString("curriculum")?.ifBlank { null }
                            val sub = entry.arguments?.getString("sub")?.ifBlank { null }
                            VocabularyLibraryScreen(
                                allVocab = allVocab,
                                initialCurriculum = cur,
                                initialSubCategory = sub,
                                romanizationMode = settings.romanizationMode,
                                translationLang = settings.translationLanguage,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onToggleBookmark = { viewModel.toggleBookmark(it) },
                                onOpenWordDetail = { wordId -> navController.navigate(BaeumRoutes.wordDetailRoute(wordId)) },
                                onStudyFilteredCards = { fCur, fSub ->
                                    flashcardViewModel.startSession(fCur, fSub, dueOnly = false)
                                    navController.navigate(BaeumRoutes.FLASHCARD)
                                }
                            )
                        }

                        composable(
                            route = BaeumRoutes.WORD_DETAIL,
                            arguments = listOf(navArgument("wordId") { type = NavType.StringType })
                        ) { entry ->
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            val wordId = entry.arguments?.getString("wordId").orEmpty()
                            val word = remember(allVocab, wordId) { allVocab.firstOrNull { it.id == wordId } }
                            WordDetailScreen(
                                word = word,
                                romanizationMode = settings.romanizationMode,
                                translationLang = settings.translationLanguage,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onToggleBookmark = { viewModel.toggleBookmark(it) }
                            )
                        }

                        composable(BaeumRoutes.FLASHCARD) {
                            val flashcardSession by flashcardViewModel.sessionState.collectAsStateWithLifecycle()
                            SmartFlashcardScreen(
                                session = flashcardSession,
                                romanizationMode = settings.romanizationMode,
                                translationLang = settings.translationLanguage,
                                onBack = { navController.popBackStack() },
                                onReveal = {
                                    viewModel.playCardFlipSound()
                                    flashcardViewModel.revealCard()
                                },
                                onRate = { rating ->
                                    val nextCombo = when (rating) {
                                        FsrsRating.GOOD, FsrsRating.EASY -> flashcardSession.sessionCombo + 1
                                        FsrsRating.HARD -> flashcardSession.sessionCombo
                                        FsrsRating.AGAIN -> 0
                                    }
                                    viewModel.playFlashcardRatingSound(rating, nextCombo)
                                    flashcardViewModel.rateCurrentCard(rating)
                                },
                                onUndo = { flashcardViewModel.undoLastRating() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onToggleRomanization = {
                                    val next = if (settings.romanizationMode == RomanizationMode.ALWAYS) {
                                        RomanizationMode.HIDDEN
                                    } else {
                                        RomanizationMode.ALWAYS
                                    }
                                    viewModel.setRomanizationMode(next)
                                },
                                onStartNewSession = { flashcardViewModel.startSession(dueOnly = false) }
                            )
                        }

                        composable(BaeumRoutes.SEOUL_BLITZ) {
                            val blitzState by viewModel.blitzSession.collectAsStateWithLifecycle()
                            SeoulBlitzScreen(
                                state = blitzState,
                                onBack = { navController.popBackStack() },
                                onStartOrReplay = { viewModel.startSeoulBlitz() },
                                onSelectOption = { viewModel.answerBlitzOption(it) },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onAbandon = { viewModel.abandonSeoulBlitz() }
                            )
                        }

                        composable(BaeumRoutes.REVIEW_PLANNER) {
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            ReviewPlannerScreen(
                                allVocab = allVocab,
                                onBack = { navController.popBackStack() },
                                onStartDueReview = {
                                    flashcardViewModel.startSession(dueOnly = true)
                                    navController.navigate(BaeumRoutes.FLASHCARD)
                                },
                                onStartNewStudy = {
                                    flashcardViewModel.startSession(dueOnly = false)
                                    navController.navigate(BaeumRoutes.FLASHCARD)
                                }
                            )
                        }

                        composable(BaeumRoutes.QUIZ_CENTER) {
                            val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()
                            val quizModeProgress by viewModel.quizModeProgress.collectAsStateWithLifecycle()
                            QuizCenterScreen(
                                allVocab = allVocab,
                                modeProgress = quizModeProgress,
                                onBack = { navController.popBackStack() },
                                onSelectQuizMode = { mode, weakOnly ->
                                    viewModel.startQuiz(mode, weakOnly = weakOnly)
                                    navController.navigate(BaeumRoutes.ACTIVE_QUIZ)
                                }
                            )
                        }

                        composable(BaeumRoutes.ACTIVE_QUIZ) {
                            val activeQuiz by viewModel.activeQuiz.collectAsStateWithLifecycle()
                            ActiveQuizScreen(
                                state = activeQuiz,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onSelectOption = { viewModel.selectQuizOption(it) },
                                onTypedChange = { viewModel.updateQuizTypedInput(it) },
                                onAppendToken = { viewModel.appendArrangementToken(it) },
                                onRemoveToken = { viewModel.removeArrangementToken(it) },
                                onClearTokens = { viewModel.clearArrangement() },
                                onCheckAnswer = { viewModel.checkQuizAnswer() },
                                onNextQuestion = { viewModel.nextQuizQuestion() },
                                onRestart = {
                                    val mode = activeQuiz?.mode ?: QuizMode.RECALL
                                    viewModel.startQuiz(mode)
                                }
                            )
                        }

                        composable(BaeumRoutes.GRAMMAR_STUDIO) {
                            val grammarBookmarks by viewModel.grammarBookmarks.collectAsStateWithLifecycle()
                            GrammarStudioScreen(
                                bookmarks = grammarBookmarks,
                                translationLang = settings.translationLanguage,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onToggleBookmark = { viewModel.toggleGrammarBookmark(it) },
                                onPracticeGrammar = { gId ->
                                    viewModel.startQuiz(QuizMode.GRAMMAR, focusGrammarId = gId)
                                    navController.navigate(BaeumRoutes.ACTIVE_QUIZ)
                                }
                            )
                        }

                        // ==================== SPEAKING SUB-SCREENS ====================
                        composable(BaeumRoutes.SPEAK_DIFFICULTY) {
                            DifficultySelectionScreen(
                                onBack = { navController.popBackStack() },
                                onSelectDifficulty = { diff ->
                                    viewModel.startAutoConversation(diff)
                                    navController.navigate(BaeumRoutes.LIVE_CONVERSATION)
                                }
                            )
                        }

                        composable(BaeumRoutes.LIVE_CONVERSATION) {
                            val liveConversation by viewModel.liveConversation.collectAsStateWithLifecycle()
                            val isPlayingRecording by viewModel.isPlayingRecording.collectAsStateWithLifecycle()
                            LiveConversationScreen(
                                state = liveConversation,
                                isPlayingRecording = isPlayingRecording,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onStartRecording = { viewModel.startVoiceCapture() },
                                onStopRecording = { viewModel.stopVoiceCapture() },
                                onRedoRecording = { viewModel.redoVoiceCapture() },
                                onEnterManualMode = { viewModel.enterManualTranscriptMode() },
                                onDraftChange = { viewModel.updateDraftTranscript(it) },
                                onPlayRecording = { viewModel.playDraftRecording() },
                                onToggleHint = { viewModel.toggleConversationHint() },
                                onSubmitAnswer = { viewModel.submitConversationTurn() },
                                onCompletedNavigate = {
                                    navController.navigate(BaeumRoutes.SPEAK_ASSESSMENT) {
                                        popUpTo(BaeumRoutes.LIVE_CONVERSATION) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(BaeumRoutes.SPEAK_ASSESSMENT) {
                            val lastAssessment by viewModel.lastSpeakingAssessment.collectAsStateWithLifecycle()
                            SpeakingAssessmentScreen(
                                assessment = lastAssessment,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onNewScenario = {
                                    val diff = lastAssessment?.difficulty ?: SpeakingDifficulty.BEGINNER
                                    viewModel.startAutoConversation(diff)
                                    navController.navigate(BaeumRoutes.LIVE_CONVERSATION) {
                                        popUpTo(BaeumRoutes.SPEAK_ASSESSMENT) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(BaeumRoutes.PRONUNCIATION_LAB) {
                            val pronunciationPractice by viewModel.pronunciationPractice.collectAsStateWithLifecycle()
                            PronunciationLabScreen(
                                state = pronunciationPractice,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onSelectCategory = { viewModel.selectPronunciationCategory(it) },
                                onSelectDrill = { viewModel.selectPronunciationDrill(it) },
                                onNextDrill = { viewModel.selectNextPronunciationDrill() },
                                onStartListening = { viewModel.startPronunciationListening() },
                                onStopListening = { viewModel.stopPronunciationListening() },
                                onCancelListening = { viewModel.cancelPronunciationListening() },
                                onRetryDrill = { viewModel.clearPronunciationResult() }
                            )
                        }

                        composable(BaeumRoutes.SPEAK_HISTORY) {
                            val speakingSessions by viewModel.speakingSessions.collectAsStateWithLifecycle()
                            SpeakingHistoryScreen(
                                sessions = speakingSessions,
                                onBack = { navController.popBackStack() },
                                onOpenSession = { sessionEntity ->
                                    viewModel.loadHistoricalSpeakingAssessment(sessionEntity)
                                    navController.navigate(BaeumRoutes.SPEAK_ASSESSMENT)
                                }
                            )
                        }

                        // ==================== EXAM SUB-SCREENS ====================
                        composable(BaeumRoutes.ACTIVE_EXAM) {
                            val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
                            val ttsStatus by viewModel.ttsStatus.collectAsStateWithLifecycle()
                            val speechCaptureState by viewModel.speechCaptureState.collectAsStateWithLifecycle()
                            ActiveExamScreen(
                                session = activeExam,
                                ttsStatus = ttsStatus,
                                speechCaptureState = speechCaptureState,
                                onAbandonExam = {
                                    viewModel.abandonActiveExam()
                                    navController.popBackStack()
                                },
                                onPlayExamAudio = { qId -> viewModel.playExamAudio(qId) },
                                onStopExamAudio = { viewModel.stopExamAudio() },
                                onStartVoiceAnswer = { viewModel.startExamVoiceAnswer() },
                                onStopVoiceAnswer = { viewModel.stopExamVoiceAnswer() },
                                onSelectOption = { qId, idx -> viewModel.selectExamOption(qId, idx) },
                                onToggleEliminateOption = { qId, idx -> viewModel.toggleEliminateExamOption(qId, idx) },
                                onOpenResponseChange = { qId, txt -> viewModel.updateExamOpenResponse(qId, txt) },
                                onDualBlankResponseChange = { qId, g, n -> viewModel.updateExamDualBlankResponse(qId, g, n) },
                                onToggleFlag = { qId -> viewModel.toggleExamFlag(qId) },
                                onJumpToQuestion = { idx -> viewModel.jumpToExamQuestion(idx) },
                                onEnterPeriodTransition = { viewModel.enterExamPeriodTransition() },
                                onStartNextPeriod = { viewModel.startNextExamPeriod() },
                                onSubmitExam = {
                                    viewModel.submitActiveExam()
                                    navController.navigate(BaeumRoutes.EXAM_REPORT) {
                                        popUpTo(BaeumRoutes.ACTIVE_EXAM) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(BaeumRoutes.EXAM_REPORT) {
                            val lastExamReport by viewModel.lastExamReport.collectAsStateWithLifecycle()
                            ExamReportScreen(
                                report = lastExamReport,
                                onBack = { navController.popBackStack() },
                                onSpeakKorean = { viewModel.speakKorean(it) },
                                onRetakeExam = { type, mode, length ->
                                    viewModel.startExam(type, mode, length)
                                    navController.navigate(BaeumRoutes.ACTIVE_EXAM) {
                                        popUpTo(BaeumRoutes.EXAM_REPORT) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ==================== SETTINGS ====================
                        composable(BaeumRoutes.SETTINGS) {
                            val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
                            SettingsScreen(
                                settings = settings,
                                statusMessage = statusMessage,
                                onBack = { navController.popBackStack() },
                                onThemeChange = { viewModel.setThemeMode(it) },
                                onTranslationLangChange = { viewModel.setTranslationLanguage(it) },
                                onFontScaleChange = { viewModel.setFontScale(it) },
                                onDailyGoalChange = { viewModel.setDailyGoal(it) },
                                onRomanizationChange = { viewModel.setRomanizationMode(it) },
                                onFsrsRetentionChange = { viewModel.setFsrsRetention(it) },
                                onSpeechRateChange = { viewModel.setSpeechRate(it) },
                                onReducedMotionChange = { viewModel.setReducedMotion(it) },
                                onSoundEffectsChange = { viewModel.setSoundEffectsEnabled(it) },
                                onDailyReminderChange = { enabled, hour, minute ->
                                    viewModel.updateDailyReminder(enabled, hour, minute)
                                },
                                onSendTestReminder = { viewModel.sendTestStudyReminder() },
                                onExportBackupJson = { viewModel.generateBackupJson() },
                                onRestoreBackupJson = { viewModel.restoreFromBackupJson(it) },
                                onReportStatus = { viewModel.reportStatusMessage(it) },
                                onReplayOnboarding = { viewModel.replayOnboarding() },
                                onClearStatus = { viewModel.clearStatusMessage() }
                            )
                        }
                    }
                }
            }

            if (useNavRail) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (showPrimaryNav) {
                        NavigationRail(
                            containerColor = extended.dockSurface,
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(topSafeInsets)
                                .navigationBarsPadding()
                        ) {
                            topLevelTabs.forEach { tab ->
                                val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                                val tabLabel = bText(tab.labelEn, tab.labelId)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTopTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tabLabel
                                        )
                                    },
                                    label = { Text(tabLabel) },
                                    modifier = Modifier.testTag("nav_tab_${tab.tagKey}")
                                )
                            }
                        }
                    }
                    navHostContent(
                        Modifier
                            .weight(1f)
                            .windowInsetsPadding(topSafeInsets)
                            .navigationBarsPadding()
                    )
                }
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = topSafeInsets,
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showPrimaryNav,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(if (extended.reducedMotion) 0 else 280, easing = BaeumMotion.SilkEasing)
                            ) + fadeIn(tween(if (extended.reducedMotion) 0 else 220)),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(if (extended.reducedMotion) 0 else 240, easing = BaeumMotion.SilkEasing)
                            ) + fadeOut(tween(if (extended.reducedMotion) 0 else 180))
                        ) {
                            SeoulFloatingGlassDock(
                                tabs = topLevelTabs,
                                currentDestinationRoute = currentDest?.route,
                                isSelected = { tab ->
                                    currentDest?.hierarchy?.any { it.route == tab.route } == true
                                },
                                onSelectTab = { navigateTopTab(it.route) }
                            )
                        }
                    }
                ) { innerPadding ->
                    navHostContent(
                        Modifier
                            .padding(innerPadding)
                            .then(if (!showPrimaryNav) Modifier.navigationBarsPadding() else Modifier)
                    )
                }
            }

            val celebrationEvent by viewModel.celebrationEvent.collectAsStateWithLifecycle()
            GlobalCelebrationToast(
                event = if (currentDest?.route == BaeumRoutes.ACTIVE_EXAM) null else celebrationEvent,
                onDismiss = { viewModel.dismissCelebration() }
            )
        }
    }
}

/**
 * Refined Seoul Atelier floating capsule dock (zero real-time `Modifier.blur` for crisp 60/120fps scrolling),
 * with hairline beveled border, spring-gliding active capsule using draw-phase `graphicsLayer`,
 * and guaranteed >= 54dp touch targets.
 */
private val DockOuterShape = RoundedCornerShape(28.dp)
private val DockPillShape = RoundedCornerShape(22.dp)

@Composable
private fun SeoulFloatingGlassDock(
    tabs: List<TopLevelTab>,
    currentDestinationRoute: String?,
    isSelected: (TopLevelTab) -> Boolean,
    onSelectTab: (TopLevelTab) -> Unit
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val shadowTint = if (extended.isDark) Color(0x88000000) else Color(0x28192138)
    val selectedIndex = tabs.indexOfFirst { isSelected(it) || currentDestinationRoute == it.route }.coerceAtLeast(0)

    val dockFillBrush = remember(extended.isDark, extended.dockSurface, extended.glassFillBottom) {
        Brush.verticalGradient(
            if (extended.isDark) {
                listOf(extended.dockSurface, extended.glassFillBottom.copy(alpha = 0.98f))
            } else {
                listOf(Color(0xFFFFFFFF), Color(0xFFF8F7FD))
            }
        )
    }
    val dockBorderBrush = remember(extended.isDark, extended.glassHighlightBorder, extended.cardBorder, primary) {
        Brush.linearGradient(
            if (extended.isDark) {
                listOf(
                    extended.glassHighlightBorder,
                    extended.cardBorder.copy(alpha = 0.60f),
                    extended.glassHighlightBorder.copy(alpha = 0.25f)
                )
            } else {
                listOf(
                    extended.cardBorder,
                    extended.cardBorder.copy(alpha = 0.85f),
                    primary.copy(alpha = 0.25f)
                )
            }
        )
    }
    val pillFillBrush = remember(extended.isDark, primary, extended.mint) {
        Brush.verticalGradient(
            listOf(
                primary.copy(alpha = if (extended.isDark) 0.22f else 0.14f),
                extended.mint.copy(alpha = if (extended.isDark) 0.08f else 0.07f)
            )
        )
    }
    val pillBorderBrush = remember(extended.isDark, primary, extended.mint) {
        Brush.linearGradient(
            listOf(
                primary.copy(alpha = if (extended.isDark) 0.42f else 0.35f),
                extended.mint.copy(alpha = if (extended.isDark) 0.18f else 0.20f)
            )
        )
    }
    val pillAccentBarBrush = remember(primary, extended.mint) {
        Brush.horizontalGradient(listOf(primary, extended.mint))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (extended.isDark) 16.dp else 12.dp,
                    shape = DockOuterShape,
                    ambientColor = shadowTint,
                    spotColor = shadowTint
                )
                .clip(DockOuterShape)
                .background(dockFillBrush)
                .border(width = 1.dp, brush = dockBorderBrush, shape = DockOuterShape)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                val tabWidth = maxWidth / tabs.size.coerceAtLeast(1)
                val tabWidthPx = with(density) { tabWidth.toPx() }
                val indicatorIndexState = animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = if (extended.reducedMotion) tween(0) else BaeumMotion.liquidSpring(),
                    label = "dock_liquid_pill_offset"
                )

                // Gliding active indicator pill (draw-phase translationX for zero relayout)
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = indicatorIndexState.value * tabWidthPx
                        }
                        .width(tabWidth)
                        .height(54.dp)
                        .clip(DockPillShape)
                        .background(pillFillBrush)
                        .border(width = 1.dp, brush = pillBorderBrush, shape = DockPillShape),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .width(20.dp)
                            .height(2.5.dp)
                            .clip(CircleShape)
                            .background(pillAccentBarBrush)
                    )
                }

                // Foreground interactive tab buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { idx, tab ->
                        val selected = idx == selectedIndex
                        val tabLabel = bText(tab.labelEn, tab.labelId)
                        val focusProgressState = animateFloatAsState(
                            targetValue = if (selected) 1f else 0f,
                            animationSpec = if (extended.reducedMotion) tween(0) else BaeumMotion.playfulSpring(),
                            label = "dock_icon_focus_${tab.tagKey}"
                        )
                        val contentTint by animateColorAsState(
                            targetValue = if (selected) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(if (extended.reducedMotion) 0 else 200),
                            label = "dock_tint_${tab.tagKey}"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 54.dp)
                                .clip(DockPillShape)
                                .tactileClickable(pressedScale = 0.93f) { onSelectTab(tab) }
                                .testTag("nav_tab_${tab.tagKey}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tabLabel,
                                    tint = contentTint,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer {
                                            if (!extended.reducedMotion) {
                                                val p = focusProgressState.value
                                                val s = 1f + 0.10f * p
                                                scaleX = s
                                                scaleY = s
                                                translationY = (-1.5f * p).dp.toPx()
                                            }
                                        }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tabLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = contentTint,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
