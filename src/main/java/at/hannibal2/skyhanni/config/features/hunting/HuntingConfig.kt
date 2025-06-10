package at.hannibal2.skyhanni.config.features.hunting

import at.hannibal2.skyhanni.config.OnlyLegacy
import at.hannibal2.skyhanni.config.OnlyModern
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import org.lwjgl.input.Keyboard

class HuntingConfig {

    @ConfigOption(
        name = "§cNotice",
        desc = "To see all hunting features please launch the game on a modern version of Minecraft with SkyHanni installed.",
    )
    @OnlyLegacy
    @ConfigEditorInfoText
    var notice: String = ""

    @Expose
    @ConfigOption(name = "Panda Helper", desc = "")
    @Accordion
    @OnlyModern
    var pandaHelper = PandaHelper()

    class PandaHelper {

        @Expose
        @ConfigOption(name = "Enabled", desc = "Enables the helper for Pandas.")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(true)

        @Expose
        @ConfigOption(name = "Information", desc = "Information that is shown by the helper above a panda")
        @ConfigEditorDropdown
        var whatToShow: Property<List<PandaLines>> = Property.of(listOf(PandaLines.UNTIL_STAGE, PandaLines.UNTIL_TOTAL))

        @Expose
        @ConfigOption(name = "Remember last Panda", desc = "Remembers the last Panda you feed.")
        @ConfigEditorBoolean
        var remember = false

        @Expose
        @ConfigOption(name = "Path to Remembered", desc = "By pressing your key you will be path to the last know location of your panda.")
        @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
        var pathToRemembered = Keyboard.KEY_NONE

        @Expose
        @ConfigOption(name = "Show Remembered on HUD", desc = "Shows your information of your remembered Panda.")
        @ConfigEditorBoolean
        var rememberHud = true

        @Expose
        @ConfigLink(owner = PandaHelper::class, field = "enabled")
        var rememberHudPosition = Position(20, 20)

        enum class PandaLines(val s: String) {
            UNTIL_TOTAL("Needs 40x-43x"),
            UNTIL_STAGE("Stage needs: 4x-6x "),
            FEED_TOTAL("Feed 25x"),
            FEED_STAGE("Stage feed 5x"),
            ;

            override fun toString(): String = s
        }
    }
}
