package asterhaven.vega.arboretum.lsystems

object Systems {
    val list : List<TreeLSystem.Specification> by lazy { listOf(page25, page60) }
    private val page25 by lazy { specify {
        name("Page 25")
        constant("δ", 30f)
        initial("!(.05)F(.02)")
        production("F(x)", "F(x)[+(δ)F(x)]F(x)[+(-1*δ)][F(x)]") //pg 25
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
        initial("!(w₀)F(h₀)/(45)A")
        productions(
            "A", "!(w₀*vᵣ)F(.25*h₀)[&(a)F(.25*h₀)A]/(d₁)[&(a)F(.25*h₀)A]/(d₂)[&(a)F(.25*h₀)A]",
            "F(l)", "F(l*lᵣ)",
            "!(w)", "!(w*vᵣ)"
        )
    }}
}

fun specify(lambda : TreeLSystem.Specification.()-> Unit) =
    TreeLSystem.Specification().apply(lambda)
