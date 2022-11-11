package asterhaven.vega.arboretum.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class PreviewGLSurfaceView(c : Context) : GLSurfaceView(c) {
    private val renderer = PreviewRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    //make changes on the rendering thread
    fun updateState(t : TreeLSystem) = queueEvent {
        renderer.newSystem(t)
        CoroutineScope(Dispatchers.Default).launch {
            repeat(8) {
                delay(125 - measureTimeMillis {
                    renderer.tree.grow() //TODO check memory in or before this
                })
                requestRender()
            }
        }
    }
}