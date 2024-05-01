package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.utility.DEFAULT_STEPS
import asterhaven.vega.arboretum.utility.DEFAULT_STEPS_SLIDER_MAX
import dev.nesk.akkurate.ValidationResult

object Systems {
    val list : List<Specification> by lazy { listOf(page25, page60) }
    private val page25 by lazy { specify {
        name("Page 25")
        constant("δ", 30f)
        initial(" !(.05) F(.02) ")
        production(" F(x) ", " F(x) [ +(δ) F(x) ] F(x) [ +(-1*δ) ] [ F(x) ] ") //pg 25
    }}

    private val page60 by lazy { specify {
        name("Page 60")
        param("d₁", 94.74f, "divergence angle 1", AngleNonReflex)
        param("d₂", 132.63f, "divergence angle 2", AngleNonReflex)
        param("a", 18.95f, "branching angle", AngleAcute)
        param("lᵣ", 1.109f, "elongation rate", SecondUnitInterval)
        param("vᵣ", 1.732f, "width increase rate", SecondUnitInterval)
        constant("w₀", .0035f)
        constant("h₀", .29f)
        initial(" !(w₀) F(h₀) /(45) A ")
        productions(
            " A ", " !(w₀*vᵣ) F(.25*h₀) [ &(a) F(.25*h₀) A ] /(d₁) [ &(a) F(.25*h₀) A ] /(d₂) [ &(a) F(.25*h₀) A ] ",
            " F(l) ", " F(l*lᵣ) ",
            " !(w) ", " !(w*vᵣ) "
        )
    }}
}

//convenience for writing in Systems.kt
class SpecificationBuilder {
    private val cons = HashMap<String, Float>()
    private var i = ""
    private var n = ""
    private val pa = arrayListOf<Specification.Parameter>()
    private val pr = arrayListOf<Specification.Production>()
    private var hasSteps = false
    fun initial(s : String){
        i = s
    }
    fun param(symbol: String, value : Float, name : String = "", type : ParameterType){
        pa.add(Specification.Parameter(symbol, name, type, value))
        cons[symbol] = value
        if(type is DerivationSteps) hasSteps = true
    }
    fun constant(symbol : String, value : Float, name : String = "") =
        param(symbol, value, name, TrueConstant(value))
    fun name(name : String){ n = name }
    fun production(vararg s : String) = productions(*s)
    fun productions(vararg s : String){
        Array(s.size / 2){
            Specification.Production(s[it * 2], s[it * 2 + 1])
        }.forEach {
            pr.add(it)
        }
    }
    fun addStepsIfMissing() {
        if(!hasSteps) param("", DEFAULT_STEPS.toFloat(), "Steps",
            DerivationSteps(DEFAULT_STEPS_SLIDER_MAX))
    }
    fun build() = Specification(n, i, pr, pa, cons)
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

