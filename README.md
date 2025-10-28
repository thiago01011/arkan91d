# arkan91d

Minimal Arkanoid (Breakout) game for Android written in Kotlin using Jetpack Compose and Canvas.

## What is this?
A tiny, readable codebase that implements the core Breakout mechanics: paddle, ball, brick grid, collisions, score, and lives. It uses Compose Canvas instead of Views/XML.

## Project structure
```
app/
  src/main/
    AndroidManifest.xml
    java/com/arkan91d/breakout/
      MainActivity.kt
      game/
        GameScreen.kt
        GameState.kt
        model/
          Paddle.kt
          Ball.kt
          Brick.kt
```

## Technologies
- Kotlin
- Jetpack Compose (Canvas, pointer input, frame clock)
- Material3 (theme wrapper)

## How to run
1. Open in Android Studio (Giraffe+ recommended).
2. Sync Gradle.
3. Run on an emulator or device (minSdk 24, targetSdk 34).

## Controls
- Drag horizontally anywhere on screen to move the paddle.

## Next steps (suggested roadmap)
- Add pause overlay and simple start/retry buttons.
- Add multiple levels and brick types (hard, power-ups).
- Add sound effects (e.g., with MediaPlayer / ExoPlayer).
- Add settings (ball speed, paddle size, lives).
- Persist high scores.

## Tutorial
See `docs/arkanoid-tutorial.md` for a step-by-step guide (each section explains Why, What, and How).
