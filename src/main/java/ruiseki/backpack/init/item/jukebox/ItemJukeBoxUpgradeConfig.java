package ruiseki.backpack.init.item.jukebox;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.jukebox.ItemJukeboxUpgrade;

public class ItemJukeBoxUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemJukeBoxUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemJukeBoxUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "jukebox_upgrade",
            null,
            config -> new ItemJukeboxUpgrade(() -> 1, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.jukebox_upgrade.tooltip"))));
    }

}
