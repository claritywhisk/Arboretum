package asterhaven.vega.arboretum.graphics

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import asterhaven.vega.arboretum.graphics.draw.TreeProgram
import asterhaven.vega.arboretum.lsystems.Tree
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.ui.theme.PurpleGrey40
import asterhaven.vega.arboretum.utility.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private const val framingSpace = 1.05f // >= 1
private const val largeDistance = 1000f

class PreviewRenderer : GLSurfaceView.Renderer {
    lateinit var tree : Tree

    fun newTreeForNewSystem(sys : TreeLSystem){
        tree = Tree(Vector.build(0f,0f,0f), UnitVector(0f,0f,1f), sys)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        val background = PurpleGrey40
        GLES20.glClearColor(background.red, background.green, background.blue, background.alpha)

        GLES20.glClearColor(.3f, 0.6f, 0.4f, 1.0f) //todo
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // Load program upon new graphics context
        TreeProgram.load()
    }

    var w = 0
    var h = 0
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        w = width
        h = height
    }

    private val projectionMatrix = Matrix4X4()
    private val viewMatrix = Matrix4X4()
    private val vPMatrix = Matrix4X4()
    override fun onDrawFrame(p0: GL10?) {
        if(!::tree.isInitialized) return
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) //redraw background
        val m = tree.measurements
        val midX = (m.x.max + m.x.min)/2f
        val midY = (m.y.max + m.y.min)/2f
        val midZ = (m.z.max + m.z.min)/2f
        println(m)
        println("$midX $midY $midZ")
        //left third: looking back in the x direction
        GLES20.glViewport(0, 0, h, h)
        Matrix.setLookAtM(viewMatrix.floatArrayValue, 0,
            largeDistance, 0f, 0f,
            0f, 0f, midZ,
            0f, 0f, 1.0f
        )
        Matrix.frustumM(projectionMatrix.floatArrayValue, 0,
            -1f, 1f,
            -1f, 1f,
            largeDistance - m.x.max * framingSpace,
            largeDistance - m.x.min * framingSpace
            //(m.x.max * framingSpace - m.x.max)/2f, (m.x.max - m.x.min) * framingSpace
        )
        Matrix.multiplyMM(vPMatrix.floatArrayValue, 0,
            projectionMatrix.floatArrayValue, 0,
            viewMatrix.floatArrayValue, 0
        )
        tree.draw(vPMatrix)
        //middle third: looking back in the y direction
        GLES20.glViewport(h, 0, h, h)
        Matrix.setLookAtM(viewMatrix.floatArrayValue, 0,
            0f, largeDistance, 0f,
            0f, 0f, midZ,
            0f, 0f, 1.0f
        )
        Matrix.frustumM(projectionMatrix.floatArrayValue, 0,
            -1f, 1f,
            -1f, 1f,
            largeDistance - m.y.max * framingSpace,
            largeDistance - m.y.min * framingSpace
        )
        Matrix.multiplyMM(vPMatrix.floatArrayValue, 0,
            projectionMatrix.floatArrayValue, 0,
            viewMatrix.floatArrayValue, 0
        )
        tree.draw(vPMatrix)
        //right third: looking down in the z direction
        GLES20.glViewport(h + h, 0, h, h)
        Matrix.setLookAtM(viewMatrix.floatArrayValue, 0,
            0f, 0f, largeDistance,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
        Matrix.frustumM(projectionMatrix.floatArrayValue, 0,
            -1f, 1f,
            -1f, 1f,
            largeDistance - m.z.max * framingSpace,
            largeDistance - m.z.min * framingSpace
        )
        Matrix.multiplyMM(vPMatrix.floatArrayValue, 0,
            projectionMatrix.floatArrayValue, 0,
            viewMatrix.floatArrayValue, 0
        )
        tree.draw(vPMatrix)
    }
}