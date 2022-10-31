package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.graphics.Turtle

sealed class LSymbol {
    companion object {
        fun parse(type: String, a: Float): LSymbol = when (type.toCharArray()[0]) {
            '!' -> SetWidth(a)
            'F' -> Forward(a)
            'f' -> ForwardNoDraw(a)
            '+' -> Plus(a)
            '&' -> And(a)
            '/' -> Over(a)
            'A' -> Apex
            '[' -> BracketL
            ']' -> BracketR
            else -> throw Exception("Error reading L-system from plaintext")
        }
    }
    object Apex : LSymbol() {
        init {
            val test = this::class
            println(test)
        }
    }
    object BracketL : LSymbol()
    object BracketR : LSymbol()
    sealed class ParametricWord(open val a : Float) : LSymbol(){
        fun withValue(a : Float) : ParametricWord =
            this::class.java.getConstructor(Float::class.java).newInstance(a)
    }
    data class SetWidth(override val a : Float) : ParametricWord(a)
    data class Forward(override val a : Float) : ParametricWord(a)
    data class ForwardNoDraw(override val a : Float) : ParametricWord(a)
    sealed interface TurtleRotation {
        val axis : Turtle.Axis
    }
    data class Plus(override val a: Float) : TurtleRotation, ParametricWord(a) {
        override val axis = Turtle.Up // turn left by angle a, see page 19 of Alg Beaut o' Plants
    }
    data class And(override val a: Float) : TurtleRotation, ParametricWord(a) {
        override val axis = Turtle.Left // pitch down using R_L
    }
    data class Over(override val a: Float) : TurtleRotation, ParametricWord(a) {
        override val axis = Turtle.Heading // roll/twist by angle a
    }
}