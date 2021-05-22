import interpreter.CFInterpreter
import java.io.File

fun main(args: Array<String>) {
    var code = ""
    // If the first argument is -f, read from the file and strip all newlines
    if (args.isNotEmpty() && args[0] == "-f") {
        code = File(args[1]).readText(charset("UTF-8")).replace("\n", "").replace("\r", "")
    }

    // If we didn't read anything, grab it from the terminal
    if (code.isEmpty()) {
        print("Enter code: ")
        code = readLine()!!
    }

    // Build an interpreter
    val interpreter = CFInterpreter(code, CFOptions(args))

    // Run the whole program
    while (interpreter.step()) {
        doNothing()
    }

    // Dump the program memory
    println("\n" + interpreter.memory)
}
