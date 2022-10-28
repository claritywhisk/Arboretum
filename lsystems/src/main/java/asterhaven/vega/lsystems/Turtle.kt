package asterhaven.vega.lsystems

import asterhaven.vega.lsystems.LSymbol.*
import asterhaven.vega.utility.*
import asterhaven.vega.utility.shapes.CommonShape
import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import com.hackoeur.jglm.Vec3

object Turtle {
    private class State(var pos: Vec3, var orientation: Orientation) {
        val heading: Vec3
            get() = orientation.axisVector(Heading)
    }

    sealed class Axis
    object Heading : Axis()
    object Left : Axis()
    object Up : Axis()
    private class Orientation(val HLU: Mat4) { //[ Heading Left Up ]
        companion object {
            fun forDirection(dir: Vec3) : Orientation {
                // Left should be horizontal
                val s = Vec3(0f, 0f, -1f) // south pole maybe it doesn't matter
                val x = dir.unitVector.cross(s)
                val axisL = if (x.length != 0f) x else Vec3(0f, 1f, 0f)
                return Orientation(Mat4(dir, axisL, dir.cross(axisL), Vec3.VEC3_ZERO))
            }
        }
        fun rotated(angleDeg : AngleDeg, axis : Axis) = rotated(angleDeg.asRadians(), axis)
        fun rotated(angleRad : AngleRad, axis : Axis) : Orientation {
            val r = Matrices.rotate(angleRad.floatValue, axisVector(axis))
            return Orientation(HLU.multiply(r))
        }
        fun axisVector(axisName: Axis): Vec3 = HLU.getColumn(when (axisName) {
            Heading -> 0
            Left -> 1
            Up -> 2
        })
    }
    fun graphTree(string : Iterable<LSymbol>, beginAt : Vec3, beginFacing : Vec3) : Tree.TreeStructure {
        val stack : ArrayDeque<State> = ArrayDeque(listOf(State(beginAt, Orientation.forDirection(beginFacing))))
        val branches : ArrayList<Tree.TreePart.Branch> = arrayListOf()
        var width = -1f
        for(s in string) when(s){
            Apex -> continue
            BracketL -> stack.addLast(State( //push a state copy onto stack
                stack.last().pos,
                Orientation(Mat4(stack.last().orientation.HLU))
            ))
            BracketR -> stack.removeLast() //...pop one
            is ParametricWord -> when(s){
                is SetWidth -> width = s.a
                is Forward, is ForwardNoDraw -> {
                    val here = stack.last()
                    if(s is Forward){
                        check(width > 0f)
                        val p = CommonShape.ConstructorParams(here.pos, here.heading, s.a, width / 2f)
                        branches.add(Tree.TreePart.Branch(p))
                    }
                    here.pos = here.pos.add(here.heading.multiply(s.a))
                }
                is TurtleRotation -> stack.last().orientation = stack.last().orientation.rotated(AngleDeg(s.a), s.axis)

            }
        }
        return Tree.TreeStructure(branches, arrayListOf())
    }
}