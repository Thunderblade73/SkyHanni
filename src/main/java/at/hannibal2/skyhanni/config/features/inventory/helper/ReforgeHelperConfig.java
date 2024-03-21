package at.hannibal2.skyhanni.config.features.inventory.helper;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class ReforgeHelperConfig {

    @Expose
    public Position posList = new Position(-200, 85, true, true);
    @Expose
    public Position posCurrent = new Position(280, 45, true, true);

    @Expose
    @ConfigOption(name = "Enable", desc = "Enables the reforge helper")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enable = true;

    @Expose
    @ConfigOption(name = "Reforge Stones Hex Only", desc = "Displays reforge stones only when in Hex")
    @ConfigEditorBoolean
    public boolean reforgeStonesOnlyHex = true;

    @Expose
    @ConfigOption(name = "Hide chat", desc = "Hides the vanilla chat messages from reforging")
    @ConfigEditorBoolean
    public boolean hideChat = false;
}
