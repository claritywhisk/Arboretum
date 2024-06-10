package asterhaven.vega.arboretum.data.model

import asterhaven.vega.arboretum.lsystems.LSymbol
import asterhaven.vega.arboretum.lsystems.SymbolSet

data class CanonicalSymbolSet(val list : ArrayList<LSymbol> = ArrayList()) {
    //todo metadata
    constructor(s : SymbolSet) : this(s.list)
}
