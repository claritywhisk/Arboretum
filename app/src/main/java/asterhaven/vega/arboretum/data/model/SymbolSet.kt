package asterhaven.vega.arboretum.data.model

import asterhaven.vega.arboretum.lsystems.BasicLSymbol
import asterhaven.vega.arboretum.lsystems.LSymbol
import asterhaven.vega.arboretum.lsystems.LWord

class SymbolSet(
    val list : ArrayList<LSymbol> = ArrayList(),
    val word : HashMap<String, LWord> = HashMap()
) {
    constructor(css : CanonicalSymbolSet) : this(css.list)

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