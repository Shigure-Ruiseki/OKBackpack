package ruiseki.backpack.init.item.refill;

import ruiseki.backpack.upgrades.refill.BasicRefillSlotUpdate;
import ruiseki.backpack.upgrades.refill.ItemRefillUpgrade;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdateRegistry;

public class ItemRefillUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemRefillUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.refill",
        comment = "Whether to enable target slot selection.",
        configLocation = ConfigLocation.SERVER)
    public static boolean targetSlotSelection = true;

    @ConfigurableProperty(
        category = "upgrade.refill",
        comment = "Whether this upgrade supports block picking.",
        configLocation = ConfigLocation.SERVER)
    public static boolean supportsBlockPick = true;

    /**
     * Make a new instance.
     */
    public ItemRefillUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "refill_upgrade",
            null,
            config -> new ItemRefillUpgrade(() -> 6, () -> targetSlotSelection, () -> supportsBlockPick).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.refill_upgrade.tooltip"))));
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        UpgradeSlotUpdateRegistry.register(new BasicRefillSlotUpdate());
    }
}
