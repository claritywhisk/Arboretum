package asterhaven.vega.lsystems

import asterhaven.vega.utility.shapes.CommonShape
import com.hackoeur.jglm.Vec3

class Tree(private val baseXYZ : Vec3, private val up : Vec3, private val system : TreeLSystem, n : Int) {
    constructor(b: Vec3, u: Vec3, s: TreeLSystem) : this(b, u, s, 0)

    // number of steps grown & the string representing the mathematical structure there
    private var mathematicalAge = n
    private val structure
        get() = system.valueForStep(mathematicalAge)

    // "wireframe" object before cylinder resolution
    private var shapes: TreeStructure? = shapes()
    private fun shapes() = Turtle.graphTree(structure, baseXYZ, up)

    fun grow() {
        mathematicalAge++
        shapes = shapes()
    }

    sealed class TreePart(params : ConstructorParams) : CommonShape(params) {
        class Branch(params: ConstructorParams) : TreePart(params)
        class Leaf(params: ConstructorParams) : TreePart(params)
    }
    data class TreeStructure(val branches : ArrayList<TreePart.Branch>, val leaves : ArrayList<TreePart.Leaf>)
}