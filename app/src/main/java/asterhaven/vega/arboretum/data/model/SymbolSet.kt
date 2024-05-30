package asterhaven.vega.arboretum.data.model

import asterhaven.vega.arboretum.lsystems.LSymbol
import asterhaven.vega.arboretum.lsystems.LWord

class SymbolSet(val aList : ArrayList<LSymbol>, val aMap : HashMap<String, LWord>) {
    //todo metadata

    companion object {
        val standard = LSymbol.standardSymbols
    }
}