class CFOptions(args: Array<String>) {
    val verbose = args.contains("-v")
    val nonZeroJumpOnEnd = args.contains("-nzr")
    val eraseAllowsJumps = args.contains("-eaj")
}
