package asterhaven.vega.arboretum.graphics

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.graphics.draw.TreeProgram
import asterhaven.vega.arboretum.lsystems.Tree
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.utility.UnitVector
import asterhaven.vega.arboretum.utility.Vector
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PreviewRenderer : GLSurfaceView.Renderer {
    lateinit var tree : Tree

    fun newSystem(sys : TreeLSystem){
        tree = Tree(Vector.build(0f,0f,0f), UnitVector(0f,0f,1f), sys)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(.3f, 0.6f, 0.4f, 1.0f) //todo
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // Load program upon new graphics context
        TreeProgram.load()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {

    }

}