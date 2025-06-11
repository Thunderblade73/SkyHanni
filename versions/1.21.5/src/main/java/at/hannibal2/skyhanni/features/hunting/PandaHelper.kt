package at.hannibal2.skyhanni.features.hunting

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.hunting.HuntingConfig.PandaHelper.PandaLines
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.entity.EntityClickEvent
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.hunting.PandaHelper.format
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.onDisable
import at.hannibal2.skyhanni.utils.ConditionalUtils.onEnable
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.MobUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.removeIfValue
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawString
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableString
import net.minecraft.entity.passive.PandaEntity
import net.minecraft.item.Items
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object PandaHelper {

    private val config get() = SkyHanniMod.feature.hunting.pandaHelper

    private val pandaData = mutableMapOf<Int, PandaData>()
    private var remembered: PandaData? = null

    private fun clear() {
        pandaData.clear()
        remembered = null
    }

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) = handleMobIn(event.mob)

    private fun handleMobIn(mob: Mob) {
        val entity = mob.baseEntity
        if (entity !is PandaEntity || mob.name != "Mochibear") return
        val data = pandaData[entity.id]
        if (data == null) {
            val data = PandaData(entity)
            pandaData[data.id] = data
        } else {
            data.updateEntity(entity)
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onTick() {
        pandaData.removeIfValue {
            it.updatePosition()
            it.stage == PandaStage.DONE || it.lifeTime.isInPast()
        }
        val target = MobUtils.rayTraceForMob(MinecraftCompat.localPlayer, 0f) ?: return
        val data = pandaData[target.id] ?: return
        val entity = target.baseEntity as? PandaEntity ?: return
        data.updateEntity(entity)
    }

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onEntityClick(event: EntityClickEvent) {
        val data = pandaData[event.clickedEntity?.id ?: return] ?: return
        if (event.itemInHand?.item != Items.BAMBOO) return
        data.feed()
        remembered = data
    }

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onSkyHanniRenderWorld(event: SkyHanniRenderWorldEvent) {
        pandaData.forEach { _, data ->
            if (!data.location.canBeSeen()) return@forEach
            data.display.forEachIndexed { i, line ->
                event.drawString(data.location.add(y = 1.0 * i), line, seeThroughBlocks = false)
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onGuiRender(event: GuiRenderEvent) {
        val data = remembered ?: return
        config.rememberHudPosition.renderRenderables(
            buildList {
                add(RenderableString("§6Last Panda:"))

                addAll(data.display.map { RenderableString(it) })

                add(Renderable.clickable("§lPath to Panda", ::pathToRememberedPanda))
            },
            posLabel = "Panda Hud",
        )
    }

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onKeyPress(event: KeyPressEvent) {
        if(event.keyCode != config.pathToRemembered) return
        pathToRememberedPanda()
    }

    @HandleEvent
    fun onIslandChange() {
        clear()
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.enabled.onDisable {
            clear()
        }
        if(!IslandType.GALATEA.isCurrent()) return
        config.enabled.onEnable {
            MobData.skyblockMobs.forEach { handleMobIn(it) }
        }
        config.whatToShow.onToggle {
            pandaData.forEach {
                it.value.updateDisplay()
            }
        }
    }

    private fun pathToRememberedPanda() {
        val data = remembered ?: return
        IslandGraphs.pathFind(data.location, "Last Panda") { data.location.distanceSqToPlayer() < 5 * 5 }
    }

    private class PandaData(
        val id: Int,
        var stage: PandaStage,
        var location: LorenzVec,
    ) {
        var feed: Int = 0
        var feedStage: Int = 0
        var lifeTime: SimpleTimeMark = refreshLifetime()

        var display = emptyList<String>()

        private fun refreshLifetime() = 5.0.minutes.fromNow()

        constructor(entity: PandaEntity) : this(entity.id, entity.getStage(), entity.getLorenzVec())

        fun feed() {
            feed++
            feedStage++
            updateDisplay()
        }

        fun updatePosition() {
            val newLocation = EntityUtils.getEntityByID(id)?.getLorenzVec()
            if (newLocation == null) return
            location = newLocation
            lifeTime = refreshLifetime()
        }

        fun updateEntity(entity: PandaEntity) {
            lifeTime = refreshLifetime()
            val newStage = entity.getStage()
            if (newStage == stage) return
            ChatUtils.debug("Panda was feed with: $feedStage for $stage")
            feedStage = 0
            if (newStage < stage) {
                feed = 0
            }
            stage = newStage
            updateDisplay()
        }

        fun updateDisplay() {
            display = config.whatToShow.get().map { it.format(this) }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PandaData

            return id == other.id
        }

        override fun hashCode(): Int {
            return id
        }

    }

    private fun PandaLines.format(data: PandaData) = when (this) {
        PandaLines.UNTIL_TOTAL -> "Needs ${data.stage.cumulativeMin}x-${data.stage.cumulativeMax}x"
        PandaLines.UNTIL_STAGE -> "Stage needs ${data.stage.min}x-${data.stage.max}x"
        PandaLines.FEED_TOTAL -> "Feed ${data.feed}x"
        PandaLines.FEED_STAGE -> "Stage feed ${data.feedStage}x"
    }

    private fun PandaEntity.getStage(): PandaStage {
        val isBaby = this.isBaby
        val scale = this.scale
        return PandaStage.entries.firstOrNull {
            it.isBaby == isBaby && it.scale == scale
        } ?: PandaStage.DONE
    }

    private enum class PandaStage(val isBaby: Boolean, val scale: Float, median: Int, deviation: Int) {
        BASE(true, 0.8f, 4, 0),
        STAGE1(true, 1f, 5, 1),
        STAGE2(true, 1.2f, 6, 1),
        STAGE3(false, 0.8f, 7, 1),
        STAGE4(false, 1f, 8, 1),
        STAGE5(false, 1.2f, 10, 0),
        DONE(false, Float.MAX_VALUE, 0, 0)
        ;
        val min = median - deviation
        val max = median + deviation

        val cumulativeMin = entries.subList(ordinal, entries.size).sumOf { it.min }
        val cumulativeMax = entries.subList(ordinal, entries.size).sumOf { it.max }
    }
}
