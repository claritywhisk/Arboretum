package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.graphics.Turtle

sealed class LWord(vararg val p : Float) {
    open fun withValues(vararg a : Float) : LWord {
        throw UnsupportedOperationException()
    }
    open fun name() : String = this::class.simpleName ?: "unnamed?!?"
    override fun toString(): String = "${name()}(${p.contentToString()})"
    data object BracketL : LWord()
    data object BracketR : LWord()
    sealed class VanillaWord(val a : Float) : LWord(a) {
        override fun withValues(vararg a: Float): LWord = when(this) {
            is SetWidth -> SetWidth(a[0])
            is Forward -> Forward(a[0])
            is ForwardNoDraw -> ForwardNoDraw(a[0])
            is And ->   And(TurtleRotation.degFromRad(a[0])) //never forget, maybe refactor
            is Over ->  Over(TurtleRotation.degFromRad(a[0]))
            is Plus ->  Plus(TurtleRotation.degFromRad(a[0]))
        }
    }
    class OtherWord(private val sym : String, vararg a : Float) : LWord(*a) {
        override fun withValues(vararg a: Float): LWord = OtherWord(sym, *a)
        override fun name() = "OtherWord $sym"
    }
    class SetWidth(a : Float) : VanillaWord(a)
    class Forward(a : Float) : VanillaWord(a)
    class ForwardNoDraw(a : Float) : VanillaWord(a)
    sealed class TurtleRotation(open val ang : Float, val axis : Turtle.Axis)
        //** Convert the actual value to radians **
        : VanillaWord(radFromDeg(ang)) {
            companion object {
                fun radFromDeg(deg : Float) = Math.PI.toFloat() * deg / 180f
                fun degFromRad(rad : Float) = 180f * rad / Math.PI.toFloat()
            }
        }
    // turn left by angle a, see page 19 of Alg Beaut o' Plants
    data class Plus(override val ang : Float) : TurtleRotation(ang, Turtle.Up)
    // pitch down using R_L
    data class And( override val ang : Float) : TurtleRotation(ang, Turtle.Left)
    // roll/twist by angle a
    data class Over(override val ang : Float) : TurtleRotation(ang, Turtle.Heading)
}