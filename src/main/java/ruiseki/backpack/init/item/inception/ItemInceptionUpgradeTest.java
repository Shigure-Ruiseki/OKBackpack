package ruiseki.backpack.init.item.inception;

import ruiseki.backpack.upgrades.inception.ItemInceptionUpgrade;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemInceptionUpgradeTest extends ItemConfig {

    public static ItemInceptionUpgradeTest _instance;

    public ItemInceptionUpgradeTest() {
        super(OKBackpack._instance, true, "inception_upgrade_test", null, config -> new ItemInceptionUpgrade());
    }
}
