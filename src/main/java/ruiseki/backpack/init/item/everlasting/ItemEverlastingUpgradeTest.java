package ruiseki.backpack.init.item.everlasting;

import ruiseki.backpack.upgrades.everlasting.ItemEverlastingUpgrade;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemEverlastingUpgradeTest extends ItemConfig {

    public static ItemEverlastingUpgradeTest _instance;

    public ItemEverlastingUpgradeTest() {
        super(OKBackpack._instance, true, "everlasting_upgrade_test", null, config -> new ItemEverlastingUpgrade());
    }
}
