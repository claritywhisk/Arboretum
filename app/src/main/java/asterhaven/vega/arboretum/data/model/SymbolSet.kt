package asterhaven.vega.arboretum.data.model

import asterhaven.vega.arboretum.lsystems.LSymbol

class SymbolSet {
    val symbols = LinkedHashMap<String, LSymbol>()
    //todo metadata

    companion object {
        val standard = LSymbol.standardSymbolLookup
    }
}