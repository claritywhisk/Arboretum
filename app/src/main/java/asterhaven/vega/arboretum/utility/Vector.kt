package asterhaven.vega.arboretum.utility

import kotlin.math.PI

const val COORDS_PER_VERTEX = 3

@JvmInline
value class AngleDeg(val floatValue : Float){
    fun asRadians() = AngleRad((PI * floatValue / 180f).toFloat())
}
@JvmInline
value class AngleRad(val floatValue : Float)

@JvmInline
value class Matrix4X4(val floatArrayValue : FloatArray) {
    constructor() : this(FloatArray(16))
    operator fun get(column : Int) = Vector.build(floatArrayValue, column * 4)
    operator fun set(column : Int, v : Vector){
        val i = column * 4
        floatArrayValue[i] =     v.x
        floatArrayValue[i + 1] = v.y
        floatArrayValue[i + 2] = v.z
    }
    override fun toString(): String {
        val sb = StringBuilder("(in memory column-major) ")
        for(i in floatArrayValue.indices) sb.append(" ${myIndex(i)}")
        for(r in 0..3) {
            sb.append("\n\t")
            for(c in 0..3) sb.append(" ${myIndex(r + c * 4)}")
        }
        return sb.toString()
    }
    private fun myIndex(i : Int) = String.format("%.3f",floatArrayValue[i])
}

sealed interface Vector {
    companion object {
        fun build(x : Float, y : Float, z : Float) : Vector = V(x, y, z)
        fun build(arr : FloatArray, i : Int) : Vector = V(arr[i], arr[i + 1], arr[i + 2])
        private data class V(override val x : Float, override val y : Float, override val z : Float) :
            Vector
    }
    val x : Float
    val y : Float
    val z : Float
    operator fun plus(v : Vector) : Vector = V(x + v.x, y + v.y, z + v.z)
    operator fun times(f : Float) : Vector = V(f * x, f * y, f * z)
    fun cross(v : Vector) : Vector = V(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
    fun dot(v : Vector) = x * v.x + y * v.y + z * v.z
    fun length() = kotlin.math.sqrt(this.dot(this))
}

data class UnitVector(override var x : Float, override var y : Float, override var z : Float) :
    Vector {
    init {
        val len = length()
        require(len != 0f)
        x /= len
        y /= len
        z /= len
    }
    companion object {
        fun normalize(v : Vector) = UnitVector(v.x, v.y, v.z)
    }
}