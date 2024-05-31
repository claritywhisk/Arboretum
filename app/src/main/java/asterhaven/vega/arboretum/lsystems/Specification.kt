package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.data.model.SymbolSet
import dev.nesk.akkurate.annotations.Validate

//a variable which becomes a number
@Validate data class LParameter(
    val symbol: String,
    val name: String,
    val type: ParameterType,
    val initialValue: Float
)
//a replacement rule
@Validate data class LProduction(val before: String, val after: String)
//a representation of an L-system, with metadata, input from text possibly with spaces
@Validate
data class Specification(
    val name : String, //todo metadata object
    val initial : String,
    val productions : List<LProduction> = arrayListOf(),
    val params : List<LParameter> = arrayListOf(),
    val constants : HashMap<String, Float> = HashMap(), //symbols from params
    val symbolSet : SymbolSet
) {
    fun updateConstant(symbol: String, value: Float): Boolean {
        if (constants[symbol] == value) return false
        constants[symbol] = value
        return true
    }
    fun compile(): TreeLSystem {
        fun String.remSpace() = this.filterNot { it.isWhitespace() }
        //turn text into machine words
        fun parse(sym : String, vararg params : Float) : LWord {
            val nParams = params.size
            //check the auxiliary
            val w = symbolSet.word[sym] ?: LSymbol.standardSymbols.word[sym]
            if(w != null && w.p.size == nParams)
                return if(nParams == 0) w else w.withValues(*params)
            else throw Error("Error reading L-system from plaintext\n" +
                    "symbol: $sym(${params.contentToString()}) nParams $nParams word $w expecting ${w?.p?.size} params")
        }
        val initialParsed = LList().apply {
            val m = SpecificationRegexAndValidation.patWord.matcher(initial.remSpace())
            while (m.find()) this +=
                parse(m.group(1)!!, *m.group(3)?.split(',')?.map {
                    p -> constants[p] ?: p.toFloat() //is it in constants?
                }?.toFloatArray() ?: FloatArray(0))
        }
        val rules = Array(productions.size) { rI ->
            val ruleParam = HashMap<String, Int>() //index of variable when reading before string
            fun template(raw: String, onParam: (String) -> Unit): LStr {
                val m = SpecificationRegexAndValidation.patWord.matcher(raw)
                var x = 0
                while (m.find()) x++
                m.reset()
                return LStr(x) {
                    m.find()
                    val args = m.group(3)?.split(',')
                    args?.forEach { onParam(it) }
                    //no need to parse the arguments for a template,
                    //but a placeholder (NaN) is expected
                    parse(m.group(1)!!, *FloatArray(args?.size ?: 0) { Float.NaN } )
                }
            }

            val beforeTemplate = template(productions[rI].before.remSpace()) {
                ruleParam[it] = ruleParam.size
            }
            val functions = ArrayList<(FloatArray) -> Float>()
            val afterTemplate = template(productions[rI].after.remSpace()) {
                //this is a possible area to expand logic
                //given should be numeric or defined constants, strings in 'param', or * the multiplication sign
                var coeff = 1f
                val terms: ArrayList<Int> = arrayListOf()
                it.split("*").forEach { token ->
                    if (constants.containsKey(token)) coeff *= constants[token]!!
                    else if (ruleParam.containsKey(token)) terms.add(ruleParam[token]!!)
                    else {
                        if (coeff != 1f || terms.size > 0) throw IllegalArgumentException(
                            "in RHS of production: "+ productions[rI].after +" offending token $token")
                        coeff = token.toFloat()
                    }
                }
                functions.add { paramArr: FloatArray ->
                    var a = coeff
                    terms.forEach { tI -> a *= paramArr[tI] }
                    a
                }
            }
            TreeLSystem.Rule(beforeTemplate, afterTemplate, *functions.toTypedArray())
        }
        return TreeLSystem(initialParsed, rules)
    }
}