package asterhaven.vega.arboretum.utility.shapes

import java.nio.FloatBuffer
import java.nio.ShortBuffer

data class DrawBuffers(val vertices : FloatBuffer, val drawOrder : ShortBuffer, val lenDrawOrder : Int)