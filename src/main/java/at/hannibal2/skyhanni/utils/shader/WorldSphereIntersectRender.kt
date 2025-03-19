package at.hannibal2.skyhanni.utils.shader

import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule

@SkyHanniModule
object WorldSphereIntersectRender {

    fun start() {
        ShaderManager.enableShader(ShaderManager.Shaders.WORLD_SPHERE_INTERSECT)
    }

    fun end() {
        ShaderManager.disableShader()
    }
}
