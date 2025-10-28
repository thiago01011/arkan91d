package com.arkan91d.breakout.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import com.arkan91d.breakout.game.model.Ball
import com.arkan91d.breakout.game.model.Brick
import com.arkan91d.breakout.game.model.Paddle

@Composable
fun GameScreen() {
  var state by remember { mutableStateOf(GameState.Playing) }
  var score by remember { mutableStateOf(0) }
  var lives by remember { mutableStateOf(3) }

  var worldW by remember { mutableStateOf(0f) }
  var worldH by remember { mutableStateOf(0f) }

  // Entities
  var paddle by remember { mutableStateOf(Paddle(0f, 0f, 0f, 0f, 1200f)) }
  var ball by remember { mutableStateOf(Ball(0f, 0f, 10f, 360f, -360f)) }
  var bricks by remember { mutableStateOf(listOf<Brick>()) }

  fun resetLevel(w: Float, h: Float) {
    worldW = w; worldH = h
    val paddleW = w * 0.22f
    val paddleH = h * 0.02f
    paddle = Paddle(
      x = (w - paddleW) / 2f,
      y = h - paddleH * 3f,
      width = paddleW,
      height = paddleH,
      speedPx = 1200f
    )
    val r = (h * 0.012f).coerceAtLeast(8f)
    ball = Ball(x = w * 0.5f, y = h * 0.6f, radius = r, vx = 360f, vy = -360f)

    // Build simple brick grid
    val cols = 7
    val rows = 5
    val margin = w * 0.02f
    val totalMarginX = margin * (cols + 1)
    val brickW = (w - totalMarginX) / cols
    val brickH = h * 0.03f
    val top = h * 0.1f
    bricks = (0 until rows).flatMap { rIdx ->
      (0 until cols).map { cIdx ->
        val bx = margin + cIdx * (brickW + margin)
        val by = top + rIdx * (brickH + margin)
        Brick(bx, by, brickW, brickH, true)
      }
    }
    state = GameState.Playing
  }

  LaunchedEffect(Unit) {
    var lastNanos = 0L
    while (true) {
      val frameTimeNanos = withFrameNanos { it }
      if (lastNanos == 0L) { lastNanos = frameTimeNanos; continue }
      val dt = ((frameTimeNanos - lastNanos).coerceAtMost(33_000_000L)).toFloat() / 1_000_000_000f
      lastNanos = frameTimeNanos
      if (state == GameState.Playing) {
        // Update ball
        ball = ball.copy(
          x = ball.x + ball.vx * dt,
          y = ball.y + ball.vy * dt
        )

        // Walls
        if (ball.x - ball.radius < 0f) { ball = ball.copy(x = ball.radius, vx = -ball.vx) }
        if (ball.x + ball.radius > worldW) { ball = ball.copy(x = worldW - ball.radius, vx = -ball.vx) }
        if (ball.y - ball.radius < 0f) { ball = ball.copy(y = ball.radius, vy = -ball.vy) }

        // Bottom: lose life
        if (ball.y - ball.radius > worldH) {
          lives -= 1
          if (lives <= 0) {
            state = GameState.GameOver
          } else {
            // Reset ball
            ball = ball.copy(x = worldW * 0.5f, y = worldH * 0.6f, vx = 360f, vy = -360f)
          }
        }

        // Paddle collision (simple AABB vs circle)
        val px1 = paddle.x
        val py1 = paddle.y
        val px2 = paddle.x + paddle.width
        val py2 = paddle.y + paddle.height
        val closestX = ball.x.coerceIn(px1, px2)
        val closestY = ball.y.coerceIn(py1, py2)
        val dx = ball.x - closestX
        val dy = ball.y - closestY
        if (dx * dx + dy * dy <= ball.radius * ball.radius && ball.vy > 0f) {
          // Reflect up and add some angle based on hit position
          val hit = ((ball.x - paddle.centerX()) / (paddle.width / 2f)).coerceIn(-1f, 1f)
          val speed = kotlin.math.hypot(ball.vx.toDouble(), ball.vy.toDouble()).toFloat()
          val angle = (kotlin.math.PI.toFloat() * 0.25f) * hit // +/- 45°
          val newVx = speed * kotlin.math.sin(angle)
          val newVy = -kotlin.math.abs(speed * kotlin.math.cos(angle))
          ball = ball.copy(y = py1 - ball.radius - 1f, vx = newVx, vy = newVy)
        }

        // Bricks
        if (bricks.any { it.alive }) {
          val updated = bricks.map { b ->
            if (!b.alive) b else {
              val bx1 = b.x; val by1 = b.y; val bx2 = b.x + b.width; val by2 = b.y + b.height
              val cx = ball.x.coerceIn(bx1, bx2)
              val cy = ball.y.coerceIn(by1, by2)
              val ddx = ball.x - cx; val ddy = ball.y - cy
              if (ddx * ddx + ddy * ddy <= ball.radius * ball.radius) {
                // Decide reflection axis by which penetration is smaller
                val overlapX = if (ball.x < bx1) (ball.x + ball.radius) - bx1 else if (ball.x > bx2) bx2 - (ball.x - ball.radius) else kotlin.math.min(ball.x + ball.radius - bx1, bx2 - (ball.x - ball.radius))
                val overlapY = if (ball.y < by1) (ball.y + ball.radius) - by1 else if (ball.y > by2) by2 - (ball.y - ball.radius) else kotlin.math.min(ball.y + ball.radius - by1, by2 - (ball.y - ball.radius))
                if (overlapX < overlapY) {
                  ball = ball.copy(vx = -ball.vx)
                } else {
                  ball = ball.copy(vy = -ball.vy)
                }
                score += 10
                b.copy(alive = false)
              } else b
            }
          }
          bricks = updated
          if (bricks.none { it.alive }) {
            state = GameState.LevelCleared
          }
        }
      }
    }
  }

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectDragGestures(onDrag = { change, _ ->
          val x = change.position.x
          if (worldW > 0f) paddle = paddle.copy(x = (x - paddle.width / 2f).coerceIn(0f, worldW - paddle.width))
        }, onDragStart = { offset: Offset ->
          val x = offset.x
          if (worldW > 0f) paddle = paddle.copy(x = (x - paddle.width / 2f).coerceIn(0f, worldW - paddle.width))
        })
      }
  ) {
    if (worldW == 0f || worldH == 0f) {
      resetLevel(size.width, size.height)
    }

    // Clear
    drawRect(Color.Black)

    // Draw ball
    drawCircle(Color.White, radius = ball.radius, center = Offset(ball.x, ball.y))

    // Draw paddle
    drawRect(
      color = Color(0xFF00E5FF),
      topLeft = Offset(paddle.x, paddle.y),
      size = androidx.compose.ui.geometry.Size(paddle.width, paddle.height)
    )

    // Draw bricks
    bricks.forEach { b ->
      if (b.alive) drawRect(
        color = Color(0xFFFF6E40),
        topLeft = Offset(b.x, b.y),
        size = androidx.compose.ui.geometry.Size(b.width, b.height)
      )
    }

    // HUD
    drawIntoCanvas { canvas ->
      val p = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 16.dp.toPx()
        isAntiAlias = true
      }
      canvas.nativeCanvas.drawText("Score: $score   Lives: $lives", 16.dp.toPx(), 24.dp.toPx(), p)
      when (state) {
        GameState.GameOver -> canvas.nativeCanvas.drawText("Game Over", size.width/2 - 60.dp.toPx(), size.height/2, p)
        GameState.LevelCleared -> canvas.nativeCanvas.drawText("Level Cleared!", size.width/2 - 80.dp.toPx(), size.height/2, p)
        else -> {}
      }
    }
  }
}
