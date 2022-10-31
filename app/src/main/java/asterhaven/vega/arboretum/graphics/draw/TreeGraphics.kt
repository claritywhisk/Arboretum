package asterhaven.vega.arboretum.graphics.draw

import android.opengl.GLES20
import asterhaven.vega.arboretum.graphics.ArboretumRenderer
import asterhaven.vega.arboretum.utility.COORDS_PER_VERTEX
import asterhaven.vega.arboretum.utility.Matrix4X4
import asterhaven.vega.arboretum.utility.shapes.CommonShape
import asterhaven.vega.arboretum.utility.shapes.UnitCylinder

private val brown = floatArrayOf(83/255f,53/255f,10/255f, 1f) //todo

object TreeGraphics {
    private val mProgram : Int
    init {
        val branchShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "uniform mat4 uBranchTransform;" +
                    "attribute vec4 vUnitCylinder;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * uBranchTransform * vUnitCylinder;" +
                    "}"

        val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}"
        val branchShader: Int = ArboretumRenderer.loadShader(GLES20.GL_VERTEX_SHADER, branchShaderCode)
        val fragmentShader: Int = ArboretumRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, branchShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
            GLES20.glDeleteShader(branchShader)
            GLES20.glDeleteShader(fragmentShader)
        }
    }
    private val branchTransformation = Matrix4X4()
    fun draw(branches : ArrayList<CommonShape>, mvpMatrix : Matrix4X4){
        val cylinder = UnitCylinder.get(8) //todo choose number of sides
        GLES20.glUseProgram(mProgram)
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(mProgram, "vColor"), 1, brown, 0)
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgram, "uMVPMatrix"), 1, false, mvpMatrix.floatArrayValue, 0)
        GLES20.glGetAttribLocation(mProgram, "vUnitCylinder").also {
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
                    GLES20.glGetUniformLocation(mProgram, "vColor"), 1, brown, 0
                )
                GLES20.glUniformMatrix4fv(
                    GLES20.glGetUniformLocation(mProgram, "uMVPMatrix"),
                    1, false,
                    mvpMatrix.floatArrayValue, 0
                )
                GLES20.glUniformMatrix4fv(
                    GLES20.glGetUniformLocation(mProgram, "uBranchTransform"),
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