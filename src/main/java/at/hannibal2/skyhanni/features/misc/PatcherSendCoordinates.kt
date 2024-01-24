package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import at.hannibal2.skyhanni.utils.RenderUtils.getViewerPos
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager



import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PatcherSendCoordinates {

    private val patcherBeacon = mutableListOf<PatcherBeacon>()
    private val logger = LorenzLogger("misc/patchercoords")

    // TODO USE SH-REPO
    private val pattern = "(?<playerName>.*): [xX]: (?<x>[0-9.-]+),? [yY]: (?<y>[0-9.-]+),? [zZ]: (?<z>.*)".toPattern()

    @SubscribeEvent
    fun onPatcherCoordinates(event: LorenzChatEvent) {
        if (!SkyHanniMod.feature.misc.patcherSendCoordWaypoint) return

        val message = event.message.removeColor()
        pattern.matchMatcher(message) {
            var description = group("playerName").split(" ").last()
            val x = group("x").toFloat()
            val y = group("y").toFloat()

            val end = group("z")
            val z = if (end.contains(" ")) {
                val split = end.split(" ")
                val extra = split.drop(1).joinToString(" ")
                description += " $extra"

                split.first().toFloat()
            } else end.toFloat()
            patcherBeacon.add(PatcherBeacon(LorenzVec(x, y, z), description, System.currentTimeMillis() / 1000))
            logger.log("got patcher coords and username")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!SkyHanniMod.feature.misc.patcherSendCoordWaypoint) return

        for (beacon in patcherBeacon) {
            val location = beacon.location

            GlStateManager.pushMatrix()
            //drawColor
            run {
                val color = LorenzColor.DARK_GREEN.toColor()
                val (viewerX, viewerY, viewerZ) = getViewerPos(event.partialTicks)
                val x = location.x - viewerX
                val y = location.y - viewerY
                val z = location.z - viewerZ
                val distSq = x * x + y * y + z * z
                val realAlpha = if (1f == -1f) {
                    (0.1f + 0.005f * distSq.toFloat()).coerceAtLeast(0.2f)
                } else {
                    1f
                }

                //drawFilledBoundingBox
                run {
                    val aabb = AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expandBlock()

                    //GlStateManager.disableLighting()

                    val tessellator = Tessellator.getInstance()
                    val worldRenderer = tessellator.worldRenderer
                    //vertical
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
                    tessellator.draw()
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
                    tessellator.draw()
                    //x
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
                    tessellator.draw()
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
                    tessellator.draw()

                    //z
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
                    tessellator.draw()
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
                    tessellator.draw()

                }

                // if (distSq > 5 * 5 && false) renderBeaconBeam(x, y + 1, z, color.rgb, 1.0f, event.partialTicks)
                //GlStateManager.disableLighting()

            }
            //drawWaypointFilled
            run {
                val color = LorenzColor.GREEN.toColor()
                val (viewerX, viewerY, viewerZ) = getViewerPos(event.partialTicks)
                val x = location.x - viewerX
                val y = location.y - viewerY
                val z = location.z - viewerZ
                val distSq = x * x + y * y + z * z
                if (true) {

                }
                //drawFilledBoundingBox
                run {
                    val aabb = AxisAlignedBB(
                        x - 0.0, y - 0.0, z - 0.0,
                        x + 1 + 0.0, y + 1 + 0.0, z + 1 + 0.0
                    ).expandBlock()
                    val alphaMultiplier = (0.1f + 0.005f * distSq.toFloat()).coerceAtLeast(0.2f)

                    //GlStateManager.disableLighting()

                    val tessellator = Tessellator.getInstance()
                    val worldRenderer = tessellator.worldRenderer

                    //vertical
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
                    tessellator.draw()
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
                    tessellator.draw()

                    //x
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
                    tessellator.draw()
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
                    tessellator.draw()

                    //z
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
                    tessellator.draw()
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
                    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
                    tessellator.draw()

                }

                if (distSq > 5 * 5 && true)
                    //renderBeaconBeam
                    run {
                        val y1 = y + 1
                        val height = 300
                        val bottomOffset = 0
                        val topOffset = bottomOffset + height
                        val tessellator = Tessellator.getInstance()
                        val worldrenderer = tessellator.worldRenderer
                        Minecraft.getMinecraft().textureManager.bindTexture(RenderUtils.beaconBeam)

                        //GlStateManager.disableLighting()

                        val time = Minecraft.getMinecraft().theWorld.totalWorldTime + event.partialTicks.toDouble()
                        val d1 = MathHelper.func_181162_h(
                            -time * 0.2 - MathHelper.floor_double(-time * 0.1)
                                .toDouble()
                        )
                        val r = (color.rgb shr 16 and 0xFF) / 255f
                        val g = (color.rgb shr 8 and 0xFF) / 255f
                        val b = (color.rgb and 0xFF) / 255f
                        val d2 = time * 0.025 * -1.5
                        val d4 = 0.5 + cos(d2 + 2.356194490192345) * 0.2
                        val d5 = 0.5 + sin(d2 + 2.356194490192345) * 0.2
                        val d6 = 0.5 + cos(d2 + Math.PI / 4.0) * 0.2
                        val d7 = 0.5 + sin(d2 + Math.PI / 4.0) * 0.2
                        val d8 = 0.5 + cos(d2 + 3.9269908169872414) * 0.2
                        val d9 = 0.5 + sin(d2 + 3.9269908169872414) * 0.2
                        val d10 = 0.5 + cos(d2 + 5.497787143782138) * 0.2
                        val d11 = 0.5 + sin(d2 + 5.497787143782138) * 0.2
                        val d14 = -1.0 + d1
                        val d15 = height.toDouble() * 2.5 + d14
                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
                        worldrenderer.pos(x + d4, y1 + topOffset, z + d5).tex(1.0, d15).color(r, g, b, 1.0f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + d4, y1 + bottomOffset, z + d5).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
                        worldrenderer.pos(x + d6, y1 + bottomOffset, z + d7).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
                        worldrenderer.pos(x + d6, y1 + topOffset, z + d7).tex(0.0, d15).color(r, g, b, 1.0f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + d10, y1 + topOffset, z + d11).tex(1.0, d15).color(r, g, b, 1.0f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + d10, y1 + bottomOffset, z + d11).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
                        worldrenderer.pos(x + d8, y1 + bottomOffset, z + d9).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
                        worldrenderer.pos(x + d8, y1 + topOffset, z + d9).tex(0.0, d15).color(r, g, b, 1.0f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + d6, y1 + topOffset, z + d7).tex(1.0, d15).color(r, g, b, 1.0f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + d6, y1 + bottomOffset, z + d7).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
                        worldrenderer.pos(x + d10, y1 + bottomOffset, z + d11).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
                        worldrenderer.pos(x + d10, y1 + topOffset, z + d11).tex(0.0, d15).color(r, g, b, 1.0f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + d8, y1 + topOffset, z + d9).tex(1.0, d15).color(r, g, b, 1.0f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + d8, y1 + bottomOffset, z + d9).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
                        worldrenderer.pos(x + d4, y1 + bottomOffset, z + d5).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
                        worldrenderer.pos(x + d4, y1 + topOffset, z + d5).tex(0.0, d15).color(r, g, b, 1.0f * 1.0f)
                            .endVertex()
                        tessellator.draw()
                        val d12 = -1.0 + d1
                        val d13 = height + d12
                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
                        worldrenderer.pos(x + 0.2, y1 + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + 0.2, y1 + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
                        worldrenderer.pos(x + 0.8, y1 + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
                        worldrenderer.pos(x + 0.8, y1 + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + 0.8, y1 + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + 0.8, y1 + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
                        worldrenderer.pos(x + 0.2, y1 + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
                        worldrenderer.pos(x + 0.2, y1 + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + 0.8, y1 + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + 0.8, y1 + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
                        worldrenderer.pos(x + 0.8, y1 + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
                        worldrenderer.pos(x + 0.8, y1 + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + 0.2, y1 + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * 1.0f)
                            .endVertex()
                        worldrenderer.pos(x + 0.2, y1 + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
                        worldrenderer.pos(x + 0.2, y1 + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
                        worldrenderer.pos(x + 0.2, y1 + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * 1.0f)
                            .endVertex()
                        tessellator.draw()
                    }
                //GlStateManager.disableLighting()
                Unit
            }
            //drawString
            run {
                val location1 = location.add(0.5, 0.5, 0.5)
                GlStateManager.pushMatrix()
                val viewer = Minecraft.getMinecraft().renderViewEntity
                val renderManager = Minecraft.getMinecraft().renderManager
                var x = location1.x - renderManager.viewerPosX
                var y = location1.y - renderManager.viewerPosY - viewer.eyeHeight
                var z = location1.z - renderManager.viewerPosZ
                val distSq = x * x + y * y + z * z
                val dist = sqrt(distSq)
                if (distSq > 144) {
                    x *= 12 / dist
                    y *= 12 / dist
                    z *= 12 / dist
                }

                GlStateManager.translate(x, y, z)
                GlStateManager.translate(0f, viewer.eyeHeight, 0f)
                //drawNametag
                run {
                    val fontRenderer = Minecraft.getMinecraft().fontRendererObj
                    val f1 = 0.02666667f
                    GlStateManager.pushMatrix()
                    GL11.glNormal3f(0.0f, 1.0f, 0.0f)
                    GlStateManager.rotate(-Minecraft.getMinecraft().renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                    GlStateManager.rotate(
                        Minecraft.getMinecraft().renderManager.playerViewX,
                        1.0f,
                        0.0f,
                        0.0f
                    )
                    GlStateManager.scale(-f1, -f1, f1)
                    //GlStateManager.disableLighting()

                    val tessellator = Tessellator.getInstance()
                    val worldrenderer = tessellator.worldRenderer
                    val i = 0
                    val j = fontRenderer.getStringWidth(beacon.name) / 2

                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
                    worldrenderer.pos((-j - 1).toDouble(), (-1 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
                    worldrenderer.pos((-j - 1).toDouble(), (8 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
                    worldrenderer.pos((j + 1).toDouble(), (8 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
                    worldrenderer.pos((j + 1).toDouble(), (-1 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
                    tessellator.draw()

                    val colorCode = LorenzColor.DARK_BLUE.toColor()?.rgb ?: 553648127
                    fontRenderer.drawString(beacon.name, -j, i, colorCode)

                    fontRenderer.drawString(beacon.name, -j, i, -1)
                    fontRenderer.drawString(" ",-j, i,LorenzColor.WHITE.toColor().rgb)

                    GlStateManager.popMatrix()
                }
                GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
                GlStateManager.translate(0f, -0.25f, 0f)
                GlStateManager.rotate(-renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
                GlStateManager.rotate(renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                GlStateManager.popMatrix()
                //GlStateManager.disableLighting()
            }
            GlStateManager.popMatrix()
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!event.isMod(10)) return

        val location = LocationUtils.playerLocation()
        // removed patcher beacon!
        patcherBeacon.removeIf { System.currentTimeMillis() / 1000 > it.time + 5 && location.distanceIgnoreY(it.location) < 5 }

        // removed patcher beacon after time!
        patcherBeacon.removeIf { System.currentTimeMillis() / 1000 > it.time + 60 }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        patcherBeacon.clear()
        logger.log("Reset everything (world change)")
    }

    data class PatcherBeacon(val location: LorenzVec, val name: String, val time: Long)
}
