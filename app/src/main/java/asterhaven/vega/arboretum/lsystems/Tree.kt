package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.graphics.Turtle
import asterhaven.vega.arboretum.graphics.draw.TreeGraphics
import asterhaven.vega.arboretum.utility.Matrix4X4
import asterhaven.vega.arboretum.utility.UnitVector
import asterhaven.vega.arboretum.utility.Vector
import asterhaven.vega.arboretum.utility.shapes.CommonShape

class Tree(
    private val baseXYZ : Vector,
    private val up : UnitVector,
    private val system : TreeLSystem, n : Int
) {
    constructor(b: Vector, u: UnitVector, s: TreeLSystem) : this(b, u, s, 0)

    // number of steps grown & the string representing the mathematical structure there
    private var mathematicalAge = n
    private val string
        get() = system.valueForStep(mathematicalAge)

    // "wireframe" object before cylinder resolution
    data class Structure(val branches : ArrayList<CommonShape>, val leaves : ArrayList<CommonShape>)
    private val recompute = Structure(arrayListOf(), arrayListOf())
    private var structure: Structure = recompute
        get() {
            if (field == recompute) field = Turtle.graphTree(string, baseXYZ, up)
            return field
        }

    fun grow() {
        mathematicalAge++
        structure = recompute
    }

    fun draw(mvpMatrix: Matrix4X4) {
        TreeGraphics.draw(structure.branches, mvpMatrix)
    }
}