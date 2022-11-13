package asterhaven.vega.arboretum.utility.shapes

import android.opengl.Matrix
import asterhaven.vega.arboretum.utility.Matrix4X4
import asterhaven.vega.arboretum.utility.Vector
import kotlin.math.PI
import kotlin.math.atan2

class CommonShape(val start: Vector,
                  private val dir: Vector,
                  val len: Float,
                  private val radius: Float
                  ) {
    companion object {
        val canonicalOrientation = Vector.build(0f,0f,1f)
    }

    fun setTransformFromUnitShape(m : Matrix4X4){
        Matrix.setIdentityM(m.floatArrayValue, 0)
        Matrix.translateM(m.floatArrayValue, 0, start.x, start.y, start.z)
        rot(m)
        Matrix.scaleM(m.floatArrayValue, 0, radius, radius, len)
        // Translate, rotate, scale, In That Order!
    }
    private fun rot(m : Matrix4X4){
        val c = canonicalOrientation.cross(dir)
        val len = c.length()
        if(len != 0f){
            val angleRad = atan2(len, canonicalOrientation.dot(dir))
            val angleDeg = (180.0 / PI).toFloat() * angleRad
            Matrix.rotateM(m.floatArrayValue,0, angleDeg, c.x, c.y, c.z)
        }
        else if (canonicalOrientation.dot(dir) < 0) Matrix.scaleM(m.floatArrayValue, 0, -1f, -1f, -1f)
    }
}