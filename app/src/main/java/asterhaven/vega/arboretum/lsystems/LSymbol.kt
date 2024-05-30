package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.data.model.SymbolSet

//essentially view-level, while LWord is model
sealed interface LSymbol {
    val symbol: String
    val nParams: Int
    val desc: String
    companion object {
        val standardSymbols : SymbolSet by lazy {
            val basics = arrayOf(
                BasicLSymbol("F", 1, "Forward", LWord.Forward(0f)),
                BasicLSymbol("f", 1, "Forward, no draw", LWord.ForwardNoDraw(0f)),
                BasicLSymbol("!", 1, "Set line width", LWord.SetWidth(0f)),
                BasicLSymbol("+", 1, "Turn left", LWord.Plus(0f)),
                BasicLSymbol("&", 1, "Pitch down", LWord.And(0f)),
                BasicLSymbol("/", 1, "Roll over", LWord.Over(0f)),
                BasicLSymbol("[", 0, "Save turtle state", LWord.BracketL),
                BasicLSymbol("]", 0, "Load state (LIFO)", LWord.BracketR)
            )
            val list = ArrayList<LSymbol>(basics.size)
            val wordMap = HashMap<String, LWord>()
            basics.forEach {
                list.add(it)
                wordMap[it.symbol] = it.canonicalWord
            }
            SymbolSet(list, wordMap)
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
private data class BasicLSymbol(
    override val symbol : String,
    override val nParams: Int,
    override val desc: String,
    val canonicalWord : LWord
) : LSymbol