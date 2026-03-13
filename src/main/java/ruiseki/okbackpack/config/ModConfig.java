package ruiseki.okbackpack.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import ruiseki.okbackpack.Reference;

@Config.LangKey("config.generalConfig")
@Config(modid = Reference.MOD_ID, configSubDirectory = Reference.MOD_ID, category = "general")
public class ModConfig {

    public static void registerConfig() throws ConfigException {
        ConfigurationManager.registerConfig(ModConfig.class);
    }

    @Config.DefaultInt(27)
    @Config.RangeInt(min = 1)
    public static int leatherBackpackSlots;

    @Config.DefaultInt(54)
    @Config.RangeInt(min = 1)
    public static int ironBackpackSlots;

    @Config.DefaultInt(81)
    @Config.RangeInt(min = 1)
    public static int goldBackpackSlots;

    @Config.DefaultInt(108)
    @Config.RangeInt(min = 1)
    public static int diamondBackpackSlots;

    @Config.DefaultInt(120)
    @Config.RangeInt(min = 1)
    public static int obsidianBackpackSlots;

    @Config.DefaultInt(1)
    @Config.RangeInt(min = 1)
    public static int leatherUpgradeSlots;

    @Config.DefaultInt(2)
    @Config.RangeInt(min = 1)
    public static int ironUpgradeSlots;

    @Config.DefaultInt(3)
    @Config.RangeInt(min = 1)
    public static int goldUpgradeSlots;

    @Config.DefaultInt(5)
    @Config.RangeInt(min = 1)
    public static int diamondUpgradeSlots;

    @Config.DefaultInt(7)
    @Config.RangeInt(min = 1)
    public static int obsidianUpgradeSlots;

    @Config.DefaultInt(2)
    @Config.RangeInt(min = 1)
    public static int stackUpgradeTier1Mul;

    @Config.DefaultInt(4)
    @Config.RangeInt(min = 1)
    public static int stackUpgradeTier2Mul;

    @Config.DefaultInt(8)
    @Config.RangeInt(min = 1)
    public static int stackUpgradeTier3Mul;

    @Config.DefaultInt(16)
    @Config.RangeInt(min = 1)
    public static int stackUpgradeTier4Mul;

    @Config.DefaultInt(33554431)
    @Config.RangeInt(min = 1)
    public static int stackUpgradeTierOmegaMul;

    @Config.DefaultInt(5)
    public static int magnetRange;
}
