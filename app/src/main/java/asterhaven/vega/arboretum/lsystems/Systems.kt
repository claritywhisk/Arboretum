package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.utility.DEFAULT_STEPS
import asterhaven.vega.arboretum.utility.DEFAULT_STEPS_SLIDER_MAX
import dev.nesk.akkurate.ValidationResult

object Systems {
    val list : List<Specification> by lazy { listOf(page25, page56, page60) } //Todo from db
    private val page25 by lazy { specify {
        name("Page 25")
        constant("δ", 30f)
        initial("!(.05)F(.02)")
        production("F(x)","F(x)[+(δ)F(x)]F(x)[+(-1*δ)][F(x)]") //pg 25
    }}

    private val page56 by lazy { specify {
        name("Page 56")
        param("r₁", 0.9f, "contraction ratio for the trunk", UnitInterval)
        param("r₂", 0.6f, "contraction ratio for the branches", UnitInterval)
        param("a₀", 45f, "branching angle from the trunk", AngleAcute)
        param("a₂", 45f, "branching angle for lateral axes", AngleAcute)
        param("d", 137.5f, "divergence angle", AngleObtuse)
        param("wᵣ", 0.707f, "width decrease rate", UnitInterval)
        constant("l₀", 1f) //TODO sizing general
        constant("w₀", 10f)
        initial("A(l₀,w₀)")
        productions(
            "A(l,w)", "!(w)F(l)[&(a₀)B(l*r₂,w*wᵣ)]/(d)A(l*r₁,w*wᵣ)",
            "B(l,w)", "!(w)F(l)[-(a₂)\$C(l*r₂,w*wᵣ)]C(l*r₁,w*wᵣ)", //todo $ symbol
            "C(l,w)", "!(w)F(l)[+(a₂)\$B(l*r₂,w*wᵣ)]B(l*r₁,w*wᵣ)"
        )
    }}

    private val page60 by lazy { specify {
        name("Page 60")
        param("d₁", 94.74f, "divergence angle 1", AngleNonReflex)
        param("d₂", 132.63f, "divergence angle 2", AngleNonReflex)
        param("a", 18.95f, "branching angle", AngleAcute)
        param("lᵣ", 1.109f, "elongation rate", SecondUnitInterval)
        param("vᵣ", 1.732f, "width increase rate", SecondUnitInterval)
        constant("w₀", .0035f) //TODO sizing
        constant("h₀", .29f)
        initial("!(w₀)F(h₀)/(45)A")
        productions( //Todo this was not faithful to the book in order to be visible!!
            "A","!(w₀*vᵣ)F(.25*h₀)[&(a)F(.25*h₀)A]/(d₁)[&(a)F(.25*h₀)A]/(d₂)[&(a)F(.25*h₀)A]",
            "F(l)","F(l*lᵣ)",
            "!(w)","!(w*vᵣ)"
        )
    }}
}

//convenience for writing in Systems.kt
class SpecificationBuilder {
    private val cons = HashMap<String, Float>()
    private var i = ""
    private var n = ""
    private val pa = arrayListOf<LParameter>()
    private val pr = arrayListOf<LProduction>()
    private var hasSteps = false
    private var nonbasicSymbols = SymbolSet()
    //the tests in Systems.kt also have non-default symbols like apex A that won't have been processed by the rules UI
    private fun parseForNonbasicSymbol(str : String) {
        fun String.remSpace() = this.filterNot { it.isWhitespace() }
        val m = SpecificationRegexAndValidation.patWord.matcher(str.remSpace())
        while (m.find()) {
            val sym = m.group(1)!!
            if(SymbolSet.standard.word.containsKey(sym)
                || nonbasicSymbols.word.containsKey(sym))
                continue
            val nArgs = m.group(3)?.split(',')?.size ?: 0
            val viewSymbol = IntermediateSymbol(sym, nArgs, "from a test specification")
            nonbasicSymbols.add(viewSymbol)
        }
    }
    fun initial(s : String){
        i = s
        parseForNonbasicSymbol(s)
    }
    fun param(symbol: String, value : Float, name : String = "", type : ParameterType){
        pa.add(LParameter(symbol, name, type, value))
        cons[symbol] = value
        if(type is DerivationSteps) hasSteps = true
    }
    fun constant(symbol : String, value : Float, name : String = "") =
        param(symbol, value, name, Constant(value))
    fun name(name : String){ n = name }
    fun production(vararg s : String) = productions(*s)
    fun productions(vararg s : String){
        Array(s.size / 2){
            LProduction(s[it * 2], s[it * 2 + 1])
        }.forEach {
            pr.add(it)
        }
        s.forEach { parseForNonbasicSymbol(it) }
    }
    fun addStepsIfMissing() {
        if(!hasSteps) param("", DEFAULT_STEPS.toFloat(), "Steps",
            DerivationSteps(DEFAULT_STEPS_SLIDER_MAX))
    }
    fun build() = Specification(n, i, pr, pa, nonbasicSymbols, cons)
}

fun specify(lambda : SpecificationBuilder.()-> Unit) : Specification {
    val spec = SpecificationBuilder().apply {
        lambda()
        addStepsIfMissing()
    }.build()
    when (val result = SpecificationRegexAndValidation.validateSpecification(spec)) {
        is ValidationResult.Failure -> {
            val s = result.violations.joinToString { cvs -> "  - ${cvs.path}: ${cvs.message}" }
            throw IllegalArgumentException("Systems.kt validation \n$s")
        }
        is ValidationResult.Success -> {}
    }
    return spec
}

