package asterhaven.vega.arboretum.graphics

import android.opengl.Matrix
import asterhaven.vega.arboretum.lsystems.LSymbol
import asterhaven.vega.arboretum.lsystems.LSymbol.*
import asterhaven.vega.arboretum.graphics.draw.Tree
import asterhaven.vega.arboretum.utility.Matrix4X4
import asterhaven.vega.arboretum.utility.UnitVector
import asterhaven.vega.arboretum.utility.Vector
import asterhaven.vega.arboretum.utility.shapes.CommonShape

object Turtle {
    private class State(var pos : Vector, val orientation : Orientation) {
        val heading : UnitVector
            get() = orientation.heading
    }
    sealed class Axis
    object Heading : Axis()
    object Left : Axis()
    object Up : Axis()
    private class Orientation(val HLU : Matrix4X4){ //[ Heading Left Up ]
        constructor(dir : UnitVector) : this(Matrix4X4()) {
            HLU[0] = dir
            // Left should be horizontal
            val s = UnitVector(0f, 0f, -1f) // south pole maybe it doesn't matter
            val x = dir.cross(s)
            val axisL = if(x.length() != 0f) x else UnitVector(0f, 1f, 0f)
            HLU[1] = axisL
            HLU[2] = dir.cross(axisL)
        }
        val heading
            get() = UnitVector( // TODO any good?
                HLU.floatArrayValue[0],
                HLU.floatArrayValue[1],
                HLU.floatArrayValue[2]
            )
        fun performRotation(axisName : Axis, angle : Float) {
            val axis = HLU[when(axisName){
                Heading -> 0
                Left -> 1
                Up -> 2
            }]
            //TODO degrees?!? directions correct and even make sense?
            Matrix.rotateM(HLU.floatArrayValue, 0, angle, axis.x, axis.y, axis.z)
        }
    }

    fun graphTree(string : Iterable<LSymbol>, beginAt : Vector, beginFacing : UnitVector) : Tree.Structure {
        val stack : ArrayDeque<State> = ArrayDeque(listOf(State(beginAt, Orientation(beginFacing))))
        val branches : ArrayList<CommonShape> = arrayListOf()
        var width = -1f
        for(s in string) when(s){
            Apex -> continue
            BracketL -> stack.addLast(State( //push a state copy onto stack
                stack.last().pos,
                Orientation(Matrix4X4(stack.last().orientation.HLU.floatArrayValue.clone()))
            ))
            BracketR -> stack.removeLast() //...pop one
            is ParametricWord -> when(s){
                is SetWidth -> width = s.a
                is Forward, is ForwardNoDraw -> {
                    val here = stack.last()
                    if(s is Forward)
                        branches.add(CommonShape(here.pos, here.heading, s.a, width / 2f))
                    here.pos += here.heading * s.a
                }
                is TurtleRotation -> stack.last().orientation.performRotation(s.axis, s.a)
            }
        }
        return Tree.Structure(branches, arrayListOf())
    }
}