package asterhaven.vega.arboretum.graphics.draw

import android.opengl.GLES20
import asterhaven.vega.arboretum.ui.theme.TrunkBrown
import asterhaven.vega.arboretum.utility.COORDS_PER_VERTEX
import asterhaven.vega.arboretum.utility.Matrix4X4
import asterhaven.vega.arboretum.utility.shapes.CommonShape
import asterhaven.vega.arboretum.utility.shapes.UnitCylinder

object TreeProgram : Drawing.ProgramLoader() {
    override val vertexShaderCode by lazy {
                "uniform mat4 uMVPMatrix;" +
                "uniform mat4 uBranchTransform;" +
                "attribute vec4 vUnitCylinder;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * uBranchTransform * vUnitCylinder;" +
                "}"
    }

    override val fragmentShaderCode by lazy {
                "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"
    }
    private val branchTransformation by lazy { Matrix4X4() }
    fun draw(branches : ArrayList<CommonShape>, mvpMatrix : Matrix4X4){
        val cylinder = UnitCylinder.get(8) //todo choose number of sides
        GLES20.glUseProgram(programId)
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(programId, "vColor"), 1, TrunkBrown, 0)
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programId, "uMVPMatrix"), 1, false, mvpMatrix.floatArrayValue, 0)
        GLES20.glGetAttribLocation(programId, "vUnitCylinder").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                cylinder.vertices
            )
            for(br in branches) {
                br.setTransformFromUnitShape(branchTransformation)
                GLES20.glUniform4fv(
                    GLES20.glGetUniformLocation(programId, "vColor"), 1, TrunkBrown, 0
                )
                GLES20.glUniformMatrix4fv(
                    GLES20.glGetUniformLocation(programId, "uMVPMatrix"),
                    1, false,
                    mvpMatrix.floatArrayValue, 0
                )
                GLES20.glUniformMatrix4fv(
                    GLES20.glGetUniformLocation(programId, "uBranchTransform"),
                    1, false,
                    branchTransformation.floatArrayValue, 0
                )
                GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    cylinder.lenDrawOrder,
                    GLES20.GL_UNSIGNED_SHORT,
                    cylinder.drawOrder
                )
            }
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}