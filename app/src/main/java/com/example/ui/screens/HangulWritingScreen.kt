package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.hangul.HangulUnicodeEngine
import com.example.domain.hangul.StrokeEvaluationResult
import com.example.ui.components.BaeumPrimaryButton
import com.example.ui.components.BaeumSecondaryButton
import com.example.ui.components.BaeumTopBar
import com.example.ui.components.ProgressBarSlim
import com.example.ui.components.SeoulCard
import com.example.ui.theme.LocalBaeumColors
import com.example.ui.theme.bText
import kotlinx.coroutines.launch

// SCREEN 06 / 24 — Hangul Writing Studio (Guided stroke practice + real stroke validation)
@Composable
fun HangulWritingScreen(
    onBack: () -> Unit,
    onSpeakKorean: (String) -> Unit,
    onPracticeCompleted: () -> Unit
) {
    val characters = remember { HangulUnicodeEngine.writableCharacters }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFreeWriting by remember { mutableStateOf(false) }
    var isDrawing by remember { mutableStateOf(false) }
    var canvasWidthPx by remember { mutableFloatStateOf(0f) }
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var evaluation by remember { mutableStateOf<StrokeEvaluationResult?>(null) }

    val scrollState = rememberScrollState()
    val demoProgress = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    val currentChar = characters[currentIndex]
    val extended = LocalBaeumColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSecondaryColor = MaterialTheme.colorScheme.onSecondary
    val userInkColor = MaterialTheme.colorScheme.onSurface
    val gridColor = extended.cardBorder.copy(alpha = 0.45f)
    val badgeTextStyle = TextStyle(
        color = onSecondaryColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )

    fun clearCanvas() {
        strokes.clear()
        currentStroke = emptyList()
        evaluation = null
    }

    fun triggerDemonstration() {
        scope.launch {
            demoProgress.snapTo(0f)
            demoProgress.animateTo(1f, animationSpec = tween(durationMillis = 1200))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState, enabled = !isDrawing)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaeumTopBar(
            title = bText("Writing practice", "Latihan menulis"),
            subtitle = "${currentIndex + 1} / ${characters.size}",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressBarSlim(progress = (currentIndex + 1).toFloat() / characters.size)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${currentChar.glyph} — ${currentChar.nameKo}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = { onSpeakKorean(currentChar.nameKo) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(bText("Listen", "Dengar"), color = primaryColor)
                }
            }
            Text(
                text = if (isFreeWriting) {
                    bText("Free writing mode (guides hidden)", "Mode menulis bebas (panduan disembunyikan)")
                } else {
                    bText(
                        "${currentChar.guideStrokes.size} stroke(s) • Trace following numbered order (1 → ${currentChar.guideStrokes.size})",
                        "${currentChar.guideStrokes.size} goresan • Ikuti urutan nomor (1 → ${currentChar.guideStrokes.size})"
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Drawing Grid Canvas (scroll-locked while actively drawing)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .onSizeChanged { canvasWidthPx = it.width.toFloat() },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.cardBorder)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentIndex) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    down.consume()
                                    isDrawing = true
                                    evaluation = null
                                    val startPos = Offset(
                                        down.position.x.coerceIn(0f, size.width.toFloat()),
                                        down.position.y.coerceIn(0f, size.height.toFloat())
                                    )
                                    val activePoints = mutableListOf(startPos)
                                    currentStroke = activePoints.toList()
                                    try {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == down.id }
                                                ?: event.changes.firstOrNull()
                                                ?: break
                                            if (!change.pressed) {
                                                change.consume()
                                                break
                                            }
                                            change.consume()
                                            val clamped = Offset(
                                                change.position.x.coerceIn(0f, size.width.toFloat()),
                                                change.position.y.coerceIn(0f, size.height.toFloat())
                                            )
                                            if ((clamped - activePoints.last()).getDistance() >= 2.5f) {
                                                activePoints.add(clamped)
                                                currentStroke = activePoints.toList()
                                            }
                                        }
                                        if (activePoints.size >= 2) {
                                            strokes.add(activePoints.toList())
                                        }
                                    } finally {
                                        currentStroke = emptyList()
                                        isDrawing = false
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height

                        // 4x4 subtle guide grid
                        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        for (i in 1..3) {
                            val x = w * (i / 4f)
                            val y = h * (i / 4f)
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = if (i == 2) 2f else 1f,
                                pathEffect = if (i == 2) dash else null
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = if (i == 2) 2f else 1f,
                                pathEffect = if (i == 2) dash else null
                            )
                        }

                        // Canonical Hangul Guide Strokes + Numbered Start Badges (Blueprint Screen 06)
                        if (!isFreeWriting) {
                            val totalStrokes = currentChar.guideStrokes.size.coerceAtLeast(1)
                            val activeFloat = demoProgress.value * totalStrokes
                            currentChar.guideStrokes.forEachIndexed { idx, guide ->
                                val strokeFraction = (activeFloat - idx).coerceIn(0f, 1f)
                                if (strokeFraction > 0f && guide.size >= 2) {
                                    val path = buildPartialPath(guide, w, h, strokeFraction)
                                    drawPath(
                                        path = path,
                                        color = primaryColor.copy(alpha = 0.42f),
                                        style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                    val startPt = Offset(guide.first().x * w, guide.first().y * h)
                                    val badgeRadius = 14.dp.toPx().coerceAtMost(24f)
                                    drawCircle(
                                        color = extended.mint,
                                        radius = badgeRadius,
                                        center = startPt
                                    )
                                    val measured = textMeasurer.measure(
                                        text = "${idx + 1}",
                                        style = badgeTextStyle
                                    )
                                    drawText(
                                        textLayoutResult = measured,
                                        topLeft = Offset(
                                            x = startPt.x - measured.size.width / 2f,
                                            y = startPt.y - measured.size.height / 2f
                                        )
                                    )
                                }
                            }
                        }

                        // User Drawn Strokes
                        (strokes + listOf(currentStroke)).forEach { pts ->
                            if (pts.size >= 2) {
                                val p = Path().apply {
                                    moveTo(pts.first().x, pts.first().y)
                                    pts.drop(1).forEach { lineTo(it.x, it.y) }
                                }
                                drawPath(
                                    path = p,
                                    color = userInkColor,
                                    style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Undo • Demonstrate • Reset controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        if (strokes.isNotEmpty()) {
                            strokes.removeAt(strokes.lastIndex)
                            evaluation = null
                        }
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("writing_undo_button")
                ) {
                    Text(bText("Undo", "Urungkan"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(
                    onClick = {
                        isFreeWriting = false
                        triggerDemonstration()
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("writing_demo_button")
                ) {
                    Text(bText("Demonstrate", "Peragakan"), color = primaryColor)
                }
                TextButton(
                    onClick = { clearCanvas() },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("writing_reset_button")
                ) {
                    Text(bText("Reset", "Ulang"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (evaluation != null) {
                val res = evaluation!!
                Spacer(modifier = Modifier.height(8.dp))
                SeoulCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (res.passed) extended.mint else extended.cherryPink,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(
                        text = if (res.passed) {
                            bText("Stroke check passed (${res.coveragePercent}%)", "Goresan lulus (${res.coveragePercent}%)")
                        } else {
                            bText("Keep refining (${res.coveragePercent}%)", "Terus rapikan (${res.coveragePercent}%)")
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (res.passed) extended.mint else extended.cherryPink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = res.feedback,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            BaeumPrimaryButton(
                text = if (evaluation?.passed == true) bText("Next character", "Huruf berikutnya") else bText("Check writing", "Periksa tulisan"),
                onClick = {
                    if (evaluation?.passed == true) {
                        currentIndex = (currentIndex + 1) % characters.size
                        clearCanvas()
                    } else {
                        val res = HangulUnicodeEngine.evaluateStrokes(
                            userStrokes = strokes.toList(),
                            guideStrokes = currentChar.guideStrokes,
                            canvasSizePx = canvasWidthPx
                        )
                        evaluation = res
                        if (res.passed) onPracticeCompleted()
                    }
                },
                testTag = "writing_check_button"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BaeumSecondaryButton(
                    text = if (isFreeWriting) bText("Guided tracing", "Mode panduan") else bText("Free writing", "Menulis bebas"),
                    onClick = { isFreeWriting = !isFreeWriting },
                    modifier = Modifier.weight(1f),
                    testTag = "writing_toggle_mode"
                )
                BaeumSecondaryButton(
                    text = bText("Skip char", "Lewati huruf"),
                    onClick = {
                        currentIndex = (currentIndex + 1) % characters.size
                        clearCanvas()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun buildPartialPath(guide: List<Offset>, w: Float, h: Float, fraction: Float): Path {
    val path = Path()
    if (guide.isEmpty()) return path
    path.moveTo(guide.first().x * w, guide.first().y * h)
    val segments = guide.size - 1
    val scaled = fraction * segments
    for (i in 0 until segments) {
        val segFrac = (scaled - i).coerceIn(0f, 1f)
        if (segFrac <= 0f) break
        val a = guide[i]
        val b = guide[i + 1]
        val px = (a.x + (b.x - a.x) * segFrac) * w
        val py = (a.y + (b.y - a.y) * segFrac) * h
        path.lineTo(px, py)
    }
    return path
}
