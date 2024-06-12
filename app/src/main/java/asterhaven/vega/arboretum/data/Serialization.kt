package asterhaven.vega.arboretum.data

import asterhaven.vega.arboretum.lsystems.AngleAcute
import asterhaven.vega.arboretum.lsystems.AngleNonReflex
import asterhaven.vega.arboretum.lsystems.AngleObtuse
import asterhaven.vega.arboretum.lsystems.Arbitrary
import asterhaven.vega.arboretum.lsystems.BasicLSymbol
import asterhaven.vega.arboretum.lsystems.Constant
import asterhaven.vega.arboretum.lsystems.Custom
import asterhaven.vega.arboretum.lsystems.CustomSymbol
import asterhaven.vega.arboretum.lsystems.DerivationSteps
import asterhaven.vega.arboretum.lsystems.Integer
import asterhaven.vega.arboretum.lsystems.IntermediateSymbol
import asterhaven.vega.arboretum.lsystems.LParameter
import asterhaven.vega.arboretum.lsystems.LProduction
import asterhaven.vega.arboretum.lsystems.SecondUnitInterval
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.lsystems.SymbolSet
import asterhaven.vega.arboretum.lsystems.UnitInterval

fun Specification.serialize() : Map<String, Any> = mapOf(
    "name" to name,
    "axiom" to initial,
    "productions" to productions.map { mapOf(
        "before" to it.before,
        "after" to it.after
    )},
    "params" to params.map { mapOf(
        "symbol" to it.symbol,
        "name" to it.name,
        "type" to when(it.type) {
            is DerivationSteps -> mapOf(
                "class" to "DerivationSteps",
                "max" to it.type.max
            )
            is Integer -> mapOf(
                "class" to "Integer",
                "min" to it.type.min,
                "max" to it.type.max
            )
            AngleAcute -> mapOf("class" to "AngleAcute")
            AngleNonReflex -> mapOf("class" to "AngleNonReflex")
            AngleObtuse -> mapOf("class" to "AngleObtuse")
            Arbitrary -> mapOf("class" to "Arbitrary")
            is Constant -> mapOf(
                "class" to "Constant",
                "value" to it.type.c
            )
            is Custom -> mapOf(
                "class" to "Custom",
                "min" to it.type.min,
                "max" to it.type.max
            )
            SecondUnitInterval -> mapOf("class" to "SecondUnitInterval")
            UnitInterval -> mapOf("class" to "UnitInterval")
        },
        "initialValue" to it.initialValue
    )},
    "symbolSetList" to nonbasicSymbols.list.map { mutableMapOf(
        "class" to when(it) {
            is CustomSymbol -> "CustomSymbol"
            is IntermediateSymbol -> "IntermediateSymbol"
            is BasicLSymbol -> throw Exception("Unexpected BS in symbols")
        },
        "sym" to it.symbol,
        "# params" to it.nParams,
        "desc" to it.desc
    ).apply{
        if(it is CustomSymbol) this["aliases"] = it.aliases
    }}
)

fun deserializeSpecification(m : Map<String, Any>) : Specification {
    val name = m["name"] as String
    val initial = m["axiom"] as String
    val productions = (m["productions"] as List<Map<String, Any>>).map {
        LProduction(it["before"] as String, it["after"] as String)
    }
    val params = (m["params"] as List<Map<String, Any>>).map { p ->
        val symbol = p["symbol"] as String
        val pName = p["name"] as String
        val type = (p["type"] as Map<String, Any>).let { t ->
            when(val className = t["class"] as String) {
                "DerivationSteps" -> DerivationSteps(longToInt(t["max"]))
                "Integer" -> Integer(longToInt(t["min"]), longToInt(t["max"]))
                "AngleAcute" -> AngleAcute
                "AngleNonReflex" -> AngleNonReflex
                "AngleObtuse" -> AngleObtuse
                "Arbitrary" -> Arbitrary
                "Constant" -> Constant(doubtFl(t["value"]))
                "Custom" -> Custom(doubtFl(t["min"]), doubtFl(t["max"]))
                "SecondUnitInterval" -> SecondUnitInterval
                "UnitInterval" -> UnitInterval
                else -> throw Exception("Unrecognized: $className")
            }
        }
        val initialValue = doubtFl(p["initialValue"])
        LParameter(symbol, pName, type, initialValue)
    }
    val symbolSet = SymbolSet()
    (m["symbolSetList"] as List<Map<String, Any>>).forEach { s ->
        val sym = when(val className = s["class"]){
            "CustomSymbol" -> CustomSymbol(
                s["sym"] as String, longToInt(s["# params"]), s["desc"] as String, s["aliases"] as String
            )
            "IntermediateSymbol" -> IntermediateSymbol(
                s["sym"] as String, longToInt(s["# params"]), s["desc"] as String
            )
            else -> throw Exception("Unexpectorecognized: $className")
        }
        symbolSet.add(sym)
    }
    val constants = HashMap<String, Float>().apply {
        params.forEach { this[it.symbol] = it.initialValue }
    }
    return Specification(name, initial, productions, params, symbolSet, constants)
}

private fun doubtFl(d : Any?) : Float = (d as Double).toFloat()
private fun longToInt(l : Any?) : Int = (l as Long).toInt()