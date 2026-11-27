package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.CurriculumPathway
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.BaeumMotion
import com.example.ui.theme.EditorialSerifFamily
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText
import com.example.ui.theme.tactileClickable

// SCREEN 01 / 24 — Luxury Atelier Splash Screen
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplashScreen(
    initError: String?,
    onRetry: () -> Unit
) {
    val extended = LocalBaeumColors.current
    val topSafeInsets = WindowInsets.statusBars
        .union(WindowInsets.statusBarsIgnoringVisibility)
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Top)
    val haloScaleState = if (extended.reducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    } else {
        val infinite = rememberInfiniteTransition(label = "splash_halo")
        infinite.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "halo_pulse"
        )
    }
    val emblemFloatState = if (extended.reducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        val infinite = rememberInfiniteTransition(label = "splash_float")
        infinite.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emblem_levitate"
        )
    }
    val loadingSweepState = if (extended.reducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.58f) }
    } else {
        val infinite = rememberInfiniteTransition(label = "splash_progress")
        infinite.animateFloat(
            initialValue = 0.20f,
            targetValue = 0.88f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = BaeumMotion.SilkEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "splash_bar_sweep"
        )
    }
    var entered by androidx.compose.runtime.remember { mutableStateOf(extended.reducedMotion) }
    androidx.compose.runtime.LaunchedEffect(Unit) { entered = true }
    val entranceScaleState = androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (entered) 1f else 0.86f,
        animationSpec = if (extended.reducedMotion) tween(0) else BaeumMotion.luxurySpring(),
        label = "splash_emblem_scale"
    )
    val entranceAlphaState = androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = if (extended.reducedMotion) tween(0) else tween(480, easing = BaeumMotion.SilkEasing),
        label = "splash_emblem_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(topSafeInsets)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .graphicsLayer {
                    alpha = entranceAlphaState.value
                    val s = entranceScaleState.value
                    scaleX = s
                    scaleY = s
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .graphicsLayer {
                            val s = haloScaleState.value
                            scaleX = s
                            scaleY = s
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.36f),
                                    extended.gold.copy(alpha = 0.16f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .graphicsLayer {
                            translationY = emblemFloatState.value.dp.toPx()
                        }
                        .shadow(20.dp, CircleShape, ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(extended.gold.copy(alpha = 0.7f), MaterialTheme.colorScheme.primary)
                            ),
                            shape = CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_moonjar_atelier),
                        contentDescription = "Baeum Korean Moon Jar emblem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "배움",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "B A E U M",
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 5.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = bText("Your serene atelier for Korean mastery.", "Atelier tenang untuk menguasai bahasa Korea."),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = EditorialSerifFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(36.dp))

            if (initError != null) {
                SeoulCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = extended.cherryPink.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = bText("Database initialization error", "Gagal memuat basis data"),
                        style = MaterialTheme.typography.titleSmall,
                        color = extended.cherryPink
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = initError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    BaeumPrimaryButton(text = bText("Retry recovery", "Coba lagi"), onClick = onRetry, testTag = "splash_retry_button")
                }
            } else {
                ProgressBarSlim(
                    progress = loadingSweepState.value,
                    modifier = Modifier.width(142.dp),
                    height = 6.dp
                )
            }
        }
    }
}

