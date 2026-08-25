package ruiseki.okbackpack;

import org.apache.logging.log4j.Level;

import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.ConfigurableType;
import ruiseki.okcore.config.ConfigurableTypeCategory;
import ruiseki.okcore.config.extendedconfig.DummyConfig;
import ruiseki.okcore.tracking.Versions;

/**
 * General config for OKBackpack using OKCore config system.
 */
public class GeneralConfig extends DummyConfig {

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.CORE,
        comment = "Config version for " + Reference.MOD_NAME + ".\nDO NOT EDIT MANUALLY!")
    public static String version = Reference.MOD_VERSION;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.CORE,
        comment = "If the version checker should be enabled.")
    public static boolean useVersionChecker = true;

    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "Magnet Upgrade item suction range.")
    public static int magnetRange = 5;

    // --- Feature Toggles ---

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.CORE,
        comment = "Enable all travelers upgrade items, recipes, runtime behaviors, and related mixins.")
    public static boolean enableTravelersUpgrades = true;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.CORE,
        comment = "Enable the arcane crafting upgrade item, recipes, runtime behaviors, and Thaumcraft mixins.")
    public static boolean enableArcaneCraftingUpgrade = true;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.CORE,
        comment = "Enable Backpack inventory interaction mixins, runtime logic, and GUI rendering.",
        requiresMcRestart = true)
    public static boolean enableBackpackInventoryInteraction = true;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.CORE,
        comment = "When a backpack is opened from the player inventory, closing it returns to the inventory screen.")
    public static boolean enableInventoryBackpackCloseReturnsToInventory = true;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.CORE,
        comment = "Maximum number of item stacks to display per row in the backpack tooltip.")
    public static int tooltipMaxItemsPerRow = 9;

    /**
     * Config type instance.
     */
    public static ConfigurableType TYPE = ConfigurableType.DUMMY;

    public GeneralConfig() {
        super(OKBackpack._instance, true, "general", null);
    }

    @Override
    public void onRegistered() {
        if (!version.equals(Reference.MOD_VERSION)) {
            getMod().log(
                Level.WARN,
                "The config file of " + Reference.MOD_NAME
                    + " is out of date and might cause problems, please remove it so it can be regenerated.");
        }

        if (useVersionChecker) {
            Versions.registerMod(getMod(), OKBackpack._instance, Reference.VERSION_URL);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
