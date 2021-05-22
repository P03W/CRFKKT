package interpreter

data class ChronoStackEntry(
    var returnIn: Int,
    val jumpIndex: Int,
    val jumpAgain: Boolean = false,
    val origReturnIn: Int = returnIn - 1
)
