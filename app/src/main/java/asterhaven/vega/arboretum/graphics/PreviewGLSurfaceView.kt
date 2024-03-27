package asterhaven.vega.arboretum.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.BuildConfig
import asterhaven.vega.arboretum.lsystems.TreeLSystem

class PreviewGLSurfaceView(c : Context) : GLSurfaceView(c) {
    private val renderer = PreviewRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }
    private lateinit var lSystem: TreeLSystem
    private var stepsWish = -1 //desired number of iterations
    private var stepsActual = -1
    private val previewAnimation = ArbAnimation(this){
        //if we were computing but the desired number of steps lowered due to input, stop there
        if(stepsActual >= stepsWish) false
        else {
            renderer.tree.grow()
            renderer.tree.measure()
            requestRender()
            ++stepsActual < stepsWish
        }
    }

    fun showPreview(lSystem : TreeLSystem, s : Int) {
        synchronized(this){
            if(!::lSystem.isInitialized || this.lSystem !== lSystem){
                this.lSystem = lSystem
                this.stepsWish = s
                previewAnimation.stopThen(::launchTreeFromScratch)
            }
            else if(stepsWish != s) {
                this.stepsWish = s
                when (stepsWish - stepsActual){
                    in 1..Int.MAX_VALUE -> previewAnimation.recurSteps()
                    0 -> {
                        previewAnimation.stopThen {  }
                        /* if we're computing one more iteration, we currently have to see it through
                         * and it's good behavior to keep it as it may have been a long calculation,
                         * but todo accurate steps count on param slider?
                         */
                    }
                    in Int.MIN_VALUE..-1 -> previewAnimation.stopThen(::launchTreeFromScratch)
                }
                if(BuildConfig.DEBUG) println("a recomposition of preview without changing parameters")
            }
        }
    }
    private fun launchTreeFromScratch() {
        queueEvent { //make changes on the rendering thread
            renderer.newTreeForNewSystem(lSystem)
            renderer.tree.measure()
            stepsActual = 0
            requestRender()
            previewAnimation.recurSteps()
        }
    }
}