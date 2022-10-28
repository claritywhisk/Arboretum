package asterhaven.vega.utility.shapes

import asterhaven.vega.utility.COORDS_PER_VERTEX
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

object UnitCylinder {
    private val m : HashMap<Int, Drawing> = HashMap()
    fun get(sides : Int) : Drawing {
        val entry = m[sides]
        if(entry != null) return entry
        val vertices = ByteBuffer.allocateDirect(sides * 2 * COORDS_PER_VERTEX * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                val theta = (2 * Math.PI / sides).toFloat()
                var angle = 0f
                repeat(sides){
                    val x = cos(angle)
                    val y = sin(angle)
                    put(x)
                    put(y)
                    put(0f)
                    put(x)
                    put(y)
                    put(1f)
                    angle += theta
                }
                position(0)
            }
        }
        val numDrawOrderVertices = sides * 2 * 3
        val order = ByteBuffer.allocateDirect(numDrawOrderVertices * Short.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                //put counterclockwise triangles
                val last = (sides * 2 - 1).s
                put(shortArrayOf(last, 1.s, 0.s))
                var v = 1
                repeat(sides - 1) {
                    put(shortArrayOf((v - 1).s, v.s, (v + 1).s))
                    put(shortArrayOf(v.s, (v + 2).s, (v + 1).s))
                    v += 2
                }
                put(shortArrayOf((last - 1).s, last, 0.s))
                position(0)
                //for(i in 0 until capacity()) println(this[i])
            }
        }
        //include the number of vertices in the draw order, needed by graphics call
        val d = Drawing(vertices, order, numDrawOrderVertices)
        m[sides] = d
        return d
    }
    private val Int.s get() = toShort()
}