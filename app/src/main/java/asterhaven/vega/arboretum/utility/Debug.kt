package asterhaven.vega.arboretum.utility

import android.opengl.GLES20
import kotlin.system.exitProcess

var gldbcounter = 0
fun endDebug(){ gldbcounter = 0 }
fun glDebug() = glDebug(null)
fun glDebug(msg : String?){
    msg?.let { println(it) }
    gldbcounter++
    val e = GLES20.glGetError()
    if(e != GLES20.GL_NO_ERROR){
        println("OPENGL ERROR ${Integer.toHexString(e)} i.e. $e on call $gldbcounter")
        exitProcess(-555)
        //Thread.sleep(1000)
    }
    //else println("passes debug call $gldbcounter")
}