package ruiseki.okbackpack.init.item.refill;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.refill.AdvancedRefillSlotUpdate;
import ruiseki.okbackpack.upgrades.refill.ItemRefillUpgrade;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdateRegistry;

public class ItemAdvRefillUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvRefillUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.advanced_refill",
        comment = "Whether to enable target slot selection.",
        configLocation = ConfigLocation.SERVER)
    public static boolean targetSlotSelection = true;

    @ConfigurableProperty(
        category = "upgrade.advanced_refill",
        comment = "Whether this upgrade supports block picking.",
        configLocation = ConfigLocation.SERVER)
    public static boolean supportsBlockPick = true;

    /**
     * Make a new instance.
     */
    public ItemAdvRefillUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_refill_upgrade",
            null,
            config -> new ItemRefillUpgrade(() -> 12, () -> targetSlotSelection, () -> supportsBlockPick)
                .tooltipBuilder(
                    builder -> builder
                        .add(LangHelpers.localize("item.items.okbackpack.advanced_refill_upgrade.tooltip"))
                        .add(LangHelpers.localize("item.items.okbackpack.advanced_refill_upgrade.tooltip.1"))));
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        UpgradeSlotUpdateRegistry.register(new AdvancedRefillSlotUpdate());
    }
}
