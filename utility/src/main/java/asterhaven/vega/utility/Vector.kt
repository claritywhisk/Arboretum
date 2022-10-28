package asterhaven.vega.utility

import com.hackoeur.jglm.Vec3

const val COORDS_PER_VERTEX = 3

@JvmInline
value class AngleDeg(val floatValue : Float){
    fun asRadians() = AngleRad((kotlin.math.PI * floatValue / 180f).toFloat())
}
@JvmInline
value class AngleRad(val floatValue : Float)

/*sealed interface Vector {
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

data class UnitVector(override var x : Float, override var y : Float, override var z : Float) : Vector {
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
}*/