package asterhaven.vega.arboretum.graphics.draw

import android.opengl.GLES20
import asterhaven.vega.arboretum.ui.theme.SeaBlue
import asterhaven.vega.arboretum.utility.COORDS_PER_VERTEX
import asterhaven.vega.arboretum.utility.Matrix4X4
import asterhaven.vega.arboretum.utility.shapes.Icosahedron
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Globe : Drawing() {
    private val vertexBuffer by lazy {
        ByteBuffer.allocateDirect(Icosahedron.vdata.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(Icosahedron.vdata)
                position(0)
            }
        }
    }
    private val drawListBuffer by lazy {
        ByteBuffer.allocateDirect(Icosahedron.vDrawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(Icosahedron.vDrawOrder)
                position(0)
            }
        }
    }
    override fun draw(mvpMatrix: Matrix4X4) = draw(mvpMatrix, vertexBuffer, drawListBuffer)
    companion object : ProgramLoader() {
        override val vertexShaderCode by lazy {
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}"
        }
        override val fragmentShaderCode by lazy {
                    "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}"
        }

        fun draw(mvpMatrix : Matrix4X4, vertexBuffer : FloatBuffer, drawListBuffer: ShortBuffer){
            // Add program to OpenGL ES environment
            GLES20.glUseProgram(programId)

            // get handle to vertex shader's vPosition member
            GLES20.glGetAttribLocation(programId, "vPosition").also {
                // Enable a handle to the triangle vertices
                GLES20.glEnableVertexAttribArray(it)

                // Prepare the triangle coordinate data
                GLES20.glVertexAttribPointer(
                    it,
                    COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT,
                    false,
                    0,//vertexStride,
                    vertexBuffer
                )

                // get handle to fragment shader's vColor member and set color
                val mColorHandle = GLES20.glGetUniformLocation(programId, "vColor")
                GLES20.glUniform4fv(mColorHandle, 1, SeaBlue, 0)

                // get handle to shape's transformation matrix
                val vPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")

                // Pass the projection and view transformation to the shader
                GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix.floatArrayValue, 0)

                // Draw triangles from vertices in order
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, Icosahedron.vDrawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer )

                // Disable vertex array
                GLES20.glDisableVertexAttribArray(it)
            }
        }
    }
}
