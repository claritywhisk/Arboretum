package asterhaven.vega.arboretum.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.BuildConfig
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.utility.DELAY_PER_STEP_MS
import kotlinx.coroutines.*

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
    private lateinit var stepJob : Job

    fun showPreview(lSystem : TreeLSystem, s : Int) {
        synchronized(this){
            if(!::lSystem.isInitialized || this.lSystem !== lSystem){
                this.lSystem = lSystem
                this.stepsWish = s
                stopAnimationThen(::launchTreeFromScratch)
            }
            else if(stepsWish != s) {
                this.stepsWish = s
                when (stepsWish - stepsActual){
                    in 1..Int.MAX_VALUE -> recurSteps()
                    0 -> {
                        stopAnimationThen {  }
                        /* if we're computing one more iteration, we currently have to see it through
                         * and it's good behavior to keep it as it may have been a long calculation,
                         * but todo accurate steps count on param slider?
                         */
                    }
                    in Int.MIN_VALUE..-1 -> stopAnimationThen(::launchTreeFromScratch)
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
            recurSteps()
        }
    }
    private fun queueStep(finished : () -> Unit) {
        queueEvent {
            renderer.tree.grow()
            renderer.tree.measure()
            stepsActual++
            requestRender()
            finished()
        }
    }
    private fun recurSteps() {
        synchronized(this) {
            //limit to one chain of calls
            if(!::stepJob.isInitialized || stepJob.isCompleted) {
                stepJob = CoroutineScope(Dispatchers.Default).launch {
                    delay(DELAY_PER_STEP_MS) //todo smooth
                    //if we were computing but the desired number of steps lowered due to input, stop there
                    if (stepsActual < stepsWish) queueStep(finished = ::recurSteps)
                    //this should complete immediately before the renderer does the work
                }
            }
        }
    }
    private fun stopAnimationThen(doNext : () -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            //this only serves to cancel the animation delay
            //the renderer could have a step queued
            if(::stepJob.isInitialized) stepJob.cancelAndJoin()
            doNext()
        }
    }
}