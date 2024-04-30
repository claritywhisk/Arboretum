package asterhaven.vega.arboretum.lsystems

import dev.nesk.akkurate.annotations.Validate

//a representation of an L-system, with metadata, input from text with spaces
@Validate
data class Specification(
    val constants: HashMap<String, Float> = HashMap(),
    val initial : String,
    val name : String, //todo metadata object
    val params : ArrayList<Parameter> = arrayListOf(),
    val productions : ArrayList<Production> = arrayListOf()
) {
    @Validate data class Parameter(
        val symbol: String,
        val name: String,
        val type: ParameterType,
        val initialValue: Float
    )
    @Validate data class Production(
        val before: String,
        val after: String
    )
    fun updateConstant(symbol: String, value: Float): Boolean {
        if (constants[symbol] == value) return false
        constants[symbol] = value
        return true
    }
    fun compile(): TreeLSystem {
        fun String.remSpace() = this.filterNot { it.isWhitespace() }
        val initialParsed = LList().also {
            val m = SpecificationRegexAndValidation.patWord.matcher(initial.remSpace())
            while (m.find()) {
                val a = when (val p = m.group(3)) {
                    null -> Float.NaN
                    else -> constants[p] ?: p.toFloat()
                }
                it += LWord.parse(m.group(1)!!, a)
            }
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
                    val p = m.group(3)
                    if (p != null) onParam(p)
                    LWord.parse(m.group(1)!!, Float.NaN)
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
                val terms: ArrayList<Int> = arrayListOf() //
                //todo review. need to split by comma first
                it.split("*").forEach { token ->
                    if (constants.containsKey(token)) coeff *= constants[token]!!
                    else if (ruleParam.containsKey(token)) terms.add(ruleParam[token]!!)
                    else {
                        if (coeff != 1f || terms.size > 0) throw IllegalArgumentException("in RHS of production")
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