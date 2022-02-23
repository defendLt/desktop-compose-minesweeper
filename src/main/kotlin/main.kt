import androidx.compose.animation.animateColorAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.*
import ui.GameGrid
import ui.MineSweeperStyles

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
@Preview
private fun GameApp(
    globalWindowState: WindowState,
    windowScope: FrameWindowScope,
    minesWeeperGame: MinesWeeperGame,
    onCloseRequest: () -> Unit
) = MaterialTheme {

    val gameState: MinesWeeperGame.GameState by minesWeeperGame.gameState.collectAsState()

    val gameTimer: Int by gameState.gameTimer.timerStateFlow.collectAsState(0)
    val gameNumber = gameState.gameNumber
    val gameType = gameState.gameType
    val gameStatus = gameState.gameStatus

    val topRowColor = animateColorAsState(
        when(gameStatus){
            MinesWeeperGame.GameStatus.Win -> MineSweeperStyles.winGameBackground
            MinesWeeperGame.GameStatus.Losing -> MineSweeperStyles.loseGameBackground
            else -> MineSweeperStyles.inGameBackground
        }
    )

    Column(
        modifier = Modifier.background(topRowColor.value).padding(MineSweeperStyles.windowPaddingSize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TopMenu(
            minesCount = "${gameState.minePoints}",
            statusName = gameStatus.getStatusName(),
            timerValue = gameTimer.toString()
        )

        GameGrid(gameNumber, gameType, gameState){ onClickSellEvent ->
            when(onClickSellEvent){
                is OnCellClickEvent.OnSingleClick -> minesWeeperGame.openPoint(onClickSellEvent.index)
                is OnCellClickEvent.OnLongClick -> minesWeeperGame.markPoint(onClickSellEvent.index)
                is OnCellClickEvent.OnDoubleClick -> minesWeeperGame.openRadianPoints(onClickSellEvent.index)
            }
        }
    }

    val density = LocalDensity.current
    LaunchedEffect(gameType.h, gameType.w){
        density.run {
            val width = (gameType.w * MineSweeperStyles.cellSize) + MineSweeperStyles.windowPaddingSize * 2
            val height = (gameType.h * MineSweeperStyles.cellSize) +
                    MineSweeperStyles.windowPaddingSize * 4 + MineSweeperStyles.topMenuHeightSize

            globalWindowState.size = globalWindowState.size.copy(width = width, height = height)
        }
    }

    windowScope.MenuBar {
        Menu(stringResource(StringValueType.MENU_MAIN)) {
            Item(stringResource(StringValueType.MENU_MAIN), onClick = {
                minesWeeperGame.resetGame()
            })
            Menu(
                stringResource(StringValueType.MENU_DIFFICULTY)
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
            Item(stringResource(StringValueType.MENU_EXIT), onClick = {
                onCloseRequest()
            })
        }
    }
}

fun main() {

    val globalWindowState = WindowState(size = DpSize.Unspecified)
    val minesWeeperGame = MinesWeeperGame()

    application {

        Window(
            icon = painterResource(MineSweeperStyles.cellIsMarkIconSrc),
            title = stringResource(StringValueType.WINDOW_TITLE),
            onCloseRequest = ::exitApplication,
            resizable = true,
            state = globalWindowState,
        ){
            GameApp(globalWindowState, this, minesWeeperGame, ::exitApplication)
        }
    }
}