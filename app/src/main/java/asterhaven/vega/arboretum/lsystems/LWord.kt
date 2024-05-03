package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.BuildConfig
import asterhaven.vega.arboretum.graphics.Turtle
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

abstract class LWord {
    data class LSymbol(
        val symbol : String,
        val params : Int,
        val desc : String,
        val objectClass : KClass<out LWord>
    ) {
        val constructor: KFunction<LWord> = objectClass.constructors.first()
        init {
            allSymbols[symbol] = this
        }
        companion object {
            val allSymbols = HashMap<String, LSymbol>()
            val standardSymbols by lazy { listOf(
                LSymbol("F", 1, "Forward", Forward::class),
                LSymbol("f", 1, "Forward, no draw", ForwardNoDraw::class),
                LSymbol("!", 1, "Set line width", SetWidth::class),
                LSymbol("+", 1, "Turn left", Plus::class),
                LSymbol("&", 1, "Pitch down", And::class),
                LSymbol("/", 1, "Roll over", Over::class),
                LSymbol("[", 0, "Save turtle state", BracketL::class),
                LSymbol("]", 0, "Load state (LIFO)", BracketR::class)
                //todo extract strings, maybe associate full explanations
            )}
            fun parse(sym: String, vararg params: Float): LWord {
                val ls = allSymbols[sym]
                return if (ls != null) {
                    if (BuildConfig.DEBUG) check(params.size == ls.params)
                    ls.constructor.call(params)
                } else {
                    if(BuildConfig.DEBUG) throw Error("Error reading L-system from plaintext")
                    Apex
                }
            }
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