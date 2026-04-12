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

    @Config.DefaultDouble(1.5)
    @Config.RangeDouble(min = 0.1)
    public static double stackUpgradeStarterMul;

    @Config.DefaultDouble(2)
    @Config.RangeDouble(min = 1)
    public static double stackUpgradeTier1Mul;

    @Config.DefaultDouble(4)
    @Config.RangeDouble(min = 1)
    public static double stackUpgradeTier2Mul;

    @Config.DefaultDouble(8)
    @Config.RangeDouble(min = 1)
    public static double stackUpgradeTier3Mul;

    @Config.DefaultDouble(16)
    @Config.RangeDouble(min = 1)
    public static double stackUpgradeTier4Mul;

    @Config.DefaultDouble(33554431)
    @Config.RangeDouble(min = 1)
    public static double stackUpgradeTierOmegaMul;

    @Config.DefaultInt(8)
    @Config.RangeInt(min = 1)
    public static int stackDowngradeTier1Div;

    @Config.DefaultInt(16)
    @Config.RangeInt(min = 1)
    public static int stackDowngradeTier2Div;

    @Config.DefaultInt(32)
    @Config.RangeInt(min = 1)
    public static int stackDowngradeTier3Div;

    @Config.DefaultInt(5)
    public static int magnetRange;

    @Config.Comment("Maximum number of item stacks to display per row in the backpack tooltip")
    @Config.DefaultInt(9)
    @Config.RangeInt(min = 1)
    public static int tooltipMaxItemsPerRow;
}
