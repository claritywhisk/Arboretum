package asterhaven.vega.arboretum.utility.shapes

import asterhaven.vega.arboretum.utility.Ray
import asterhaven.vega.arboretum.utility.UnitVector
import asterhaven.vega.arboretum.utility.Vector

object Icosahedron {
    private const val X = 0.5257311f
    private const val Z = 0.8506508f

    val vdata by lazy { floatArrayOf(
        -X,   0f, Z  ,  X,   0f, Z  ,  -X,   0f, -Z  ,  X,   0f, -Z  ,
        0f, Z, X  ,    0f, Z, -X  ,    0f, -Z, X  ,    0f, -Z, -X  ,
        Z, X,   0f  ,  -Z, X,   0f  ,  Z, -X,   0f  ,  -Z, -X,   0f
    ) }

    val vDrawOrder by lazy { shortArrayOf(
        0,4,1  ,  0,9,4  ,  9,5,4  ,  4,5,8  ,  4,8,1  ,
        8,10,1  ,  8,3,10  ,  5,3,8  ,  5,2,3  ,  2,7,3  ,
        7,10,3  ,  7,6,10  ,  7,11,6  ,  11,0,6  ,  0,1,6  ,
        6,1,10  ,  9,0,11  ,  9,11,2  ,  9,2,5  ,  7,2,11
    ) }

    val stems by lazy { Array(20) { rayI ->
        val v1i = vDrawOrder[3 * rayI] * 3
        val v2i = vDrawOrder[3 * rayI + 1] * 3
        val v3i = vDrawOrder[3 * rayI + 2] * 3
        Ray(xyz = Vector.build(//midpoint of triangular face
            (vdata[v1i] + vdata[v2i] + vdata[v3i])/3f,
            (vdata[v1i + 1] + vdata[v2i + 1] + vdata[v3i + 1])/3f,
            (vdata[v1i + 2] + vdata[v2i + 2] + vdata[v3i + 2])/3f
            ),
            dir = UnitVector.normalize(//normal vector by crossing two edges
                Vector.build(
                    vdata[v2i] - vdata[v1i],
                    vdata[v2i + 1] - vdata[v1i + 1],
                    vdata[v2i + 2] - vdata[v1i + 2]
                ).cross(Vector.build(
                    vdata[v2i] - vdata[v3i],
                    vdata[v2i + 1] - vdata[v3i + 1],
                    vdata[v2i + 2] - vdata[v3i + 2]
                ))
            )
        )
    }}
}