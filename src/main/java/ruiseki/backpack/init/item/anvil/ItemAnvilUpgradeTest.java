package ruiseki.backpack.init.item.anvil;

import ruiseki.backpack.upgrades.anvil.AnvilSlotUpdate;
import ruiseki.backpack.upgrades.anvil.ItemAnvilUpgrade;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdateRegistry;

public class ItemAnvilUpgradeTest extends ItemConfig {

    public static ItemAnvilUpgradeTest _instance;

    public ItemAnvilUpgradeTest() {
        super(OKBackpack._instance, true, "anvil_upgrade_test", null, config -> new ItemAnvilUpgrade());
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        UpgradeSlotUpdateRegistry.register(new AnvilSlotUpdate());
    }
}
