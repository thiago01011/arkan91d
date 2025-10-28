package com.arkan91d.breakout.game.model

data class Paddle(
  var x: Float,
  var y: Float,
  var width: Float,
  var height: Float,
  val speedPx: Float
) {
  fun moveCenterTo(targetX: Float, screenWidth: Float) {
    val half = width / 2f
    x = (targetX - half).coerceIn(0f, (screenWidth - width))
  }

  fun centerX(): Float = x + width / 2f
}
