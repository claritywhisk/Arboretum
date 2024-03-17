package asterhaven.vega.arboretum.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.utility.SingleRestartableJob
import kotlinx.coroutines.*

class PreviewGLSurfaceView(c : Context) : GLSurfaceView(c) {
    private val renderer = PreviewRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private var job = SingleRestartableJob()
    fun launchPreview(lSystem : TreeLSystem) {
        job.resetThenLaunch {
            queueEvent { //make changes on the rendering thread
                renderer.newTreeForNewSystem(lSystem)
                renderer.tree.measure()
                requestRender()
            }
            repeat(6) {
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