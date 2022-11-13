package asterhaven.vega.arboretum.graphics

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import asterhaven.vega.arboretum.graphics.draw.TreeProgram
import asterhaven.vega.arboretum.lsystems.Tree
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.utility.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates

const val framingSpace = 1.1f // >= 1

class PreviewRenderer : GLSurfaceView.Renderer {
    lateinit var tree : Tree

    fun newTreeForNewSystem(sys : TreeLSystem){
        tree = Tree(Vector.build(0f,0f,0f), UnitVector(0f,0f,1f), sys)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(.3f, 0.6f, 0.4f, 1.0f) //todo
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        glDebug("onSurfaceCreated")
        // Load program upon new graphics context
        TreeProgram.load()
        glDebug()
        endDebug()
    }

    private var ratio by Delegates.notNull<Float>()
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        ratio = width.toFloat() / height.toFloat()
        println("dims $width $height")
    }

    private val projectionMatrix = Matrix4X4()
    private val viewMatrix = Matrix4X4()
    private val vPMatrix = Matrix4X4()
    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) //redraw background
        if(!::tree.isInitialized) return
        val m = tree.measurements
        val midX = (m.x.max + m.x.min)/2f
        val midY = (m.y.max + m.y.min)/2f
        val midZ = (m.z.max + m.z.min)/2f
        println(m)
        println("$midX $midY $midZ")
        //left third: looking back in the x direction
        Matrix.frustumM(projectionMatrix.floatArrayValue, 0,
            -ratio, -ratio/3f,
            -1f, 1f,
            (m.x.max * framingSpace - m.x.max)/2f, (m.x.max - m.x.min) * framingSpace
        )
        Matrix.setLookAtM(viewMatrix.floatArrayValue, 0,
            m.x.max * framingSpace, midY, midZ,
            midX, midY, midZ,
            0f, 0f, 1.0f
        )
        Matrix.multiplyMM(vPMatrix.floatArrayValue, 0,
            projectionMatrix.floatArrayValue, 0,
            viewMatrix.floatArrayValue, 0
        )
        tree.draw(vPMatrix)

        //center third: looking down in the z direction
        Matrix.frustumM(projectionMatrix.floatArrayValue, 0,
            -ratio/3f, ratio/3f,
            -1f, 1f,
            (m.z.max * framingSpace - m.z.max)/2f, (m.z.max - m.z.min) * framingSpace
        )
        Matrix.setLookAtM(viewMatrix.floatArrayValue, 0,
            midX, midY, m.z.max * framingSpace,
            midX, midY, midZ,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vPMatrix.floatArrayValue, 0,
            projectionMatrix.floatArrayValue, 0,
            viewMatrix.floatArrayValue, 0
        )
        tree.draw(vPMatrix)
    }

}