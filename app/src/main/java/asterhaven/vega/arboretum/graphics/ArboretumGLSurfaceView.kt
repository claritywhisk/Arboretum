package asterhaven.vega.arboretum.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.graphics.draw.Drawing

class ArboretumGLSurfaceView(c : Context) : GLSurfaceView(c) {
    private val renderer = ArboretumRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    //make changes on the rendering thread
    fun updateState(t: Iterable<Drawing>) = queueEvent { renderer.state = t }
}