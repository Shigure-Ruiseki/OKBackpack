package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.storagecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackUpgradeTest extends ItemConfig {

    public static ItemStackUpgradeTest _instance;

    public ItemStackUpgradeTest() {
        super(OKBackpack._instance, true, "stack_upgrade_test", null, config -> new ItemStackUpgrade(2, null));
    }
}
