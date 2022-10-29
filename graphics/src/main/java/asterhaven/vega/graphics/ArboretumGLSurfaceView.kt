package asterhaven.vega.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.graphics.draw.Drawing

class ArboretumGLSurfaceView private constructor(c : Context) : GLSurfaceView(c) {
    private val renderer : ArboretumRenderer = ArboretumRenderer().also { setRenderer(it) }
    companion object {
        fun manufacture(c : Context) = ArboretumGLSurfaceView(c).apply {
            setEGLContextClientVersion(2)
            renderMode = RENDERMODE_WHEN_DIRTY
        }
    }
    //make changes on the rendering thread
    fun updateState(t: Iterable<Drawing>?) = queueEvent { renderer.state = t }
}