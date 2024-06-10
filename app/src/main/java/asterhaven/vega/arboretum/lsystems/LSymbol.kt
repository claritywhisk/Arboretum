package asterhaven.vega.arboretum.lsystems

//essentially view-level, while LWord is model
sealed interface LSymbol {
    val symbol: String
    val nParams: Int
    val desc: String
    companion object {
        val standardSymbols : SymbolSet by lazy {
            SymbolSet().apply {
                val nan = Float.NaN
                arrayOf(
                    BasicLSymbol("F", 1, "Forward", LWord.Forward(nan)),
                    BasicLSymbol("f", 1, "Forward, no draw", LWord.ForwardNoDraw(nan)),
                    BasicLSymbol("!", 1, "Set line width", LWord.SetWidth(nan)),
                    BasicLSymbol("+", 1, "Turn left", LWord.Plus(nan)),
                    BasicLSymbol("&", 1, "Pitch down", LWord.And(nan)),
                    BasicLSymbol("/", 1, "Roll over", LWord.Over(nan)),
                    BasicLSymbol("[", 0, "Save turtle state", LWord.BracketL),
                    BasicLSymbol("]", 0, "Load state (LIFO)", LWord.BracketR)
                ).forEach { s -> initBuildStandardAdd(s, s.canonicalWord) }
            }
        }
    }
}

//"custom symbol" which notates some hopefully small sentence of others - for example ABOP pg. 7
data class CustomSymbol(
    override val symbol : String,
    override val nParams: Int,
    override val desc: String,
    val aliases : String
    //convert compound/custom letters (poss. param'd) into sentence of LWords
    //fun parseAlias Todo (maybe here)
) : LSymbol
//producing other symbols but not translating to LWord(s); classic example is A for Apex
data class IntermediateSymbol(
    override val symbol : String,
    override val nParams: Int,
    override val desc: String
) : LSymbol
//otherwise, base symbol directly producing 1 standard LWord
data class BasicLSymbol(
    override val symbol : String,
    override val nParams: Int,
    override val desc: String,
    val canonicalWord : LWord
) : LSymbol