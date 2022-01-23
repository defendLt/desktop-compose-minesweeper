package model

data class MinerPoint(
    val index: Int,
    val x: Int,
    val y: Int,
    val radianIndexes: Set<Int>,
    var radianMineCount: Int = 0,
    var isMine: Boolean = false,
    var isMark: Boolean = false,
    var isOpen: Boolean = false
)