import androidx.compose.animation.animateColorAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import ui.GameGrid

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
@Preview
private fun App(globalWindowState: WindowState, windowScope: FrameWindowScope, minesWeeperGame: MinesWeeperGame) {
    MaterialTheme {

        val gameState: MinesWeeperGame.GameState? by minesWeeperGame.gameState.collectAsState(null)

        if(gameState == null) return@MaterialTheme

        println("trigger game state is $gameState")

        val gameNumber = gameState!!.gameNumber
        val gameType = gameState!!.gameType
        val gameStatus = gameState!!.gameStatus
        val gameTimer = remember { mutableStateOf(0) }

        val topRowColor = animateColorAsState(
            when(gameStatus){
                MinesWeeperGame.GameStatus.Win -> Color.Green
                MinesWeeperGame.GameStatus.Losing -> Color.Red
                else -> Color.Transparent
            }
        )

        Column(
            modifier = Modifier.background(topRowColor.value).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TopMenu(
                minesCount = "${gameState?.minePoints ?: gameType.getMineCount()}",
                statusName = gameStatus.getStatusName(),
                timerValue = gameTimer.value.toString()
            )

            GameGrid(gameNumber, gameType, gameState!!){ onClickSellEvent ->
                when(onClickSellEvent){
                    is OnCellClickEvent.OnSingleClick -> minesWeeperGame.openPoint(onClickSellEvent.index)
                    is OnCellClickEvent.OnLongClick -> minesWeeperGame.markPoint(onClickSellEvent.index)
                    is OnCellClickEvent.OnDoubleClick -> minesWeeperGame.openRadianPoints(onClickSellEvent.index)
                }
            }
        }

        LaunchedEffect(gameType.h, gameType.l){
            globalWindowState.size = DpSize.Unspecified
        }

        windowScope.MenuBar {
            Menu("Игра") {
                Item("Новая игра", onClick = {
                    minesWeeperGame.resetGame()
                })
                Menu(
                    "Сложность"
                ){
                    Item(MinesWeeperGame.GameType.Easy.getName(), onClick = {
                        minesWeeperGame.changeGameType(MinesWeeperGame.GameType.Easy)
                    })
                    Item(MinesWeeperGame.GameType.Medium.getName(), onClick = {
                        minesWeeperGame.changeGameType(MinesWeeperGame.GameType.Medium)
                    })
                    Item(MinesWeeperGame.GameType.Hard.getName(), onClick = {
                        minesWeeperGame.changeGameType(MinesWeeperGame.GameType.Hard)
                    })
                }
                Item("Выход", onClick = {})
            }
        }
    }
}

fun main() {

    val globalWindowState = WindowState(size = DpSize.Unspecified)
    val minesWeeperGame = MinesWeeperGame()

    singleWindowApplication(
        title = "MineSweeper",
        state = globalWindowState
    ) {
        App(globalWindowState, this, minesWeeperGame)
    }
}