package ui

import MinesWeeperGame
import OnCellClickEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.MinerPoint

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun GameGrid(
    gameNumber: Int,
    gameType: MinesWeeperGame.GameType,
    gameState: MinesWeeperGame.GameState,
    onClickEventListener: (OnCellClickEvent) -> Unit
){
    for (y in 1..gameType.h) {
        Row {
            for (x in 1..gameType.w) {
                val index = remember(x, y) {
                    if (y > 9) {
                        y * 100 + x
                    } else y * 100 + x
                }

                val point = remember(gameNumber, index) {
                    gameState.minerPoints[index]!!
                }

                GameSell(gameNumber, index, point, onClickEventListener)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun GameSell(
    gameNumber: Int,
    index: Int,
    point: MinerPoint,
    onClickEventListener: (OnCellClickEvent) -> Unit
) {
    val background = animateColorAsState(if (point.isOpen) MineSweeperStyles.cellOpenColor else MineSweeperStyles.cellCloseColor)

    Box(
        modifier = Modifier
            .background(background.value)
            .border(BorderStroke(MineSweeperStyles.cellBorderSize, MineSweeperStyles.cellBorderColor))
            .size(MineSweeperStyles.cellSize, MineSweeperStyles.cellSize)
            .onPointerEvent(PointerEventType.Press) {
                if (it.buttons.isSecondaryPressed) {
                    onClickEventListener(OnCellClickEvent.OnLongClick(index))
                }
            }
            .combinedClickable(
                onClick = {
                    onClickEventListener(OnCellClickEvent.OnSingleClick(index))
                },
                onLongClick = {
                    onClickEventListener(OnCellClickEvent.OnLongClick(index))
                },
                onDoubleClick = {
                    onClickEventListener(OnCellClickEvent.OnDoubleClick(index))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        val minesCount = remember(gameNumber, index) {
            if (point.radianMineCount > 0) "${point.radianMineCount}"
            else ""
        }
        if (point.isOpen && point.isMine) {
            Mine()
        } else if (point.isOpen) {
            Open(minesCount)
        } else if (point.isMark) {
            Flag()
        }
    }
}

@Composable
internal fun Open(minesCount: String){
    Text(
        modifier = Modifier.padding(0.dp),
        text = minesCount,
        color = MineSweeperStyles.cellFontColor,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
internal fun Mine(){
    Image(
        painter = painterResource(MineSweeperStyles.cellIsBombIconSrc),
        contentDescription = "",
        modifier = Modifier.fillMaxSize()
    )
}
@Composable
internal fun Flag(){
    Image(
        painter = painterResource(MineSweeperStyles.cellIsMarkIconSrc),
        contentDescription = "",
        modifier = Modifier.fillMaxSize()
    )
}