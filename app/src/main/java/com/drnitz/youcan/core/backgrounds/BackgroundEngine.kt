package com.drnitz.youcan.core.backgrounds

import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import coil.compose.AsyncImage
import com.drnitz.youcan.data.preferences.BackgroundType
import kotlin.math.PI
import kotlin.math.sin

fun isSystemReduceMotionOrPowerSaving(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isPowerSave = pm?.isPowerSaveMode == true

    val durationScale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f
    )
    val isReduceMotion = durationScale == 0.0f
    return isPowerSave || isReduceMotion
}

@Composable
fun TimerBackgroundView(
    backgroundType: BackgroundType,
    customWallpaperUri: String = "",
    wallpaperBlurRadius: Float = 10f,
    wallpaperDimAlpha: Float = 0.4f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val motionDisabled = remember { if (isPreview) true else isSystemReduceMotionOrPowerSaving(context) }

    Box(modifier = modifier.fillMaxSize()) {
        when (backgroundType) {
            BackgroundType.AURORA -> AuroraBackground(animate = !motionDisabled)
            BackgroundType.GENTLE_WAVES -> WavesBackground(animate = !motionDisabled)
            BackgroundType.SOFT_RAIN -> RainBackground(animate = !motionDisabled)
            BackgroundType.DRIFTING_STARS -> StarsBackground(animate = !motionDisabled)
            BackgroundType.AMOLED -> Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            BackgroundType.SOLID_SAGE -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1B2621)))
            BackgroundType.CUSTOM -> {
                if (customWallpaperUri.isNotBlank()) {
                    AsyncImage(
                        model = customWallpaperUri,
                        contentDescription = "Custom wallpaper",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141816)))
                }
            }
        }

        // Automatic contrast scrim overlay so foreground text is always WCAG AA readable
        val scrimAlpha = when (backgroundType) {
            BackgroundType.CUSTOM -> wallpaperDimAlpha.coerceIn(0.2f, 0.85f)
            BackgroundType.AMOLED -> 0f
            else -> 0.35f
        }
        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
            )
        }
    }
}

@Composable
private fun AuroraBackground(animate: Boolean) {
    val progress = if (animate) {
        val transition = rememberInfiniteTransition(label = "aurora")
        val anim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aurora_progress"
        )
        anim
    } else 0.5f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep obsidian base
        drawRect(Color(0xFF0F1412))

        // Flowing aurora ribbons
        val offset1 = Offset(w * (0.2f + 0.4f * progress), h * (0.25f + 0.1f * progress))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x553C7D64), Color(0x221B473A), Color.Transparent),
                center = offset1,
                radius = w * 0.85f
            ),
            center = offset1,
            radius = w * 0.85f
        )

        val offset2 = Offset(w * (0.8f - 0.3f * progress), h * (0.45f - 0.15f * progress))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x44265E68), Color(0x18183A42), Color.Transparent),
                center = offset2,
                radius = w * 0.75f
            ),
            center = offset2,
            radius = w * 0.75f
        )
    }
}

@Composable
private fun WavesBackground(animate: Boolean) {
    val phase = if (animate) {
        val transition = rememberInfiniteTransition(label = "waves")
        val anim by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "waves_phase"
        )
        anim
    } else 0f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF101719))

        drawSineWave(
            width = w,
            height = h,
            baseHeight = h * 0.72f,
            amplitude = 28f,
            phase = phase,
            color = Color(0x283A6B7C)
        )

        drawSineWave(
            width = w,
            height = h,
            baseHeight = h * 0.80f,
            amplitude = 22f,
            phase = phase + 1.6f,
            color = Color(0x382A5261)
        )
    }
}

private fun DrawScope.drawSineWave(
    width: Float,
    height: Float,
    baseHeight: Float,
    amplitude: Float,
    phase: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(0f, height)
        lineTo(0f, baseHeight)
        val step = 15
        for (x in 0..width.toInt() step step) {
            val y = baseHeight + amplitude * sin((x / width * 2 * PI + phase).toFloat())
            lineTo(x.toFloat(), y)
        }
        lineTo(width, height)
        close()
    }
    drawPath(path, color)
}

@Composable
private fun RainBackground(animate: Boolean) {
    val progress = if (animate) {
        val transition = rememberInfiniteTransition(label = "rain")
        val anim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rain_progress"
        )
        anim
    } else 0.5f

    // Fixed pseudo-random rain streaks
    val drops = remember {
        (0..40).map { i ->
            val xRatio = (i * 37 % 100) / 100f
            val ySpeed = 0.8f + (i * 17 % 50) / 100f
            val length = 30f + (i * 13 % 40)
            Triple(xRatio, ySpeed, length)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF121516))

        for ((xRatio, speed, len) in drops) {
            val currentY = ((progress * speed * h) % h)
            val x = xRatio * w
            drawLine(
                color = Color(0x356A8896),
                start = Offset(x, currentY),
                end = Offset(x, currentY + len),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
private fun StarsBackground(animate: Boolean) {
    val twinkle = if (animate) {
        val transition = rememberInfiniteTransition(label = "stars")
        val anim by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "twinkle"
        )
        anim
    } else 0.6f

    val stars = remember {
        (0..50).map { i ->
            val x = (i * 47 % 100) / 100f
            val y = (i * 73 % 100) / 100f
            val radius = 1.2f + (i * 11 % 20) / 10f
            Triple(x, y, radius)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF0C0E10))

        for ((xRatio, yRatio, r) in stars) {
            drawCircle(
                color = Color.White.copy(alpha = 0.35f * twinkle),
                radius = r,
                center = Offset(xRatio * w, yRatio * h)
            )
        }
    }
}
