package asterhaven.vega.arboretum.data.model

import asterhaven.vega.arboretum.lsystems.LSymbol
import asterhaven.vega.arboretum.lsystems.Specification

class SymbolSet {
    val symbols : MutableCollection<LSymbol> = LinkedHashSet()
    //todo metadata

    companion object {
        val standard by lazy { SymbolSet().apply {
            symbols.addAll(LSymbol.standardSymbolLookup.values)
        }}
    }
}