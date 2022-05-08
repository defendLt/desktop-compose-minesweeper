package com.platdm.minesweeper

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.platdm.minesweeper.model.MinerPoint

class MinesWeeperGame(
    private val gameStateScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var step: Int = 0
    private var number: Int = 0
    private var status: Status = Status.WaitGame
    private var difficultyType: DifficultyType = DifficultyType.Easy
    private val minerPoints: MutableMap<Int, MinerPoint> = mutableMapOf()
    private var gameTimer: GameTimer = GameTimer()
    val gameTimerListener: GameTimerListener = gameTimer

    private val _gameState = MutableStateFlow(
        GameState(step, number, status, difficultyType, minePointsCount, minerPoints)
    )
    val gameState: StateFlow<GameState> get() = _gameState.asStateFlow()

    private val minePointsCount
        get() = minerPoints.count { it.value.isMine } - minerPoints.count { it.value.isMark }

    init {
        resetGame()
    }

    fun resetGame() {
        gameStateScope.launch {
            generatePoints()
            generateMines()
            recalculateRadianMineCount()
            step = -1
            status = Status.InGame
            number++
            updateGameState()
        }
    }

    fun changeGameType(difficultyType: DifficultyType){
        this.difficultyType = difficultyType
        resetGame()
    }

    fun openPoint(index: Int){
        minerPoints[index]?.let { point ->
            if (point.isMine && !point.isMark) {
                status = Status.Losing
                openAllPoints()
            } else if (!point.isOpen && !point.isMark) {
                checkAndOpenRadianPoints(index)
            }
            updateGameState()
        }
    }

    fun markPoint(index: Int){
        minerPoints[index]?.let { point ->
            if (!point.isMark && !point.isOpen) {
                point.isMark = true
                if(minerPoints.count { it.value.isMark && it.value.isMine } == difficultyType.mineCount){
                    status = Status.Win
                    openAllPoints()
                }
            } else {
                point.isMark = false
            }
            updateGameState()
        }
    }

    fun openRadianPoints(index: Int){
        if (minerPoints[index]?.isOpen == true) {
            checkAndOpenRadianPoints(index, isAutoOpen = true)
            updateGameState()
        }
    }

    private fun checkAndOpenRadianPoints(
        index: Int,
        isCheckRadian: Boolean = true,
        isAutoOpen: Boolean = false,
        completeCheckIndexes: MutableSet<Int> = mutableSetOf()
    ){
        if (minerPoints[index]?.isMine == true) {
            return
        } else {
            minerPoints[index]?.isOpen = true
        }

        if (isCheckRadian) {
            val radianIndexes = minerPoints[index]!!.radianIndexes

            if (isAutoOpen) {
                val minesInRadian =
                    minerPoints.filter { radianIndexes.contains(it.key) && it.value.isMine }.map { it.key }.sorted()
                val markInRadian =
                    minerPoints.filter { radianIndexes.contains(it.key) && it.value.isMark }.map { it.key }.sorted()
                if (minesInRadian != markInRadian) return
            }

            radianIndexes.filterNot { completeCheckIndexes.contains(it) }.forEach { checkIndex ->
                completeCheckIndexes.add(checkIndex)

                minerPoints[checkIndex]?.let { checkPoint ->

                    val isEmptyMineAndNotActivated = !checkPoint.isMine && !checkPoint.isOpen && !checkPoint.isMark

                    if (checkPoint.radianMineCount == 0 && isEmptyMineAndNotActivated) {
                        checkAndOpenRadianPoints(
                            checkIndex,
                            completeCheckIndexes = completeCheckIndexes
                        )
                    } else if (checkPoint.radianMineCount > 0 && isEmptyMineAndNotActivated) {
                        checkAndOpenRadianPoints(
                            checkIndex,
                            completeCheckIndexes = completeCheckIndexes,
                            isCheckRadian = false
                        )
                    }
                }
            }
        }
    }

    private fun generatePoints(){
        minerPoints.clear()
        for (y in 1..difficultyType.h) {
            for (x in 1..difficultyType.w){
                val index = if(y > 9){
                    y * 100 + x
                } else y * 100 + x
                minerPoints[index] = MinerPoint(index, x, y, getRadianIndexes(index))
            }
        }
    }

    private fun generateMines(){
        val mineIndexes = mutableSetOf<Int>()

        repeat(difficultyType.mineCount){
            checkAndAddMine(mineIndexes, minerPoints.keys)
        }

        mineIndexes.forEach {
            minerPoints[it]!!.isMine = true
        }
    }

    private fun checkAndAddMine(mineIndexes: MutableSet<Int>, indexesPoints: Set<Int>){
        val random = (indexesPoints).random()
        if (mineIndexes.contains(random)) {
            checkAndAddMine(mineIndexes, indexesPoints)
        } else mineIndexes.add(random)
    }

    private fun openAllPoints(){
        gameTimer.stop()
        minerPoints.forEach { (_, point) ->
            point.isOpen = true
        }
    }

    private fun getRadianIndexes(index: Int): Set<Int> {
        return mutableSetOf<Int>().apply {
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

    private fun recalculateRadianMineCount(){
        minerPoints.forEach { (_, point) ->
            if(!point.isMine) point.radianMineCount = getRadianMineCount(point.radianIndexes)
        }
    }

    private fun getRadianMineCount(indexes: Set<Int>): Int{
        return minerPoints.count { indexes.contains(it.key) && it.value.isMine }
    }

    private fun updateGameState() {
        gameStateScope.launch {
            GameState(
                ++step, number, status, difficultyType, minePointsCount, minerPoints.toMap()
            ).let {
                if(step == 1) gameTimer.start()
                _gameState.emit(it)
            }
        }
    }

    sealed class DifficultyType(val h: Int, val w: Int) {
        object Easy : DifficultyType(8, 8)
        object Medium : DifficultyType(16, 16)
        object Hard : DifficultyType(16, 32)

        val mineCount: Int
            get() = when (this) {
                Easy -> 10
                Medium -> 40
                Hard -> 99
            }

        val name: String
            get() = when (this) {
                Easy -> stringResource(StringValueType.GAME_DIFFICULTY_EASY)
                Medium -> stringResource(StringValueType.GAME_DIFFICULTY_MEDIUM)
                Hard -> stringResource(StringValueType.GAME_DIFFICULTY_HARD)
            }
    }

    sealed interface Status {
        object WaitGame : Status
        object InGame : Status
        object Win : Status
        object Losing : Status

        val name: String
            get() = when (this) {
                WaitGame -> ""
                InGame -> stringResource(StringValueType.GAME_STATUS_IN_GAME)
                Win -> stringResource(StringValueType.GAME_STATUS_WIN)
                Losing -> stringResource(StringValueType.GAME_STATUS_LOSING)
            }
    }

    data class GameState(
        val step: Int,
        val number: Int,
        val status: Status,
        val difficultyType: DifficultyType,
        val minePointsCount: Int,
        val minerPoints: Map<Int, MinerPoint>
    )

    interface GameTimerListener {
        val timerStateFlow: StateFlow<Int>
    }

    private class GameTimer(
        private val timerScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        private var timerJob: Job? = null
    ) : GameTimerListener {
        private val _timerStateFlow = MutableStateFlow(0)
        override val timerStateFlow: StateFlow<Int> get() = _timerStateFlow.asStateFlow()

        fun start() {
            stop()
            timerJob = timerScope.launch {
                var timeValue = 0
                _timerStateFlow.emit(timeValue)
                while (true) {
                    delay(1000)
                    _timerStateFlow.emit(++timeValue)
                }
            }
        }

        fun stop() {
            timerJob?.cancel()
        }
    }
}