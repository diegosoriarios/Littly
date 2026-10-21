package com.diego.littly.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object LittlySpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val gutter = 20.dp
}

object LittlyRadii {
    val feature = 28.dp
    val card = 24.dp
    val tile = 20.dp
    val icon = 16.dp
    val heatmap = 5.dp
}

object LittlySizes {
    val touchTarget = 48.dp
    val minTouchTarget = 48.dp
}

val LittlyShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
