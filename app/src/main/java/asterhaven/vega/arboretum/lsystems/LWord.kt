package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.graphics.Turtle

abstract class LWord {
    data object Apex : LWord()
    data object BracketL : LWord()
    data object BracketR : LWord()
    sealed class ParametricWord(open val a : Float) : LWord(){
        fun withValue(a : Float) : ParametricWord =
            this::class.java.getConstructor(Float::class.java).newInstance(a)
    }
    data class SetWidth(override val a : Float) : ParametricWord(a)
    data class Forward(override val a : Float) : ParametricWord(a)
    data class ForwardNoDraw(override val a : Float) : ParametricWord(a)
    sealed class TurtleRotation(override val a : Float, val axis : Turtle.Axis)
        //** Convert the actual value to radians **
        : ParametricWord(Math.PI.toFloat() * a / 180f)
    // turn left by angle a, see page 19 of Alg Beaut o' Plants
    data class Plus(override val a: Float) : TurtleRotation(a, Turtle.Up)
    // pitch down using R_L
    data class And(override val a: Float) : TurtleRotation(a, Turtle.Left)
    // roll/twist by angle a
    data class Over(override val a: Float) : TurtleRotation(a, Turtle.Heading)
}