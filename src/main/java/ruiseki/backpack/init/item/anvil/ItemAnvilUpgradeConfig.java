package ruiseki.backpack.init.item.anvil;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdateRegistry;
import ruiseki.okstoragecore.upgrades.anvil.AnvilSlotUpdate;
import ruiseki.okstoragecore.upgrades.anvil.ItemAnvilUpgrade;

public class ItemAnvilUpgradeConfig extends ItemConfig {

    public static ItemAnvilUpgradeConfig _instance;

    public ItemAnvilUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "anvil_upgrade",
            null,
            config -> new ItemAnvilUpgrade()
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.anvil_upgrade.tooltip")));
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        UpgradeSlotUpdateRegistry.register(new AnvilSlotUpdate());
    }
}
