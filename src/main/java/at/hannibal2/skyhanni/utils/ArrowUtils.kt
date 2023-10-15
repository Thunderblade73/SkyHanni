package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.mobs.EntityKill
import at.hannibal2.skyhanni.events.hitTrigger
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import net.minecraft.client.Minecraft
import net.minecraft.entity.projectile.EntityArrow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.LinkedList
import java.util.Queue
import kotlin.math.PI
import kotlin.math.pow

object ArrowUtils {

    private val config get() = SkyHanniMod.feature.dev

    class SkyblockArrow(val base: EntityArrow, val pierce: Int, val canHitEnderman: Boolean) {
        var piercedAmount = 0

    }

    private data class SkyblockArrowSpawn(
        val origin: LorenzVec,
        val direction: LorenzVec,
        val pierce: Int,
        val canHitEnderman: Boolean,
        val spawnTick: Long = getCurrentTick()
    ) {
        fun parabola(time: Int) = parabola(origin, direction, time)
        fun isOnParabola(arrow: EntityArrow) = isOnParabola(origin, direction, getLivingTime(), arrow)

        fun getLivingTime() =
            (getCurrentTick() - spawnTick).toInt() // The value loss only makes a difference when an arrow didn't get cough for 59652 hours or more
    }

    private val upComingArrows: Queue<SkyblockArrowSpawn> = LinkedList()

    fun newArrow(origin: LorenzVec, facingDirection: LorenzVec, pierce: Int, canHitEnderman: Boolean) =
        newArrows(origin, facingDirection, 1, 0.0, pierce, canHitEnderman)

    fun newArrows(
        origin: LorenzVec,
        facingDirection: LorenzVec,
        amount: Int,
        spread: Double,
        pierce: Int,
        canHitEnderman: Boolean,
    ) {
        if (amount < 1) return
        upComingArrows.add(SkyblockArrowSpawn(origin, facingDirection, pierce, canHitEnderman))
        if (amount < 2) return
        if (amount % 2 == 0) throw NotImplementedError("Even Number of Arrows are not supported")
        val spreadInRad = Math.toRadians(spread)
        for (i in 1..amount / 2) {
            upComingArrows.add(
                SkyblockArrowSpawn(
                    origin,
                    facingDirection.rotateXZ(spreadInRad * i),
                    pierce,
                    canHitEnderman
                )
            )
            upComingArrows.add(
                SkyblockArrowSpawn(
                    origin,
                    facingDirection.rotateXZ(-spreadInRad * i),
                    pierce,
                    canHitEnderman
                )
            )
        }
    }


    public val playerArrows = mutableSetOf<SkyblockArrow>()

    private val currentArrowsInWorld = mutableSetOf<EntityArrow>()
    private val previousArrowsInWorld = mutableSetOf<EntityArrow>()

    private val renderRealArrowLineList = mutableListOf<Line>()
    private val renderArrowDetectLineList = mutableListOf<Line>()

    data class Line(val start: LorenzVec, val end: LorenzVec)

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        previousArrowsInWorld.clear()
        previousArrowsInWorld.addAll(currentArrowsInWorld)
        currentArrowsInWorld.clear()
        currentArrowsInWorld.addAll(EntityUtils.getEntities<EntityArrow>())

        //New Arrow
        (currentArrowsInWorld - previousArrowsInWorld).forEach { onArrowSpawn(it) }
        //Arrow Disappeared
        (previousArrowsInWorld - currentArrowsInWorld).forEach { onArrowDeSpawn(it) }

        //currentArrowsInWorld.forEach{arrow ->
        //    val speed = LorenzVec(arrow.posX-arrow.lastTickPosX,arrow.posY-arrow.lastTickPosY,arrow.posZ-arrow.lastTickPosZ).multiply(20).length()
        //    LorenzDebug.log("Arrow Speed: $speed")
        //}
        if (!config.arrowDebug) return
        currentArrowsInWorld.forEach {
            renderRealArrowLineList.add(
                Line(
                    LorenzVec(it.prevPosX, it.prevPosY, it.prevPosZ),
                    LorenzVec(it.posX, it.posY, it.posZ)
                )
            )
        }

