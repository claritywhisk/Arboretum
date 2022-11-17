package asterhaven.vega.arboretum.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import kotlinx.coroutines.*

class PreviewGLSurfaceView(c : Context) : GLSurfaceView(c) {
    private val renderer = PreviewRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private var job : Job? = null
    fun beginPreview(lSystem : TreeLSystem) {
        queueEvent { //make changes on the rendering thread
            renderer.newTreeForNewSystem(lSystem)
            renderer.tree.measure()
            requestRender()
            CoroutineScope(Dispatchers.Default).launch {
                job?.cancelAndJoin()
                job = CoroutineScope(Dispatchers.Default).launch {
                    repeat(7) {
                        delay(125)
                        queueEvent {
                            renderer.tree.grow()
                            renderer.tree.measure()
                            requestRender()
                        }
                    }
                }
            }
        }
    }
}