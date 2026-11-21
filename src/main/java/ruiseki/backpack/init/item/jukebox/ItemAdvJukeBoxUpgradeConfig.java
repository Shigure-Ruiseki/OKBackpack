package ruiseki.backpack.init.item.jukebox;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.jukebox.ItemJukeboxUpgrade;

public class ItemAdvJukeBoxUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvJukeBoxUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvJukeBoxUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_jukebox_upgrade",
            null,
            config -> new ItemJukeboxUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.advanced_jukebox_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.advanced_jukebox_upgrade.tooltip.1"))));
    }

}
