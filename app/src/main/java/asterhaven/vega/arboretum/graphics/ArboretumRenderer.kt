package asterhaven.vega.arboretum.graphics

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.Globe
import asterhaven.vega.arboretum.graphics.draw.TreeProgram
import asterhaven.vega.arboretum.utility.Matrix4X4

class ArboretumRenderer : GLSurfaceView.Renderer {
    var state : Iterable<Drawing>? = null

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)
    private val combined = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(.5f, 0.5f, 0.5f, 1.0f) //todo

        // Avoid seeing the other side of the world etc.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Load programs upon new graphics context
        programs.forEach { it.load() }
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Create a rotation transformation for the globe
        val time = SystemClock.uptimeMillis()
        val angle = 0.060f * time.toInt()
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, .05f, .01f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(combined, 0, vPMatrix, 0, rotationMatrix, 0)

        state?.forEach { it.draw(Matrix4X4(combined)) }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, .8f, 100f)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    }

    companion object {
        val programs = arrayOf(Globe.Companion, TreeProgram)

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        fun loadShader(type: Int, shaderCode: String): Int =
            GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
    }
}