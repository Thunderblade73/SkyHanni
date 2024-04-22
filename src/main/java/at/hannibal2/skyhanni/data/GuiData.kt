package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.NEURenderEvent
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object GuiData {

    var preDrawEventCanceled = false

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onNeuRenderEvent(event: NEURenderEvent) {
        if (preDrawEventCanceled) event.cancel()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onClick(event: GuiContainerEvent.SlotClickEvent) {
        if (preDrawEventCanceled) event.cancel()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGuiClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (preDrawEventCanceled) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGuiKeyPress(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val (escKey, invKey) = Minecraft.getMinecraft().gameSettings.let {
            Keyboard.KEY_ESCAPE to it.keyBindInventory.keyCode
        }
        if (escKey.isKeyHeld() || invKey.isKeyHeld()) return
        if (preDrawEventCanceled) event.isCanceled = true
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        preDrawEventCanceled = false
    }
}