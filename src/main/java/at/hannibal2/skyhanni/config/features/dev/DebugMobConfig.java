package at.hannibal2.skyhanni.config.features.dev;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class DebugMobConfig {

    @Expose
    @ConfigOption(name = "Mob Detection Enable", desc = "Turn off and on again to reset all mobs.")
    @ConfigEditorBoolean
    public boolean enable = true;

    @Expose
    @ConfigOption(name = "Copy NPC", desc = "Copies an npc you hit into the a json for neu repo to the path specified below.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyNpc = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Copy NPC path", desc = "Path where the npc file will get saved.")
    @ConfigEditorText
    public String copyNpcPath = "";

    @Expose
    @ConfigOption(name = "Mob Detection", desc = "Debug feature to check the Mob Detection.")
    @Accordion
    public MobDetection mobDetection = new MobDetection();

    public enum HowToShow {
        OFF("Off"),
        ONLY_NAME("Only Name"),
        ONLY_HIGHLIGHT("Only Highlight"),
        NAME_AND_HIGHLIGHT("Both");

        final String str;

        HowToShow(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    public static class MobDetection {

        @Expose
        @ConfigOption(name = "Log Events", desc = "Logs the spawn and despawn event with full mob info.")
        @ConfigEditorBoolean
        public boolean logEvents = false;

        @Expose
        @ConfigOption(name = "Show RayHit", desc = "Highlights the mob that is currently in front of your view.")
        @ConfigEditorBoolean
        public boolean showRayHit = false;

        @Expose
        @ConfigOption(name = "Player Highlight", desc = "Highlight each entity that is a real Player in blue (you are also included in the list but won't be highlighted for obvious reasons).")
        @ConfigEditorBoolean
        public boolean realPlayerHighlight = false;

        @Expose
        @ConfigOption(name = "DisplayNPC", desc = "Shows the internal mobs that are 'DisplayNPC' as highlight (in red) or the name.")
        @ConfigEditorDropdown
        public HowToShow displayNPC = HowToShow.OFF;

        @Expose
        @ConfigOption(name = "SkyblockMob", desc = "Shows the internal mobs that are 'SkyblockMob' as highlight (in green) or the name.")
        @ConfigEditorDropdown
        public HowToShow skyblockMob = HowToShow.OFF;

        @Expose
        @ConfigOption(name = "Summon", desc = "Shows the internal mobs that are 'Summon' as highlight (in yellow) or the name.")
        @ConfigEditorDropdown
        public HowToShow summon = HowToShow.OFF;

        @Expose
        @ConfigOption(name = "Special", desc = "Shows the internal mobs that are 'Special' as highlight (in aqua) or the name.")
        @ConfigEditorDropdown
        public HowToShow special = HowToShow.OFF;

        @Expose
        @ConfigOption(name = "Show Invisible", desc = "Shows the mob even though they are invisible (do to invisibility effect) if looked at directly.")
        @ConfigEditorBoolean
        public boolean showInvisible = false;
    }
}