// SCREEN 02 / 24 — Luxury 3-Step Personalized Onboarding
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    currentLang: String = "EN",
    onSelectLang: (String) -> Unit = {},
    onFinish: (goal: String, level: String, dailyGoal: Int) -> Unit
) {
    val extended = LocalBaeumColors.current
    val topSafeInsets = WindowInsets.statusBars
        .union(WindowInsets.statusBarsIgnoringVisibility)
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Top)
    var step by rememberSaveable { mutableIntStateOf(1) }
    var selectedGoal by rememberSaveable { mutableStateOf(CurriculumPathway.HANGUL) }
    var selectedLevel by rememberSaveable { mutableStateOf("Zero") }
    var selectedDailyGoal by rememberSaveable { mutableIntStateOf(20) }

    BackHandler(enabled = step > 1) {
        step -= 1
    }

    val goals = listOf(
        Triple(
            CurriculumPathway.HANGUL,
            bText("Start from zero (Zero-to-Sentence)", "Mulai dari nol (Pemula → Susun Kalimat)"),
            bText("Step-by-step Hangul, greetings & interactive sentence building", "Panduan bertahap Hangul, sapaan & merangkai kalimat interaktif")
        ),
        Triple(
            CurriculumPathway.TOPIK,
            bText("Prepare for TOPIK I & II", "Persiapan TOPIK I & II"),
            bText("Dedicated guided syllabi for TOPIK I (1–2급) & TOPIK II (3–6급)", "Silabus terpandu khusus TOPIK I (1–2급) & TOPIK II (3–6급)")
        ),
        Triple(
            CurriculumPathway.EPS_TOPIK,
            bText("Work in South Korea", "Bekerja di Korea Selatan"),
            bText("EPS-TOPIK workplace & safety Korean", "Kosakata kerja, pabrik & keselamatan EPS-TOPIK")
        ),
        Triple(
            CurriculumPathway.EVERYDAY,
            bText("Speak naturally", "Berbicara Korea sehari-hari"),
            bText("Everyday Seoul life & polite honorifics", "Percakapan hidup di Seoul & ragam bahasa sopan")
        )
    )

    val levels = listOf(
        Triple("Zero", bText("Complete beginner (Zero)", "Pemula total (dari nol)"), bText("Hangul → Sentence Building (12-lesson path)", "Hangul → Menyusun Kalimat (12 pelajaran)")),
        Triple("Beginner", bText("Basic Hangul & phrases", "Hangul dasar & frasa"), bText("TOPIK I (1–2급) dedicated 10-lesson syllabus", "Silabus khusus TOPIK I (1–2급) · 10 pelajaran")),
        Triple("Intermediate", bText("Everyday communication", "Komunikasi sehari-hari"), bText("TOPIK II (3–4급) dedicated 8-lesson syllabus", "Silabus khusus TOPIK II (3–4급) · 8 pelajaran")),
        Triple("Advanced", bText("Professional & academic", "Profesional & akademis"), bText("TOPIK II (5–6급) & formal writing mastery", "TOPIK II (5–6급) & penulisan formal"))
    )

    val dailyTargets = listOf(
        Triple(10, bText("10 words / day", "10 kata / hari"), bText("Gentle pace • ~5 min daily", "Santai • ~5 menit sehari")),
        Triple(20, bText("20 words / day", "20 kata / hari"), bText("Recommended • ~12 min daily", "Direkomendasikan • ~12 menit sehari")),
        Triple(35, bText("35 words / day", "35 kata / hari"), bText("Focused growth • ~20 min daily", "Fokus berkembang • ~20 menit sehari")),
        Triple(50, bText("50 words / day", "50 kata / hari"), bText("Intensive exam prep • ~30 min daily", "Persiapan ujian intensif • ~30 menit sehari"))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(topSafeInsets)
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, extended.mint)))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BAEUM  •  배움",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Instant ID | EN language switcher pill right in Onboarding
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(extended.glassFillTop)
                        .border(1.dp, extended.cardBorder, RoundedCornerShape(24.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("ID" to "ID", "EN" to "EN").forEach { (code, label) ->
                        val active = currentLang.equals(code, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .tactileClickable(pressedScale = 0.92f) { onSelectLang(code) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("onboarding_lang_$code"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated expanding step pills
            val pillMs = if (extended.reducedMotion) 0 else 250
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..3).forEach { idx ->
                    val active = idx == step
                    val width by animateDpAsState(
                        targetValue = if (active) 28.dp else 8.dp,
                        animationSpec = if (extended.reducedMotion) tween(0) else BaeumMotion.silkSpring(),
                        label = "step_pill_$idx"
                    )
                    val color by animateColorAsState(
                        targetValue = if (idx <= step) MaterialTheme.colorScheme.primary else extended.cardBorder,
                        animationSpec = tween(pillMs),
                        label = "step_color_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = bText("Your Korean journey", "Perjalanan bahasa Koreamu"),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (step) {
                    1 -> bText("Choose your primary learning goal", "Pilih tujuan belajar utamamu")
                    2 -> bText("Select your current Korean level", "Pilih level bahasa Koreamu saat ini")
                    else -> bText("Set your daily vocabulary target", "Atur target kosakata harianmu")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Bespoke Moon Jar Editorial Motto Hero Card
            val mottoShape = androidx.compose.runtime.remember { RoundedCornerShape(30.dp) }
            val primaryColor = MaterialTheme.colorScheme.primary
            val mottoBorderBrush = androidx.compose.runtime.remember(extended.glassHighlightBorder, primaryColor) {
                Brush.linearGradient(
                    listOf(extended.glassHighlightBorder, primaryColor.copy(alpha = 0.4f))
                )
            }
            val mottoScrimBrush = androidx.compose.runtime.remember {
                Brush.verticalGradient(listOf(Color(0x88080D1D), Color(0xCC0A0F22)))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp)
                    .shadow(14.dp, mottoShape, ambientColor = Color(0x66000000))
                    .clip(mottoShape)
                    .border(
                        width = 1.dp,
                        brush = mottoBorderBrush,
                        shape = mottoShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_moonjar_atelier),
                    contentDescription = "Korean Moon Jar fine art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(mottoScrimBrush)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            BaeumMotion.subtleCrossfadeTransform(extended.reducedMotion, durationMs = 280)
                        },
                        label = "motto_text"
                    ) { currentStep ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (currentStep) {
                                    1 -> "오늘도 한 걸음"
                                    2 -> "시작이 반이다"
                                    else -> "꾸준함이 실력이다"
                                },
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF6F7FD),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (currentStep) {
                                    1 -> bText("One graceful step forward, every day.", "Satu langkah tenang maju setiap hari.")
                                    2 -> bText("Well begun is already half done.", "Awal yang baik adalah separuh keberhasilan.")
                                    else -> bText("Quiet consistency builds true fluency.", "Konsistensi tenang membangun kefasihan sejati.")
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = EditorialSerifFamily,
                                    fontSize = 18.sp
                                ),
                                color = Color(0xFFF6D38A),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    BaeumMotion.directionalContentTransform(
                        forward = targetState > initialState,
                        reducedMotion = extended.reducedMotion,
                        durationMs = 300
                    )
                },
                label = "onboarding_step"
            ) { currentStep ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (currentStep) {
                        1 -> goals.forEach { (id, title, subtitle) ->
                            OnboardingOptionCard(
                                title = title,
                                subtitle = subtitle,
                                selected = selectedGoal == id,
                                onClick = { selectedGoal = id },
                                testTag = "onboarding_goal_$id"
                            )
                        }
                        2 -> levels.forEach { (id, title, subtitle) ->
                            OnboardingOptionCard(
                                title = title,
                                subtitle = subtitle,
                                selected = selectedLevel == id,
                                onClick = { selectedLevel = id },
                                testTag = "onboarding_level_$id"
                            )
                        }
                        else -> dailyTargets.forEach { (count, title, subtitle) ->
                            OnboardingOptionCard(
                                title = title,
                                subtitle = subtitle,
                                selected = selectedDailyGoal == count,
                                onClick = { selectedDailyGoal = count },
                                testTag = "onboarding_target_$count"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            BaeumPrimaryButton(
                text = if (step < 3) bText("Continue", "Lanjutkan") else bText("Enter Baeum", "Mulai Baeum"),
                onClick = {
                    if (step < 3) step += 1
                    else onFinish(selectedGoal, selectedLevel, selectedDailyGoal)
                },
                testTag = "onboarding_continue_button"
            )

            if (step > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                BaeumSecondaryButton(
                    text = bText("Back", "Kembali"),
                    onClick = { step -= 1 },
                    testTag = "onboarding_back_button"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bText("Step 0$step of 03", "Langkah 0$step dari 03"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "  •  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { onFinish(selectedGoal, selectedLevel, selectedDailyGoal) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = bText("Skip", "Lewati"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private val OnboardingCardShape = RoundedCornerShape(24.dp)

@Composable
private fun OnboardingOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val extended = LocalBaeumColors.current
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val borderColor by animateColorAsState(
        targetValue = if (selected) primary else extended.cardBorder.copy(alpha = 0.6f),
        animationSpec = tween(if (extended.reducedMotion) 0 else 220),
        label = "opt_border"
    )
    val bgBrush = androidx.compose.runtime.remember(selected, primaryContainer, extended.glassFillTop, extended.glassFillBottom) {
        if (selected) {
            Brush.horizontalGradient(
                listOf(primaryContainer.copy(alpha = 0.75f), extended.glassFillTop)
            )
        } else {
            Brush.verticalGradient(listOf(extended.glassFillTop, extended.glassFillBottom))
        }
    }
    val checkBrush = androidx.compose.runtime.remember(primary, extended.mint) {
        Brush.linearGradient(listOf(primary, extended.mint))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OnboardingCardShape)
            .background(bgBrush)
            .border(width = if (selected) 1.5.dp else 1.dp, color = borderColor, shape = OnboardingCardShape)
            .tactileClickable(pressedScale = 0.975f, onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(checkBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
