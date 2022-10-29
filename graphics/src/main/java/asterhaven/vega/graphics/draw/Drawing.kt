package asterhaven.vega.graphics.draw

import com.hackoeur.jglm.Mat4

abstract class Drawing {
    abstract fun draw(mvpMatrix : Mat4)
}