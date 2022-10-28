package asterhaven.vega.utility.shapes

import java.nio.FloatBuffer
import java.nio.ShortBuffer

data class Drawing(val vertices : FloatBuffer, val drawOrder : ShortBuffer, val lenDrawOrder : Int)