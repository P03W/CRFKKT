package interpreter

@OptIn(ExperimentalStdlibApi::class)
class DualIntMap(map: Map<Int, Int>) {
    val forward: Map<Int, Int> = map
    val backwards: Map<Int, Int> = buildMap {
        map.entries.forEach {
            put(it.value, it.key)
        }
    }

    override fun toString(): String {
        return forward.toString()
    }
}
