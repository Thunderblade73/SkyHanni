package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.shader.Shader
import at.hannibal2.skyhanni.utils.shader.Uniform
import java.awt.Color

object WorldSphereIntersectShader : Shader("world_sphere_intersect", "world_sphere_intersect") {

    val INSTANCE: WorldSphereIntersectShader
        get() = this

    var radius: Float = 0f
    var centerPos: FloatArray = floatArrayOf(0f, 0f, 0f)
        private set
    var color: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)
        private set

    fun setCenterPos(pos: LorenzVec) {
        centerPos = floatArrayOf(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
    }

    fun setColor(c: Color) {
        color = floatArrayOf(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f)
    }

    override fun registerUniforms() {
        registerUniform(Uniform.UniformType.FLOAT, "radius") { radius }
        registerUniform(Uniform.UniformType.VEC3, "centerPos") { centerPos }
        //registerUniform(Uniform.UniformType.VEC4, "color") { color }
    }
}

