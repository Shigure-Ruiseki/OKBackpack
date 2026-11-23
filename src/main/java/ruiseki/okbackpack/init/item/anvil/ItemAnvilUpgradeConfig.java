package ruiseki.okbackpack.init.item.anvil;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.anvil.AnvilSlotUpdate;
import ruiseki.okbackpack.upgrades.anvil.ItemAnvilUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdateRegistry;

public class ItemAnvilUpgradeConfig extends ItemConfig {

    public static ItemAnvilUpgradeConfig _instance;

    public ItemAnvilUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "anvil_upgrade",
            null,
            config -> new ItemAnvilUpgrade().tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.anvil_upgrade.tooltip"))));
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        UpgradeSlotUpdateRegistry.register(new AnvilSlotUpdate());
    }
}
