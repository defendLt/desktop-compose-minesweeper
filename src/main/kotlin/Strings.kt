enum class StringValueType {
    WINDOW_TITLE,

    MENU_MAIN,
    MENU_NEW_GAME,
    MENU_DIFFICULTY,
    MENU_EXIT,

    GAME_DIFFICULTY_EASY,
    GAME_DIFFICULTY_MEDIUM,
    GAME_DIFFICULTY_HARD,

    GAME_STATUS_IN_GAME,
    GAME_STATUS_WIN,
    GAME_STATUS_LOSING
}

internal interface StringValues {
    fun get(type: StringValueType): String
}

object StringValuesRu : StringValues {
    override fun get(type: StringValueType) =
        when (type) {
            StringValueType.WINDOW_TITLE -> "MineSweeper"
            StringValueType.MENU_MAIN -> "Игра"
            StringValueType.MENU_NEW_GAME -> "Новая игра"
            StringValueType.MENU_DIFFICULTY -> "Сложность"
            StringValueType.MENU_EXIT -> "Выход"

            StringValueType.GAME_DIFFICULTY_EASY -> "Новичек"
            StringValueType.GAME_DIFFICULTY_MEDIUM -> "Любитель"
            StringValueType.GAME_DIFFICULTY_HARD -> "Профессионал"

            StringValueType.GAME_STATUS_IN_GAME -> "В игре"
            StringValueType.GAME_STATUS_WIN -> "Победа!"
            StringValueType.GAME_STATUS_LOSING -> "Поражение"
        }
}