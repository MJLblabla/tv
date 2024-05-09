import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TwoArcLoading(modifier: Modifier, start: Boolean) {
    val width = remember { mutableStateOf(800f) }
    val height = remember { mutableStateOf(800f) }
    val centerX = width.value / 2
    val centerY = height.value / 2
    val radius = centerX.coerceAtLeast(centerY) - 50f

    var angleDiffValue = 0f
    var circleSizeValue = 110f

    if (start) {
        val transition = rememberInfiniteTransition()
        angleDiffValue = transition.animateFloat(
            0f, 360f, animationSpec = InfiniteRepeatableSpec(
                tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        ).value
        circleSizeValue = transition.animateFloat(
            100f, 120f, animationSpec = InfiniteRepeatableSpec(
                tween(durationMillis = 1000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
            )
        ).value
    }


    Canvas(
        modifier = modifier.padding(10.dp)
    ) {
        width.value = size.width
        height.value = size.height
        drawArc(
            Color.White,
            startAngle = 0f + angleDiffValue,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(10f, cap = StrokeCap.Round)
        )
        drawArc(
            Color.White,
            startAngle = 180f + angleDiffValue,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(10f, cap = StrokeCap.Round)
        )

        drawCircle(
            color = Color.White,
            center = Offset(centerX, centerY),
            style = Stroke(10f),
            radius = circleSizeValue
        )
    }
}