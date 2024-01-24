package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.events.DrawScreenAfterEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent.CloseWindowEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent.SlotClickEvent
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.Slot
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class GuiContainerHook(guiAny: Any) {

    val gui: GuiContainer

    init {
        gui = guiAny as GuiContainer
    }

    fun closeWindowPressed(ci: CallbackInfo) {
        GlStateManager.pushMatrix()
        if (CloseWindowEvent(gui, gui.inventorySlots).postAndCatch()) ci.cancel()
        GlStateManager.popMatrix()
    }

    fun backgroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        GlStateManager.pushMatrix()
        GuiContainerEvent.BackgroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).postAndCatch()
        GlStateManager.popMatrix()
    }

    fun foregroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GlStateManager.pushMatrix()
        GuiContainerEvent.ForegroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).postAndCatch()
        GlStateManager.popMatrix()
    }

    fun onDrawSlot(slot: Slot, ci: CallbackInfo) {
        GlStateManager.pushMatrix()
        val event = GuiContainerEvent.DrawSlotEvent.GuiContainerDrawSlotPre(gui, gui.inventorySlots, slot)
        if (event.postAndCatch()) ci.cancel()
        GlStateManager.popMatrix()
    }

    fun onDrawSlotPost(slot: Slot) {
        GlStateManager.pushMatrix()
        GuiContainerEvent.DrawSlotEvent.GuiContainerDrawSlotPost(gui, gui.inventorySlots, slot).postAndCatch()
        GlStateManager.popMatrix()
    }

    fun onMouseClick(slot: Slot?, slotId: Int, clickedButton: Int, clickType: Int, ci: CallbackInfo) {
        GlStateManager.pushMatrix()
        if (SlotClickEvent(gui, gui.inventorySlots, slot, slotId, clickedButton, clickType).postAndCatch()) ci.cancel()
        GlStateManager.popMatrix()
    }

    fun onDrawScreenAfter(
        mouseX: Int,
        mouseY: Int,
        ci: CallbackInfo,
    ) {
        GlStateManager.pushMatrix()
        if (DrawScreenAfterEvent(mouseX, mouseY, ci).postAndCatch()) ci.cancel()
        GlStateManager.popMatrix()
    }
}
