package com.arkan91d.breakout.game.model

data class Brick(
  var x: Float,
  var y: Float,
  var width: Float,
  var height: Float,
  var alive: Boolean = true
)
