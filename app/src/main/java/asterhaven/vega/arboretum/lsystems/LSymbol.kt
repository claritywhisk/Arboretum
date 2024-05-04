package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.BuildConfig
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

sealed interface LSymbol {
    val symbol: String
    val nParams: Int
    val desc: String
    companion object {
        val standardSymbolLookup : HashMap<String, out LSymbol> by lazy { _standardSymbolLookup }
        private val _standardSymbolLookup by lazy {
            HashMap<String, BasicLSymbol>().apply { listOf(
                BasicLSymbol("F", 1, "Forward", LWord.Forward::class),
                BasicLSymbol("f", 1, "Forward, no draw", LWord.ForwardNoDraw::class),
                BasicLSymbol("!", 1, "Set line width", LWord.SetWidth::class),
                BasicLSymbol("+", 1, "Turn left", LWord.Plus::class),
                BasicLSymbol("&", 1, "Pitch down", LWord.And::class),
                BasicLSymbol("/", 1, "Roll over", LWord.Over::class),
                BasicLSymbol("[", 0, "Save turtle state", LWord.BracketL::class),
                BasicLSymbol("]", 0, "Load state (LIFO)", LWord.BracketR::class)
                //todo extract strings, maybe associate full explanations
            ).forEach { this[it.symbol] = it }}
        }
        //convert compound/custom letters (poss. param'd) into sentence of LWords
        //fun parseAlias Todo

        //turn text into machine words
        fun parseStandard(sym : String, vararg params : Float) : LWord {
            val ls = _standardSymbolLookup[sym]
            return if (ls != null) {
                if (BuildConfig.DEBUG) check(params.size == ls.nParams)
                ls.constructor.call(params)
            } else LWord.Apex.also {
                if (BuildConfig.DEBUG) throw Error("Error reading L-system from plaintext")
            }
        }
    }
}

//"custom symbol" which notates some hopefully small sentence of others
data class CustomSymbol(
    override val symbol : String,
    override val nParams: Int,
    override val desc: String,
    val aliases : String
) : LSymbol
//producing other symbols but not translating to LWord(s); classic example is A for Apex
data class IntermediateSymbol(
    override val symbol : String,
    override val nParams: Int,
    override val desc: String
) : LSymbol
//otherwise, base symbol directly producing 1 standard LWord
private data class BasicLSymbol(
    override val symbol : String,
    override val nParams: Int,
    override val desc: String,
    val objectClass : KClass<out LWord>
) : LSymbol {
    val constructor: KFunction<LWord> = objectClass.constructors.first()
}