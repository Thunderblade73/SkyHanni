package at.hannibal2.skyhanni.features.foraging

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEventLate
import at.hannibal2.skyhanni.events.entity.EntityLeaveWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ModernPatterns
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.compat.formattedTextCompat
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.StringRenderable
import net.minecraft.entity.decoration.ArmorStandEntity

@SkyHanniModule
object TreeProgressDisplay {

    private val config get() = SkyHanniMod.feature.foraging.trees.progress
    private var display: Renderable? = null
    private var armorstandId: Int? = null

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.enabled) return
        if (display == null) return
        config.position.renderRenderable(display, posLabel = "Tree Progress")
    }

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onEntityEnterWorld(event: EntityEnterWorldEventLate<ArmorStandEntity>) {
        if (!config.enabled) return
        val name = event.entity.displayName.formattedTextCompat()
        if (ModernPatterns.currentTreeProgressPattern.matches(name)) {
            armorstandId = event.entity.id
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GALATEA)
    fun onTick(event: SkyHanniTickEvent) {
        if (!config.enabled && event.isMod(4)) return
        val armorstand = armorstandId?.let { EntityUtils.getEntityByID(it) } ?: run {
            display = null
            armorstandId = null
            return
        }
        display = if (config.onlyHoldingAxe && InventoryUtils.getItemInHand()?.getItemCategoryOrNull() != ItemCategory.AXE) {
            null
        } else {
            val name = armorstand.displayName.formattedTextCompat()
            if (config.compact) {
                ModernPatterns.currentTreeProgressPattern.matchMatcher(name) {
                    StringRenderable("${group("treeType")} §b§l${group("percent")}%")
                }
            } else {
                StringRenderable(name)
            }
        }
    }
}
