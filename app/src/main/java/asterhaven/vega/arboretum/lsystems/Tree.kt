package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.graphics.Turtle
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.TreeProgram
import asterhaven.vega.arboretum.utility.Matrix4X4
import asterhaven.vega.arboretum.utility.UnitVector
import asterhaven.vega.arboretum.utility.Vector
import asterhaven.vega.arboretum.utility.shapes.CommonShape
import java.lang.Float.max
import java.lang.Float.min

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
    private lateinit var structure : Structure

    fun grow() = grow(1)
    fun grow(steps : Int){
        mathematicalAge += steps
        structure = Turtle.graphTree(system.valueForStep(mathematicalAge), baseXYZ, up)
    }

    data class Measurements(val x : MinMax, val y : MinMax, val z : MinMax){
        constructor(xL : Float, xH : Float, yL : Float, yH : Float, zL : Float, zH : Float) :
            this(MinMax(xL, xH), MinMax(yL, yH), MinMax(zL, zH))
        data class MinMax(val min : Float, val max : Float)
    }
    lateinit var measurements : Measurements

    fun measure() {
        var xL = Float.MAX_VALUE
        var xH = Float.MIN_VALUE
        var yL = Float.MAX_VALUE
        var yH = Float.MIN_VALUE
        var zL = Float.MAX_VALUE
        var zH = Float.MIN_VALUE
        fun eff(cs : CommonShape){ // estimate the hi
            xL = min(xL, cs.start.x)
            xH = max(xH, cs.start.x + cs.len)
            yL = min(yL, cs.start.y)
            yH = max(yH, cs.start.y + cs.len)
            zL = min(zL, cs.start.z)
            zH = max(zH, cs.start.z + cs.len)
        }
        for(br in structure.branches) eff(br)
        for(lf in structure.leaves) eff(lf)
        measurements = Measurements(xL, xH, yL, yH, zL, zH)
    }

    override fun draw(mvpMatrix: Matrix4X4) {
        TreeProgram.draw(structure.branches, mvpMatrix)
    }
}