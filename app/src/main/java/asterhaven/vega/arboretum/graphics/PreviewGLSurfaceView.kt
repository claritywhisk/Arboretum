package asterhaven.vega.arboretum.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class PreviewGLSurfaceView(c : Context) : GLSurfaceView(c) {
    private val renderer = PreviewRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private var previewJob : Job? = null
    fun updateState(lSystem : TreeLSystem) {
        val last = previewJob
        queueEvent { //make changes on the rendering thread
            previewJob = CoroutineScope(Dispatchers.Default).launch {
                last?.cancelAndJoin()
                renderer.newTreeForNewSystem(lSystem)
                repeat(5) {
                    ensureActive()
                    val ms = measureTimeMillis {
                        renderer.tree.grow() //TODO check memory in or before this
                        renderer.tree.measure()
                    }
                    ensureActive()
                    if(it != 0) delay(125 - ms)
                    ensureActive()
                    requestRender()
                }
            }
        }
    }
}