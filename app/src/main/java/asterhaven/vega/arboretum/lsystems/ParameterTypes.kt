package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.lsystems.TreeLSystem.Specification.Parameter.ParameterType

object AngleAcute : ParameterType(0f, 90f)
object AngleNonReflex : ParameterType(0f, 180f)
object Arbitrary : ParameterType(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
class TrueConstant(c : Float) : ParameterType(c, c)
object UnitInterval : ParameterType(0f, 1f)
object SecondUnitInterval : ParameterType(1f, 2f)