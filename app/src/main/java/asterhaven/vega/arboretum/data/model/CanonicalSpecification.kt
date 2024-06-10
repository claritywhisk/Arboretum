package asterhaven.vega.arboretum.data.model

import asterhaven.vega.arboretum.lsystems.LParameter
import asterhaven.vega.arboretum.lsystems.LProduction
import asterhaven.vega.arboretum.lsystems.Specification
import dev.nesk.akkurate.annotations.Validate

data class CanonicalSpecification(
    val name : String, //todo metadata object
    val initial : String,
    val productions : List<LProduction> = arrayListOf(),
    val params : List<LParameter> = arrayListOf(),
    val symbolSet : CanonicalSymbolSet
) {
    constructor(s : Specification) : this(s.name, s.initial, s.productions, s.params, CanonicalSymbolSet(s.symbolSet))
}