        if (event.repeatSeconds(3)) {
            val index = upComingArrows.indexOfLast { it.getLivingTime() > 60 }
            for (i in 0..index) {
                upComingArrows.remove()
            }
            //upComingArrows.removeIf { it.getLivingTime() > 50 }
        }
    }

    private fun onArrowSpawn(arrow: EntityArrow) {
        val match = upComingArrows.firstOrNull { it.isOnParabola(arrow) } ?: return
        playerArrows.add(SkyblockArrow(arrow, match.pierce, match.canHitEnderman))
        upComingArrows.remove(match)
        LorenzDebug.log("Added Arrow, needs to find still: ${upComingArrows.count()}")
    }

    private const val ANGLE_TOLERANCE = PI * (2.0 / 3.0)
    private const val DISTANCE_TOLERANCE = 4.0
    private const val TICK_ADJUST = 4
    private fun isOnParabola(origin: LorenzVec, direction: LorenzVec, tick: Int, arrow: EntityArrow): Boolean {
        val p = tick - TICK_ADJUST
        val pointOnParabola = parabola(origin, direction, p)    //TODO Check if the parabola is correct
        if (pointOnParabola.distance(arrow.getLorenzVec()) < DISTANCE_TOLERANCE) {
            if (config.arrowDebug) {
                LorenzDebug.log("Caught Arrow at $p Tick")
                renderArrowDetectLineList.add(
                    Line(
                        pointOnParabola,
                        arrow.getLorenzVec()
                    )
                )
            }
        } else {
            LorenzDebug.log("Caught Arrow not at $p Tick")
            renderArrowDetectLineList.add(
                Line(
                    pointOnParabola,
                    arrow.getLorenzVec()
                )
            )
            return false
        }
        //TODO Debug Angle
        val angleDiffer = arrow.getLook(0.0f).toLorenzVec()
            .angleInRad(vectorFromPoints(parabola(origin, direction, p - 1), parabola(origin, direction, p)))
        LorenzDebug.log("Soll: $ANGLE_TOLERANCE, Ist: $angleDiffer")
        return angleDiffer < ANGLE_TOLERANCE
    }

    private const val GRAVITY = 0.05
    private const val DRAG = 0.99

    /**parabola(origin, direction, time) = origin + [time * direction - (0, GRAVITY * time^2, 0)] * DRAG^time
     * Time is in Ticks*/
    private fun parabola(origin: LorenzVec, direction: LorenzVec, time: Int): LorenzVec {
        val gravityEffect = LorenzVec(0.0, GRAVITY * time * time, 0.0)
        val change = direction.multiply(time)
        val changeWithGravity = change.subtract(gravityEffect)
        val dampening = DRAG.pow(time)
        val travel = changeWithGravity.multiply(dampening)
        return origin.add(travel)
    }

    private fun onArrowDeSpawn(arrow: EntityArrow) {
        val playerArrow = playerArrows.firstOrNull { it.base == arrow } ?: return
        val hitEntity = EntityKill.currentEntityLiving.firstOrNull {
            it.getPrevLorenzVec().distance(arrow.getLorenzVec()) < 4.0
        }
        if (hitEntity == null) {
            if (config.arrowDebug) {
                LorenzDebug.log("Arrow hit the ground")
            }
        } else {
            EntityKill.addToMobHitList(hitEntity, hitTrigger.Bow)
        }
        playerArrows.remove(playerArrow)
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        playerArrows.clear()
    }

    @SubscribeEvent
    fun onTickForHighlight(event: LorenzRenderWorldEvent) {
        if (!config.arrowDebug) return
        playerArrows.forEach {
            RenderUtils.drawCylinderInWorld(
                LorenzColor.GOLD.toColor(),
                it.base.position.x.toDouble(),
                it.base.position.y.toDouble(),
                it.base.position.z.toDouble(),
                1.0.toFloat(),
                1.0.toFloat(),
                event.partialTicks
            )
        }
    }

    fun getCurrentTick() = Minecraft.getMinecraft().theWorld.worldTime //TODO move it to the "correct" place

    @SubscribeEvent
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!config.arrowDebug) return
        upComingArrows.forEach {
            event.draw3DLine(
                it.origin,
                it.direction.normalize().multiply(50).add(it.origin),
                LorenzColor.LIGHT_PURPLE.toColor(),
                5,
                true
            )
            for (i in 0..50) {
                event.draw3DLine(
                    it.parabola(i),
                    it.parabola(i + 1),
                    LorenzColor.RED.toColor(),
                    5,
                    true
                )
            }
        }
        val player = Minecraft.getMinecraft().thePlayer
        val position = player.getPositionEyes(event.partialTicks).toLorenzVec()
        val direction = player.getLook(event.partialTicks).toLorenzVec()
        event.draw3DLine(position, position.add(direction), LorenzColor.DARK_GREEN.toColor(), 5, true)

        renderRealArrowLineList.forEach {
            event.draw3DLine(it.start, it.end, LorenzColor.GREEN.toColor(), 5, true)
        }
        renderArrowDetectLineList.forEach {
            event.draw3DLine(it.start, it.end, LorenzColor.YELLOW.toColor(), 10, true)
        }
    }
}