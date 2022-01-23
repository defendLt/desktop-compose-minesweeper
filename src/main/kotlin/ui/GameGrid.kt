package ui

import OnCellClickEvent
import model.MinerPoint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
            for (x in 1..gameType.l) {
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
    val background = animateColorAsState(if (point.isOpen) Color.LightGray else Color.Gray)

    Box(
        modifier = Modifier
            .background(background.value)
            .border(BorderStroke(1.dp, Color.DarkGray))
            .size(25.dp, 25.dp)
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
            Text(
                text = "*",
                color = Color.Red,
                fontWeight = FontWeight.ExtraBold
            )
        } else if (point.isOpen) {
            Text(
                modifier = Modifier.padding(0.dp),
                text = minesCount,
                color = Color.Blue,
                fontWeight = FontWeight.ExtraBold
            )
        } else if (point.isMark) {
            Text(
                modifier = Modifier.padding(0.dp),
                text = "M",
                color = Color.Yellow
            )
        }
    }
}