package asterhaven.vega.utility

/*@JvmInline
value class Matrix4X4(val floatArrayValue : FloatArray) {
    constructor() : this(FloatArray(16))
    operator fun get(column : Int) = Vector.build(floatArrayValue, column * 4)
    operator fun set(column : Int, v : Vector){
        val i = column * 4
        floatArrayValue[i] =     v.x
        floatArrayValue[i + 1] = v.y
        floatArrayValue[i + 2] = v.z
    }
    fun rotate(angleDeg: Float, axis: Vector){

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
}*/