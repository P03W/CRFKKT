import interpreter.DualIntMap

/**
 * Replaces all `a` with `b` and vice versa
 */
fun String.swapChars(a: Char, b: Char): String {
    return map {
        when (it) {
            a -> b
            b -> a
            else -> it
        }
    }.joinToString("")
}

/**
 * Generates a jump map between the pairs of brackets, defaulting to -1 if no match is found
 */
@OptIn(ExperimentalStdlibApi::class)
fun mapBrackets(input: String): DualIntMap {
    val stack: MutableList<Int> = mutableListOf()
    return DualIntMap(buildMap {
        for (i in input.indices) {
            if (input[i] == '{') {
                stack.add(i)
            } else if (input[i] == '}') {
                put(if (stack.isEmpty()) -1 else stack.removeLast(), i)
            }
        }
        stack.forEach {
            put(it, -1)
        }
    })
}

/**
 * Does nothing, used to remove a warning about empty bodies
 */
fun doNothing() {}
