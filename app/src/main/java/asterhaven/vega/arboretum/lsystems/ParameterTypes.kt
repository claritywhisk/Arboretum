package asterhaven.vega.arboretum.lsystems

import kotlin.math.roundToInt
import kotlin.math.sqrt

open class ParameterType(val range : ClosedFloatingPointRange<Float>){
    constructor(min : Float, max : Float) : this(min .. max)
    constructor(c : Float) : this(c..c)
}
open class IntParameterType(min : Int = 0, max : Int, private val gap : Int = 1)
    : ParameterType(min.toFloat(), max.toFloat()){
    fun rungsCount() = ((range.endInclusive.roundToInt() - range.start.roundToInt())/gap) - 1
}

class DerivationSteps(max : Int) : IntParameterType(max = max)

sealed interface MenuPT {
    val name : String
    companion object {
        val list = ArrayList<MenuPT>()
    }
}

sealed class MenuPTFloat(min : Float, max : Float, override val name : String ) : ParameterType(min, max), MenuPT {
    constructor(c : Float, name : String) : this(c, c, name)
    init { MenuPT.list.add(this) }
}
sealed class MenuPTInt(min: Int, max : Int, override val name : String ) : IntParameterType(min, max), MenuPT {
    init { MenuPT.list.add(this) }
}

//non-exhaustive list of common ranges
data object AngleAcute      : MenuPTFloat(0f, 90f, "Acute angle")
data object AngleNonReflex  : MenuPTFloat(0f, 180f, "Non-reflex angle")
data object AngleObtuse     : MenuPTFloat(90f, 180f, "Obtuse angle")
data object Arbitrary       : MenuPTFloat(Float.MIN_VALUE, Float.MAX_VALUE, "Arbitrary")
data object Constant        : MenuPTFloat(((1 + sqrt(5.0)) / 2).toFloat(), "Constant")
data object Integer : MenuPTInt(0, 10, "Integer")
data object UnitInterval     : MenuPTFloat(0f, 1f, "Unit interval")
data object SecondUnitInterval : MenuPTFloat(1f, 2f, "Second unit interval")
