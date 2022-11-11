package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.graphics.Turtle
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.TreeProgram
import asterhaven.vega.arboretum.utility.Matrix4X4
import asterhaven.vega.arboretum.utility.UnitVector
import asterhaven.vega.arboretum.utility.Vector
import asterhaven.vega.arboretum.utility.shapes.CommonShape

class Tree (
    private val baseXYZ : Vector,
    private val up : UnitVector,
    private val system : TreeLSystem,
    n : Int
) : Drawing() {
    constructor(b: Vector, u: UnitVector, s: TreeLSystem) : this(b, u, s, 0)

    // number of steps grown & the string representing the mathematical structure there
    private var mathematicalAge = 0
    init {
        grow(n)
    }

    // "wireframe" object before cylinder resolution
    data class Structure(val branches : ArrayList<CommonShape>, val leaves : ArrayList<CommonShape>)
    private var structure = Structure(arrayListOf(), arrayListOf())

    fun grow() = grow(1)
    fun grow(steps : Int){
        mathematicalAge += steps
        structure = Turtle.graphTree(system.valueForStep(mathematicalAge), baseXYZ, up)
    }

    override fun draw(mvpMatrix: Matrix4X4) {
        TreeProgram.draw(structure.branches, mvpMatrix)
    }
}