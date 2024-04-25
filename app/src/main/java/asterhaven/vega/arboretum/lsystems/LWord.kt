package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.graphics.Turtle

abstract class LWord {
    companion object {
        data class LSymbol(
            val symbol : Char,
            val params : Int,
            val desc : String
        )
        val standardSymbols = listOf(//todo extract strings, maybe associate full explanations
            LSymbol('F', 1, "Forward"),
            LSymbol('f', 1, "Forward, no draw"),
            LSymbol('!', 1, "Set line width"),
            LSymbol('+', 1, "Turn left"),
            LSymbol('&', 1, "Pitch down"),
            LSymbol('/', 1, "Roll over"),
            LSymbol('[', 0, "Save turtle state"),
            LSymbol(']', 0, "Load saved state (LIFO)")
        )
        //todo parse multiple parameters and about 8 custom symbols
        fun parse(type: String, a: Float): LWord = when (type.toCharArray()[0]) {
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