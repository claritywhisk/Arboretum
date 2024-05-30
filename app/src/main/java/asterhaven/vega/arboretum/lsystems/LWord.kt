package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.graphics.Turtle

sealed class LWord(vararg val p : Float) {
    open fun withValues(vararg a : Float) : LWord {
        throw UnsupportedOperationException()
    }
    data object BracketL : LWord()
    data object BracketR : LWord()
    sealed class VanillaWord(val a : Float) : LWord(a) {
        override fun withValues(vararg a: Float): LWord = when(this) {
            is SetWidth -> SetWidth(a[0])
            is Forward -> Forward(a[0])
            is ForwardNoDraw -> ForwardNoDraw(a[0])
            is And ->   And(a[0])
            is Over ->  Over(a[0])
            is Plus ->  Plus(a[0])
        }
    }
    class OtherWord(val sym : String, vararg a : Float) : LWord(*a) {
        override fun withValues(vararg a: Float): LWord = OtherWord(sym, *a)
    }
    class SetWidth(a : Float) : VanillaWord(a)
    class Forward(a : Float) : VanillaWord(a)
    class ForwardNoDraw(a : Float) : VanillaWord(a)
    sealed class TurtleRotation(open val ang : Float, val axis : Turtle.Axis)
        //** Convert the actual value to radians **
        : VanillaWord(Math.PI.toFloat() * ang / 180f)
    // turn left by angle a, see page 19 of Alg Beaut o' Plants
    data class Plus(override val ang : Float) : TurtleRotation(ang, Turtle.Up)
    // pitch down using R_L
    data class And( override val ang : Float) : TurtleRotation(ang, Turtle.Left)
    // roll/twist by angle a
    data class Over(override val ang : Float) : TurtleRotation(ang, Turtle.Heading)
}