package asterhaven.vega.arboretum.lsystems

class SymbolSet(
    val list : ArrayList<LSymbol> = ArrayList(),
    val word : HashMap<String, LWord> = HashMap()
) {
    companion object {
        val standard = LSymbol.standardSymbols
    }
    fun add(s : LSymbol) : Boolean {
        if(word.containsKey(s.symbol)) return false
        list.add(s)
        val paramsTemplate = FloatArray(s.nParams) { Float.NaN }
        word[s.symbol] = LWord.OtherWord(s.symbol, *paramsTemplate)
        return true
    }
    fun initBuildStandardAdd(s : BasicLSymbol, w : LWord) {
        list.add(s)
        word[s.symbol] = w
    }
}