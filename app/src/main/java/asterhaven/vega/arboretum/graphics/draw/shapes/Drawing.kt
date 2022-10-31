package asterhaven.vega.arboretum.graphics.draw.shapes

import android.opengl.GLES20
import asterhaven.vega.arboretum.graphics.ArboretumRenderer
import asterhaven.vega.arboretum.utility.Matrix4X4

sealed class Drawing {
    abstract fun draw(mvpMatrix : Matrix4X4)
    abstract class ProgramLoader {
        protected var programId : Int = -9
        abstract val vertexShaderCode : String
        abstract val fragmentShaderCode : String
        // this should be run on the renderer thread including when graphics context lost due to config change
        fun load(){
            val vertexShader: Int = ArboretumRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader: Int = ArboretumRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
            programId = GLES20.glCreateProgram().also {
                GLES20.glAttachShader(it, vertexShader)
                GLES20.glAttachShader(it, fragmentShader)
                GLES20.glLinkProgram(it)
                //GLES20.glDeleteShader(vertexShader)
                //GLES20.glDeleteShader(fragmentShader)
            }
        }
    }
}