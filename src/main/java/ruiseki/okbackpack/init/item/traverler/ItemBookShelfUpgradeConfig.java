package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.bat.ItemBatUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemBookShelfUpgradeConfig extends ItemConfig {

    public static ItemBookShelfUpgradeConfig _instance;

    public ItemBookShelfUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "bookshelf_upgrade",
            null,
            config -> new ItemBatUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.bookshelf_upgrade.tooltip"))));
    }
}
