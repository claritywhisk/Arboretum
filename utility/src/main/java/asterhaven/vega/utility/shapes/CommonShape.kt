package asterhaven.vega.utility.shapes

import com.hackoeur.jglm.Vec3

abstract class CommonShape(cp : ConstructorParams) {
    data class ConstructorParams(val start: Vec3, val dir: Vec3, val len: Float, val radius: Float)
    private val start: Vec3 = cp.start
    private val dir : Vec3 = cp.dir
    private val len : Float = cp.len
    private val radius: Float = cp.radius
    companion object {
        val canonicalOrientation = Vec3(0f,0f,1f)
    }
}
