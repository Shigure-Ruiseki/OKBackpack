package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.bat.ItemBatUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemBookShelfUpgradeConfig extends ItemConfig {

    public static ItemBookShelfUpgradeConfig _instance;

    public ItemBookShelfUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "bookshelf_upgrade",
            null,
            config -> new ItemBatUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.bookshelf_upgrade.tooltip")));
    }
}
