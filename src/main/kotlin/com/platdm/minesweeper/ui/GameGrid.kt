package com.platdm.minesweeper.ui

import com.platdm.minesweeper.MinesWeeperGame
import com.platdm.minesweeper.OnCellClickEvent
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
import com.platdm.minesweeper.model.MinerPoint

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun GameGrid(
    gameNumber: Int,
    difficultyType: MinesWeeperGame.DifficultyType,
    minerPoints: Map<Int, MinerPoint>,
    onClickEventListener: (OnCellClickEvent) -> Unit
){
    for (y in 1..difficultyType.h) {
        Row {
            for (x in 1..difficultyType.w) {
                val index = remember(x, y) {
                    if (y > 9) {
                        y * 100 + x
                    } else y * 100 + x
                }

                val point = remember(gameNumber, index) {
                    minerPoints[index]!!
                }

                val rememberUpdateState = remember {
                    Modifier.combinedClickable(
                        onClick = {
                            onClickEventListener(OnCellClickEvent.OnSingleClick(index))
                        },
                        onLongClick = {
                            onClickEventListener(OnCellClickEvent.OnLongClick(index))
                        },
                        onDoubleClick = {
                            onClickEventListener(OnCellClickEvent.OnDoubleClick(index))
                        }
                    ).onPointerEvent(PointerEventType.Press) {
                        if (it.buttons.isSecondaryPressed) {
                            onClickEventListener(OnCellClickEvent.OnLongClick(index))
                        }
                    }
                }

                GameSell(gameNumber,
                    index,
                    point.radianMineCount,
                    point.isOpen,
                    point.isMine,
                    point.isMark,
                    combinedClickable = rememberUpdateState
                )
            }
        }
    }
}

@Composable
internal fun GameSell(
    gameNumber: Int,
    index: Int,
    radianMineCount: Int,
    isOpen: Boolean = false,
    isMine: Boolean = false,
    isMark: Boolean = false,
    combinedClickable: Modifier
) {
    val background = animateColorAsState(if (isOpen) MineSweeperStyles.cellOpenColor else MineSweeperStyles.cellCloseColor)

    Box(
        modifier = combinedClickable
            .background(background.value)
            .border(BorderStroke(MineSweeperStyles.cellBorderSize, MineSweeperStyles.cellBorderColor))
            .size(MineSweeperStyles.cellSize, MineSweeperStyles.cellSize),
        contentAlignment = Alignment.Center
    ) {
        val minesCount = remember(gameNumber, index) {
            if (radianMineCount > 0) "$radianMineCount"
            else ""
        }
        if (isOpen && isMine) {
            Mine()
        } else if (isOpen) {
            Open(minesCount)
        } else if (isMark) {
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