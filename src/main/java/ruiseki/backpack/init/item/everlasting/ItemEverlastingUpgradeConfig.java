package ruiseki.backpack.init.item.everlasting;

import ruiseki.backpack.upgrades.everlasting.ItemEverlastingUpgrade;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemEverlastingUpgradeConfig extends ItemConfig {

    public static ItemEverlastingUpgradeConfig _instance;

    public ItemEverlastingUpgradeConfig() {
        super(OKBackpack._instance, true, "everlasting_upgrade", null, config -> new ItemEverlastingUpgrade());
    }
}
