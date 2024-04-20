package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.utility.DEFAULT_STEPS
import asterhaven.vega.arboretum.utility.DEFAULT_STEPS_SLIDER_MAX
import java.util.regex.Pattern

/* L-Systems see e.g.
    Przemyslaw Prusinkiewicz
    Aristid Lindenmayer
    The Algorithmic Beauty of Plants
 */

private typealias LStr = Array<LSymbol>

class TreeLSystem private constructor(ω : ArrayList<LSymbol>, private vararg val prod : Rule) {
    class Specification {
        data class Parameter(val symbol : String, val name : String, val type : ParameterType, val initialValue : Float)
        var name = ""
        val parameters = arrayListOf<Parameter>()
        private data class Production(val before : String, val after : String)
        private val initial = ArrayList<LSymbol>()
        private val constants = HashMap<String, Float>()
        private val productionsRaw = ArrayList<Production>()
        private companion object {
            private const val rxWord = "(.)([(](.*?)[)])?" // group 1 is symbol, group 3 is param
            private const val rxValidSentence = "($rxWord)+"
            private val patWord = Pattern.compile(rxWord)
            private val patValid = Pattern.compile(rxValidSentence)
        }
        fun initial(s : String) = ω(s)
        fun ω(s : String){
            val m = patValid.matcher(s)
            if(!m.matches()) throw IllegalArgumentException("ωhitespace??")
            m.usePattern(patWord)
            while(m.find()) {
                val a = when(val p = m.group(3)){
                    null -> Float.NaN
                    else -> constants[p] ?: p.toFloat()
                }
                initial += LSymbol.parse(m.group(1)!!, a)
            }
        }
        fun param(symbol: String, value : Float, name : String = "", type : ParameterType){
            if(constants.containsKey(symbol)) throw IllegalArgumentException("duplicate symbol")
            if(!type.range.contains(value)) throw IllegalArgumentException("parameter value out of provided range")
            updateConstant(symbol, value)
            parameters.add(Parameter(symbol, name, type, value))
        }
        fun constant(symbol : String, value : Float, name : String = ""){
            param(symbol, value, name, TrueConstant(value))
        }
        fun steps(v : Int, max : Int = v){
            require(missingStepSuggestion)
            parameters.add(Parameter("", "Steps", DerivationSteps(max), v.toFloat()))
            missingStepSuggestion = false
        }
        fun name(name : String){
            this.name = name
        }
        private var missingStepSuggestion = true
        fun production(vararg s : String){
            if(s.size != 2) throw IllegalArgumentException()
            productions(*s)
        }
        fun productions(vararg s : String){
            if(s.size % 2 == 1) throw IllegalArgumentException()
            production(*Array(s.size / 2){
                Production(s[it * 2], s[it * 2 + 1])
            })
        }
        fun updateConstant(symbol : String, value : Float) : Boolean {
            if(constants[symbol] == value) return false
            constants[symbol] = value
            return true
        }
        private fun production(vararg p : Production){
            p.forEach {
                val m1 = patValid.matcher(it.before)
                val m2 = patValid.matcher(it.after)
                if(!m1.matches() || !m2.matches()) throw IllegalArgumentException("whitespace?")
                productionsRaw.add(it)
            }
        }
        fun compile() : TreeLSystem {
            if(missingStepSuggestion) steps(DEFAULT_STEPS, DEFAULT_STEPS_SLIDER_MAX)
            val rules = Array(productionsRaw.size) { rI ->
                val param = HashMap<String, Int>() //index of variable when reading before string
                fun template(raw : String, onParam : (String) -> Unit) : LStr {
                    val m = patWord.matcher(raw)
                    var x = 0
                    while(m.find()) x++
                    m.reset()
                    return LStr(x){
                        m.find()
                        val p = m.group(3)
                        if(p != null) onParam(p)
                        LSymbol.parse(m.group(1)!!, Float.NaN)
                    }
                }
                val beforeTemplate = template(productionsRaw[rI].before) {
                    if(param.containsKey(it)) throw Exception("Duplicate parameter")
                    if(constants.containsKey(it)) throw Exception("Constant in LHS of production")
                    param[it] = param.size
                }
                val functions = ArrayList<(FloatArray) -> Float>()
                val afterTemplate = template(productionsRaw[rI].after) {
                    //this is a possible area to expand logic
                    //given should be numeric or defined constants, strings in 'param', or * the multiplication sign
                    var coeff = 1f
                    val terms : ArrayList<Int> = arrayListOf() //
                    it.split("*").forEach { token ->
                        if(constants.containsKey(token)) coeff *= constants[token]!!
                        else if(param.containsKey(token)) terms.add(param[token]!!)
                        else {
                            if(coeff != 1f || terms.size > 0) throw IllegalArgumentException("in RHS of production")
                            coeff = token.toFloat()
                        }
                    }
                    functions.add { paramArr : FloatArray ->
                        var a = coeff
                        terms.forEach{ tI -> a *= paramArr[tI] }
                        a
                    }
                }
                Rule(beforeTemplate, afterTemplate, *functions.toTypedArray())
            }
            return TreeLSystem(initial, *rules)
        }
    }
    private class Rule(
        val beforeTemplate : LStr,
        private val afterTemplate: LStr,
        private vararg val fPWord : (FloatArray) -> Float //given the LHS params, produce each param value
    ){
        private val inParams = FloatArray(beforeTemplate.count{ it is LSymbol.ParametricWord })
        private var matchIndex = 0
        fun beginMatch() {
            matchIndex = 0
        }
        fun match(s : LSymbol) : LStr? {
            if(s::class == beforeTemplate[matchIndex]::class){
                if(s is LSymbol.ParametricWord){
                    inParams[matchIndex] = s.a
                }
                if(++matchIndex == beforeTemplate.size) {
                    matchIndex = 0
                    var iF = 0
                    return LStr(afterTemplate.size) {
                        val sym = afterTemplate[it]
                        if(sym is LSymbol.ParametricWord) sym.withValue(fPWord[iF++](inParams))
                        else sym
                    }
                }
            }
            else matchIndex = 0
            return null
        }
    }

