import androidx.compose.animation.animateColorAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun App(globalWindowState: WindowState, windowScope: FrameWindowScope) {
    MaterialTheme {
        var gameNumber by remember { mutableStateOf(1) }

        var gameType: MinesWeeperGame.GameType by remember { mutableStateOf(MinesWeeperGame.GameType.Easy) }

        var gameStatus: MinesWeeperGame.GameStatus by remember { mutableStateOf(MinesWeeperGame.GameStatus.InGame) }

        val gameTimer = remember { mutableStateOf(0) }

        val indexesPoints = remember { mutableListOf<Int>() }

        val openPoints = remember { mutableStateListOf<Int>() }

        val minePoints = remember { mutableStateListOf<Int>() }

        val markPoints = remember { mutableStateListOf<Int>() }

        val refreshGameUnit = {
            refreshFields(minePoints, indexesPoints, openPoints, markPoints)
            gameNumber++
            gameStatus = MinesWeeperGame.GameStatus.InGame
        }

        val openRadianPointsScope = rememberCoroutineScope()

        val topRowColor = animateColorAsState(
            when(gameStatus){
                MinesWeeperGame.GameStatus.Win -> Color.Green
                MinesWeeperGame.GameStatus.Losing -> Color.Red
                else -> Color.Transparent
            }
        )

        println("minePoints is ${minePoints.size} - ${minePoints.joinToString(", ")}")

        Column(
            modifier = Modifier.background(topRowColor.value).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .width(200.dp)
                    .padding(start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.Start,
                    text = "${minePoints.size - markPoints.size}"
                )
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = gameStatus.getStatusName()
                )
                Text(
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.End,
                    text = gameTimer.value.toString()
                )
            }

            for (y in 1 .. gameType.h) {
                Row{
                    for (x in 1..gameType.l) {
                        val index = remember(x, y) {
                            if (y > 9) {
                                y * 100 + x
                            } else y * 100 + x
                        }
                        val isOpen = openPoints.contains(index)
                        val isMark = markPoints.contains(index)
                        val isMine = remember(gameNumber, minePoints.size) {
                            minePoints.contains(index)
                        }

                        LaunchedEffect(gameNumber, gameType.l, gameType.h) {
                            indexesPoints.add(index)
                        }
                        val background = animateColorAsState(if (isOpen) Color.LightGray else Color.Gray)
                        println("test for in fore item $x in $y")
                        Box(
                            modifier = Modifier
                                .background(background.value)
                                .border(BorderStroke(1.dp, Color.DarkGray))
                                .size(25.dp, 25.dp)
                                .onPointerEvent(PointerEventType.Press) {
                                    if (it.buttons.isSecondaryPressed) {
                                        println("onPointerEvent to index:$index")
                                        if (!isMark && !isOpen) {
                                            markPoints.add(index)
                                            if (markPoints.containsAll(minePoints)) {
                                                gameStatus = MinesWeeperGame.GameStatus.Win
                                            }
                                        } else {
                                            markPoints.remove(index)
                                        }
                                    }
                                }
                                .combinedClickable(
                                    onClick = {
                                        if (gameStatus !is MinesWeeperGame.GameStatus.InGame) return@combinedClickable

                                        if (isMine && !isMark) {
                                            gameStatus = MinesWeeperGame.GameStatus.Losing
                                        } else if (!isOpen && !isMark) {
                                            openRadianPointsScope.launch {
                                                val startCheckTime = System.currentTimeMillis()
                                                checkAndOpenRadianPoints(
                                                    minePoints, indexesPoints,
                                                    openPoints, markPoints,
                                                    index
                                                )
                                                println("click check result time si ${System.currentTimeMillis() - startCheckTime}")
                                            }
                                        }
                                        println("click for $index it is mine $isMine")
                                    },
                                    onLongClick = {
                                        println("onPointerEvent to index:$index")
                                        if (!isMark && !isOpen) {
                                            markPoints.add(index)
                                            if (markPoints.containsAll(minePoints)) {
                                                gameStatus = MinesWeeperGame.GameStatus.Win
                                            }
                                        } else {
                                            markPoints.remove(index)
                                        }
                                    },
                                    onDoubleClick = {
                                        if (isOpen) {
                                            openRadianPointsScope.launch {
                                                checkAndOpenRadianPoints(
                                                    minePoints, indexesPoints,
                                                    openPoints, markPoints,
                                                    index, isAutoOpen = true
                                                )
                                            }
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val minesCount = remember(gameNumber, index, minePoints.size) {
                                searchMines(minePoints, index)
                            }
                            if (isOpen) {
                                if (isMine) {
                                    Text(
                                        text = "*",
                                        color = Color.Red,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                } else {
                                    Text(
                                        modifier = Modifier.padding(0.dp),
                                        text = if (minesCount > 0) "$minesCount"
                                        else "",
                                        color = Color.Blue,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else if (isMark) {
                                Text(
                                    modifier = Modifier.padding(0.dp),
                                    text = "M",
                                    color = Color.Yellow
                                )
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(gameNumber, gameStatus){
            when(gameStatus){
                MinesWeeperGame.GameStatus.InGame -> {

                }
                else -> {
                    openPoints.addAll(indexesPoints)
                }
            }
        }
        LaunchedEffect(gameNumber, gameType.h, gameType.l){
            minePoints.addAll(generateMine(indexesPoints, gameType))
        }
        LaunchedEffect(gameType.h, gameType.l){
            globalWindowState.size = DpSize.Unspecified
        }

        windowScope.MenuBar {
            Menu("Игра") {
                Item("Новая игра", onClick = {
                    refreshGameUnit()
                })
                Menu(
                    "Сложность"
                ){
                    Item(MinesWeeperGame.GameType.Easy.getName(), onClick = {
                        gameType = MinesWeeperGame.GameType.Easy
                        refreshGameUnit()
                    })
                    Item(MinesWeeperGame.GameType.Medium.getName(), onClick = {
                        gameType = MinesWeeperGame.GameType.Medium
                        refreshGameUnit()
                    })
                    Item(MinesWeeperGame.GameType.Hard.getName(), onClick = {
                        gameType = MinesWeeperGame.GameType.Hard
                        refreshGameUnit()
                    })
                }
                Item("Выход", onClick = {
                })
            }
        }
    }
}

private fun refreshFields(
    minePoints: MutableList<Int>,
    indexesPoints: MutableList<Int>,
    openPoints: MutableList<Int>,
    markPoints: MutableList<Int>
) {
    indexesPoints.clear()
    openPoints.clear()
    markPoints.clear()
    minePoints.clear()
}

private fun generateMine(listIndexes: List<Int>, gameType: MinesWeeperGame.GameType): List<Int> {
    val mines = mutableListOf<Int>()

    repeat(gameType.getMineCount()){
        checkAndAddMine(mines, listIndexes)
    }

    println("generateMine result count ${mines.size} is $mines")

    return mines
}

private fun checkAndAddMine(mines: MutableList<Int>, listIndexes: List<Int>) {
    val random = (listIndexes).random()
    if (mines.contains(random)) {
        checkAndAddMine(mines, listIndexes)
    } else mines.add(random)
}

private fun searchMines(mines: List<Int>, index: Int): Int {
    val radianIndexes = getRadianIndexes(index)

    return mines.count { radianIndexes.contains(it) }
}

private fun getRadianIndexes(index: Int): List<Int> {
    return mutableListOf<Int>().apply {
        val left = index - 1
        val right = index + 1

        add(left)
        add(right)

        add(left - 100)
        add(index - 100)
        add(right - 100)

        add(left + 100)
        add(index + 100)
        add(right + 100)
    }
}

private fun checkAndOpenRadianPoints(
    mines: List<Int>,
    listIndexes: List<Int>,
    openIndexes: MutableList<Int>,
    markIndexes: List<Int>,
    index: Int,
    isCheckRadian: Boolean = true,
    isAutoOpen: Boolean = false,
    completeCheckIndexes: MutableSet<Int> = mutableSetOf()
) {

    if (mines.contains(index)) {
        return
    } else {
        if(!openIndexes.contains(index)) openIndexes.add(index)
    }

    if (isCheckRadian) {
        val radianIndexes = getRadianIndexes(index)

        if (isAutoOpen) {
            val minesInRadian = mines.filter { radianIndexes.contains(it) }.sorted()
            val markMinesInRadian = markIndexes.filter { radianIndexes.contains(it) }.sorted()

            if (minesInRadian != markMinesInRadian) return
        }

        radianIndexes.filterNot { completeCheckIndexes.contains(it) }.forEach { checkIndex ->
            completeCheckIndexes.add(checkIndex)
            if (
                listIndexes.contains(checkIndex) && searchMines(mines, checkIndex) == 0
                && !mines.contains(checkIndex) && !openIndexes.contains(checkIndex) && !markIndexes.contains(checkIndex)
            ) {
                checkAndOpenRadianPoints(mines, listIndexes, openIndexes, markIndexes, checkIndex)
            } else if (listIndexes.contains(checkIndex) && searchMines(mines, checkIndex) > 0
                && !mines.contains(checkIndex) && !openIndexes.contains(checkIndex) && !markIndexes.contains(checkIndex)
            ) {
                checkAndOpenRadianPoints(mines, listIndexes, openIndexes, markIndexes, checkIndex, false)
            }
        }
    }
}

internal class MinesWeeperGame{
    sealed class GameType(val h: Int, val l: Int) {
        object Easy : GameType(8, 8)
        object Medium : GameType(16, 16)
        object Hard : GameType(16, 32)

        fun getMineCount(): Int {
            return when(this){
                Easy -> 10
                Medium -> 40
                Hard -> 99
            }
        }
        fun getName(): String {
            return when(this){
                Easy -> "Новичек"
                Medium -> "Любитель"
                Hard -> "Профессионал"
            }
        }
    }

    sealed interface GameStatus{
        object InGame : GameStatus
        object Win : GameStatus
        object Losing : GameStatus

        fun getStatusName(): String {
            return when (this) {
                InGame -> "В игре"
                Win -> "Победа!"
                Losing -> "Поражение"
            }
        }
    }
    data class MinerPoint(
        val x: Int,
        val y: Int,
        var isMine: Boolean = false,
        var isOpen: Boolean = false,
        val index: String = "$x$y"
    )
}

fun main() {

    val globalWindowState = WindowState(size = DpSize.Unspecified)

    singleWindowApplication(
        title = "MineSweeper",
        state = globalWindowState
    ) {
        App(globalWindowState, this)
    }
}