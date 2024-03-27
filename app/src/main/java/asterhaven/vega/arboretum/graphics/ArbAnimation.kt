package asterhaven.vega.arboretum.graphics

import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.BuildConfig
import asterhaven.vega.arboretum.utility.DELAY_PER_STEP_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArbAnimation(
    private val surfaceView : GLSurfaceView,
    private val stepIsNotLast : () -> Boolean){
    private lateinit var stepJob : Job
    fun recurSteps(){
        synchronized(this){
            if(!::stepJob.isInitialized || stepJob.isCompleted) {
                stepJob = CoroutineScope(Dispatchers.Default).launch {
                    delay(DELAY_PER_STEP_MS) //todo smooth
                    surfaceView.queueEvent {
                        if(stepIsNotLast()) recurSteps()
                        else if(BuildConfig.DEBUG) println("Finished for $surfaceView")
                    }
                }
            }
        }
    }
    fun stopThen(doNext : () -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            //this only serves to cancel the animation delay
            //the renderer could have a step queued
            if(::stepJob.isInitialized) stepJob.cancelAndJoin()
            doNext()
        }
    }
}

