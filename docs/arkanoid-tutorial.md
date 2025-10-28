# Minimal Arkanoid (Breakout) in Android + Kotlin + Jetpack Compose

This tutorial builds the simplest playable Arkanoid using Compose Canvas. Each step answers: Why, What, How.

## 1) Project setup
- Why: Compose provides modern, declarative UI without XML and integrates well with Kotlin.
- What: Android app in Kotlin with Jetpack Compose enabled and a single `MainActivity`.
- How: Use `buildFeatures.compose = true` and `setContent { GameScreen() }`.

## 2) Compose Canvas game loop
- Why: We need continuous updates for motion and collisions.
- What: A composable `GameScreen()` with a per-frame update driven by the frame clock.
- How: `LaunchedEffect + withFrameNanos` computes delta time and advances the world; `Canvas` draws.

```kotlin
LaunchedEffect(Unit) {
  var lastNanos = 0L
  while (true) {
    val frameTimeNanos = withFrameNanos { it }
    if (lastNanos == 0L) { lastNanos = frameTimeNanos; continue }
    val dt = ((frameTimeNanos - lastNanos).coerceAtMost(33_000_000L)).toFloat() / 1_000_000_000f
    lastNanos = frameTimeNanos
    // update world using dt
  }
}
```

## 3) Entities
- Why: Separation of concerns keeps logic readable and testable.
- What: Simple data classes `Paddle`, `Ball`, `Brick` storing pixels and velocity.
- How: Draw with `drawRect`/`drawCircle` from Compose `Canvas`.

```kotlin
data class Ball(var x: Float, var y: Float, var radius: Float, var vx: Float, var vy: Float)
```

## 4) Input
- Why: Touch drag is the most intuitive on mobile.
- What: Horizontal drag moves the paddle center to finger X.
- How: `pointerInput` + `detectDragGestures` and clamp within screen width.

```kotlin
pointerInput(Unit) {
  detectDragGestures(onDrag = { change, _ ->
    paddle = paddle.copy(x = (change.position.x - paddle.width/2f).coerceIn(0f, worldW - paddle.width))
  })
}
```

## 5) Collisions
- Why: Core gameplay loop—bounce and brick destruction.
- What: Circle vs rect nearest-point checks for paddle/bricks; wall checks.
- How: Reflect velocity on hit normal; remove bricks and update score.

```kotlin
val closestX = ball.x.coerceIn(px1, px2)
val closestY = ball.y.coerceIn(py1, py2)
val dx = ball.x - closestX
val dy = ball.y - closestY
if (dx*dx + dy*dy <= ball.radius*ball.radius) { /* reflect */ }
```

## 6) HUD and states
- Why: Player feedback and flow.
- What: Score/lives text; states: Playing, GameOver, LevelCleared.
- How: Keep state vars and render conditional messages; reset as needed.

## 7) Activity and Manifest
- Why: Minimal, single-activity setup keeps focus on the game.
- What: `MainActivity` calls `setContent { MaterialTheme { GameScreen() } }`; lock portrait.
- How: Add `android:screenOrientation="portrait"` in manifest.

## 8) Next steps
- Why: Extend replayability and polish.
- What: Power-ups, multi-hit bricks, sound, pause overlay, level loader.
- How: Add systems incrementally, keeping logic isolated and composables small.

You now have a minimal playable Arkanoid as a base to iterate on.