    fun valueForStep(n : Int) : ArrayList<LSymbol> {
        if(valueForStep.size > n) valueForStep[n]?.let { return it }
        var x = valueForStep.highestStoredStep
        if(x > n) while(valueForStep[x] == null) x--
        return computeValueForStep(n, x)
    }
    private val valueForStep = object : ArrayList<ArrayList<LSymbol>?>(){
        init { add(ω) }
        var highestStoredStep = 0
        override fun set(index: Int, element: java.util.ArrayList<LSymbol>?): java.util.ArrayList<LSymbol>? {
            if(index == 0) throw UnsupportedOperationException()
            if(element != null && index > highestStoredStep) highestStoredStep = index
            if(element == null && index == highestStoredStep)
                do highestStoredStep-- while(this[highestStoredStep] == null)
            return super.set(index, element)
        }
    }
    private fun computeValueForStep(n : Int, givenStep : Int) : ArrayList<LSymbol> {
        var string = valueForStep[givenStep] ?: throw ConcurrentModificationException()
        fun clearMatch() {
            prod.forEach(Rule::beginMatch)
        }
        repeat(n - givenStep) {
            clearMatch()
            val new = ArrayList<LSymbol>()
            var l = 0
            fun takeUnmatched(j : Int) {
                for (i in l..j) new += string[i]
            }
            letter@for(r in string.indices) for (p in prod.indices)
                when(val replacement = prod[p].match(string[r])) {
                    null -> continue
                    else -> {
                        val len = prod[p].beforeTemplate.size
                        takeUnmatched(r - len )
                        l = r + 1
                        new += replacement
                        clearMatch()
                        continue@letter
                    }
                }
            takeUnmatched(string.lastIndex)
            string = new
        }
        return string
    }
}
