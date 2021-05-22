@file:Suppress("MemberVisibilityCanBePrivate")

package interpreter

import CFOptions
import exceptions.ParadoxicalStateException
import mapBrackets
import swapChars

class CFInterpreter(var code: String, val options: CFOptions) {
    private val standard = code
    private val reversed = code.swapChars('{', '}')

    var instructionPointer = 0
    var memoryPointer = 0
    var mode = Mode.FORWARD

    val stack: MutableList<ChronoStackEntry> = mutableListOf()
    val memory: MutableList<UByte> = mutableListOf(UByte.MIN_VALUE)

    val forwardJumpMap = mapBrackets(standard)
    val reverseJumpMap = mapBrackets(reversed)

    init {
        if (options.verbose) {
            println("Forward jump map: $forwardJumpMap")
            println("Reverse jump map: $reverseJumpMap")
        }
    }

    fun step(): Boolean {
        val instruction = tickExecutedInstructions(code[instructionPointer])

        if (options.verbose) println(
            "Ins. Pointer: ${
                instructionPointer.toString().padEnd(4, ' ')
            }\tNext Ins.: ${code[instructionPointer]}\t\t" +
                    "Mem. Pointer: $memoryPointer\t\t" +
                    "Cell Value: ${memoryAt()}\t\t" +
                    "Mode: $mode\t\t" +
                    "Stack Top: ${stack.lastOrNull()}"
        )

        when (instruction) {
            'R' -> {
                mode = when (mode) {
                    Mode.FORWARD -> {
                        code = standard; Mode.REVERSE
                    }
                    Mode.REVERSE -> {
                        code = reversed; Mode.FORWARD
                    }
                }
            }
            'E' -> {
                val count = memoryAt().toInt()

                // Pretend to execute `count` instructions
                val instructionPointerBefore = instructionPointer
                for (x in 0 until count) {
                    tickExecutedInstructions()
                }
                if (!options.eraseAllowsJumps) instructionPointer = instructionPointerBefore
            }
            'J' -> {
                when (mode) {
                    Mode.FORWARD -> instructionPointer += memoryAt().toInt()
                    Mode.REVERSE -> instructionPointer -= memoryAt().toInt()
                }
            }
            'W' -> {
                when (mode) {
                    Mode.FORWARD -> stack.add(
                        ChronoStackEntry(
                            memoryAt().toInt(),
                            instructionPointer + 1
                        )
                    )
                    Mode.REVERSE -> stack.add(
                        ChronoStackEntry(
                            memoryAt().toInt(),
                            instructionPointer - 1
                        )
                    )
                }
            }
            'P' -> {
                val count = memoryAt().toInt()
                when (mode) {
                    Mode.FORWARD -> {
                        stack.add(ChronoStackEntry(count + 1, instructionPointer + 1, true))
                        instructionPointer += count
                    }
                    Mode.REVERSE -> {
                        stack.add(ChronoStackEntry(count + 1, instructionPointer - 1, true))
                        instructionPointer -= count
                    }
                }
            }
            '.' -> {
                when (mode) {
                    Mode.FORWARD -> {
                        print(Char(memoryAt().toUShort()))
                        if (options.verbose) print("\n")
                    }
                    Mode.REVERSE -> {
                        if (options.verbose) print("Awaiting input: ")
                        val input = readLine()!!
                        if (input.isNotEmpty()) {
                            memory[memoryPointer] = input[0].code.toUByte()
                        } else {
                            memory[memoryPointer] = UByte.MIN_VALUE
                        }
                    }
                }
            }
            '+' -> {
                when (mode) {
                    Mode.FORWARD -> memory[memoryPointer]++
                    Mode.REVERSE -> memory[memoryPointer]--
                }
            }
            '>' -> {
                when (mode) {
                    Mode.FORWARD -> memoryPointer++
                    Mode.REVERSE -> memoryPointer--
                }
                boundsCheck()
            }
            '{', '}' -> jump(instruction)
            else -> if (options.verbose) println("Skipping unknown instruction $instruction (${instruction.code})")
        }

        when (mode) {
            Mode.FORWARD -> instructionPointer++
            Mode.REVERSE -> instructionPointer--
        }
        return canContinue()
    }

    fun memoryAt(): UByte {
        boundsCheck()
        return memory[memoryPointer]
    }

    fun boundsCheck() {
        while (memoryPointer > memory.lastIndex) {
            memory.add(UByte.MIN_VALUE)
        }
        if (memoryPointer < 0) throw ParadoxicalStateException(
            "Attempting to find forbidden knowledge is not permitted",
            "Attempted to index a negative cell"
        )
    }

    fun jump(char: Char) {
        val before = instructionPointer
        when (char) {
            '{' -> {
                if (memoryAt() == UByte.MIN_VALUE) {
                    instructionPointer = when (mode) {
                        Mode.FORWARD -> forwardJumpMap.forward[instructionPointer]!!
                        Mode.REVERSE -> reverseJumpMap.forward[instructionPointer]!!
                    }
                }
            }
            '}' -> {
                if (options.nonZeroJumpOnEnd) {
                    if (memoryAt() != UByte.MIN_VALUE) {
                        instructionPointer = when (mode) {
                            Mode.FORWARD -> forwardJumpMap.backwards[instructionPointer]!!
                            Mode.REVERSE -> reverseJumpMap.backwards[instructionPointer]!!
                        }
                    }
                } else {
                    if (memoryAt() == UByte.MIN_VALUE) {
                        instructionPointer = when (mode) {
                            Mode.FORWARD -> forwardJumpMap.backwards[instructionPointer]!!
                            Mode.REVERSE -> reverseJumpMap.backwards[instructionPointer]!!
                        }
                    }
                }
            }
            else -> throw ParadoxicalStateException(
                "Tear in space time!",
                "Invalid character $char (${char.code}) passed to jump, but like actually this shouldn't be possible what did you do"
            )
        }
        if (options.verbose && before != instructionPointer) println("Jumped from $before to $instructionPointer")
    }

    fun tickExecutedInstructions(origInstruction: Char = '\u0000'): Char {
        val possible = stack.lastOrNull()
        if (possible != null) {
            possible.returnIn--
            if (possible.returnIn <= 0) {
                stack.removeLast()

                if (possible.jumpAgain) {
                    stack.add(ChronoStackEntry(possible.origReturnIn, instructionPointer))
                }

                instructionPointer = possible.jumpIndex
                return code[instructionPointer]
            }
        }
        return origInstruction
    }

    fun canContinue(): Boolean {
        return when (mode) {
            Mode.FORWARD -> {
                if (instructionPointer < 0) throw ParadoxicalStateException(
                    "Attempting to discover The Past is not permitted",
                    "Negative instruction pointer in forward mode"
                )
                if (instructionPointer > code.lastIndex) return false
                true
            }
            Mode.REVERSE -> {
                if (instructionPointer > code.lastIndex) throw ParadoxicalStateException(
                    "Attempting to discover The Future is not permitted",
                    "Instruction pointer over length of code in reverse mode"
                )
                if (instructionPointer < 0) return false
                true
            }
        }
    }

    enum class Mode {
        FORWARD,
        REVERSE
    }
}
