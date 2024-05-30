package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.BuildConfig
import asterhaven.vega.arboretum.data.model.SymbolSet
import kotlin.reflect.KClass

sealed interface LSymbol {
    val symbol: String
    val nParams: Int
    val desc: String
    companion object {
        val standardSymbolLookup = SymbolSet()
        init { listOf(
            BasicLSymbol("F", 1, "Forward", LWord.Forward::class),
            BasicLSymbol("f", 1, "Forward, no draw", LWord.ForwardNoDraw::class),
            BasicLSymbol("!", 1, "Set line width", LWord.SetWidth::class),
            BasicLSymbol("+", 1, "Turn left", LWord.Plus::class),
            BasicLSymbol("&", 1, "Pitch down", LWord.And::class),
            BasicLSymbol("/", 1, "Roll over", LWord.Over::class),
            BasicLSymbol("[", 0, "Save turtle state", LWord.BracketL::class),
            BasicLSymbol("]", 0, "Load state (LIFO)", LWord.BracketR::class),
            //todo extract strings, maybe associate full explanations
        ).forEach { standardSymbolLookup.symbols[it.symbol] = it }}

        //convert compound/custom letters (poss. param'd) into sentence of LWords
        //fun parseAlias Todo

        //turn text into machine words
        fun parseStandard(sym : String, vararg params : Float, symbolSet : SymbolSet = standardSymbolLookup) : LWord {
            fun squawk() : Nothing = throw Error("Error reading L-system from plaintext\nsymbol: $sym(${params.contentToString()}) nparams:${ls?.nParams}}")
            return symbolSet.symbols[sym]!!.let { ls ->
                if (params.size == 1) ls.objectClass.constructors.first().call(params[0])
                else ls.objectClass.objectInstance ?: ls.objectClass.constructors.first()
                    .call(Float.NaN) //todo all bollocks
            }
            if (BuildConfig.DEBUG) squawk()
        }
    }
}

//"custom symbol" which notates some hopefully small sentence of others - for example ABOP pg. 7
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
) : LSymbol