package asterhaven.vega.arboretum.lsystems

import kotlin.math.roundToInt

open class ParameterType(val range : ClosedFloatingPointRange<Float>){
    constructor(min : Float, max : Float) : this(min .. max)
}
open class IntParameterType(min : Int = 0, max : Int, private val gap : Int = 1)
    : ParameterType(min.toFloat(), max.toFloat()){
    fun rungsCount() = ((range.endInclusive.roundToInt() - range.start.roundToInt())/gap) - 1
}

//non-exhaustive list of common ranges
object AngleAcute : ParameterType(0f, 90f)
object AngleNonReflex : ParameterType(0f, 180f)
object AngleObtuse : ParameterType(90f, 180f)
object Arbitrary : ParameterType(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
class DerivationSteps(max : Int) : IntParameterType(max = max)
class TrueConstant(c : Float) : ParameterType(c, c)
object UnitInterval : ParameterType(0f, 1f)
object SecondUnitInterval : ParameterType(1f, 2f)
