package asterhaven.vega.arboretum.graphics

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.Tree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ArboretumGLSurfaceView(c : Context) : GLSurfaceView(c) {
    private val renderer = ArboretumRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    //make changes on the rendering thread
    fun updateState(t: Iterable<Drawing>) = queueEvent {
        renderer.state = t
        requestRender()
        val grows = arrayListOf<Tree>()
        for(d in t) if(d is Tree) repeat(d.desiredAge - d.mathematicalAge){
            grows.add(d)
        }
        CoroutineScope(Dispatchers.Default).launch {
            grows.shuffle()
            val stopUseAtPercent = 7
            val threshold = availMemory() * ((100 - stopUseAtPercent) / 100.0)
            for(gt in grows){
                delay(500)
                if(availMemory() > threshold) {//todo clearly more memory management to be done
                    queueEvent {
                        gt.grow()
                        requestRender()
                    }
                }
                else Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "Stopped out of abundance of caution over memory",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun availMemory() : Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
}