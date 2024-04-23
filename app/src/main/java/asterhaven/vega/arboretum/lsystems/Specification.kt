package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.utility.DEFAULT_STEPS
import asterhaven.vega.arboretum.utility.DEFAULT_STEPS_SLIDER_MAX
import java.util.regex.Pattern

//a representation of an L-system, with metadata, input from text
class Specification {
    data class Parameter(val symbol : String, val name : String, val type : ParameterType, val initialValue : Float)
    data class Production(val before : String, val after : String)
    var name = ""
    val parameters = arrayListOf<Parameter>()
    private val initial = LList()
    private val constants = HashMap<String, Float>()
    val productionsRaw = ArrayList<Production>()
    var initialRaw = ""
    private companion object {
        private const val rxWord = "(.)([(](.*?)[)])?" // group 1 is symbol, group 3 is param
        private const val rxValidSentence = "($rxWord)+"
        private val patWord = Pattern.compile(rxWord)
        private val patValid = Pattern.compile(rxValidSentence)
    }
    fun initial(s : String) = ω(s)
    fun ω(s : String){
        if(initial.isNotEmpty()) throw IllegalStateException("multiple initial")
        initialRaw = s
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
            TreeLSystem.Rule(beforeTemplate, afterTemplate, *functions.toTypedArray())
        }
        return TreeLSystem(initial, *rules)
    }
}