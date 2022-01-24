package ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object MineSweeperStyles{

    // size
    val windowPaddingSize = 8.dp
    val topMenuHeightSize = 42.dp
    val topMenuWidthSize = 200.dp
    val cellSize = 25.dp
    val cellBorderSize = 1.dp

    // colors
    val inGameBackground = Color.Transparent
    val winGameBackground = Color.Green
    val loseGameBackground = Color.Red
    val cellBorderColor = Color.DarkGray
    val cellCloseColor = Color.Gray
    val cellOpenColor = Color.LightGray
    val cellFontColor = Color.Blue

    // icons
    const val cellIsBombIconSrc = "assets/icon-bomb.png"
    const val cellIsMarkIconSrc = "assets/icon-flag.png"
}