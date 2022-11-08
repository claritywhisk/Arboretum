package asterhaven.vega.arboretum.lsystems

object Systems {
    val page25 = specify {
        constant("δ", 30f)
        initial("!(.05)F(.02)")
        production("F(x)", "F(x)[+(δ)F(x)]F(x)[+(-1*δ)][F(x)]") //pg 25
    }

    val page60 = specify {
        constant("d1", 94.74f, "divergence angle 1")
        constant("d2", 132.63f, "divergence angle 2")
        constant("a", 18.95f, "branching angle")
        constant("lr", 1.109f, "elongation rate")
        constant("vr", 1.732f, "width increase rate")
        constant("w0", .0035f)
        constant("h0", .29f)
        initial("!(w0)F(h0)/(45)A")
        production(
            "A",
            "!(w0*vr)F(.25*h0)[&(a)F(.25*h0)A]/(d1)[&(a)F(.25*h0)A]/(d2)[&(a)F(.25*h0)A]"
        )
        production(
            "F(l)", "F(l*lr)",
            "!(w)", "!(w*vr)"
        )
    }
}

private fun specify(lambda : TreeLSystem.Specification.()-> Unit) =
    TreeLSystem.Specification().apply(lambda)
