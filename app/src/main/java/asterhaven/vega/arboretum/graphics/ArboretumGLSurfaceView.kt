package asterhaven.vega.arboretum.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.Tree


class ArboretumGLSurfaceView(c : Context) : GLSurfaceView(c) {
    private val renderer = ArboretumRenderer()

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    private val grows = arrayListOf<Tree>()
    private val worldAnimation = ArbAnimation(this) {
        grows.removeLast().grow()
        requestRender()
        grows.isNotEmpty()
    }

    //make changes on the rendering thread
    fun updateState(t: Iterable<Drawing>) = queueEvent {
        renderer.state = t
        requestRender()
        grows.clear()
        for (d in t) if (d is Tree) repeat(d.desiredAge - d.mathematicalAge) {
            grows.add(d)
        }
        if(grows.isNotEmpty()) {
            grows.shuffle()
            worldAnimation.recurSteps()
        }
    }
}
/* todo likely memory management to be done at some point - seems to be plenty though
            val stopUseAtPercent = 10
            val threshold = availMemory() * ((100 - stopUseAtPercent) / 100.0)
            for(gt in grows){
                delay(500)
                if(availMemory() > threshold) {
                    queueEvent {
                        gt.grow()
                        requestRender()
                    }
                }
                else Handler(Looper.getMainLooper()).post {
                    val msg = "Stopped out of abundance of caution over memory"
                    if(BuildConfig.DEBUG) println(msg)
                    Toast.makeText(
                        context,
                        msg,
                        Toast.LENGTH_LONG
                    ).show()
                }

    fun availMemory() : Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
}  */