package ruiseki.backpack.init.item.crafting;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdateRegistry;
import ruiseki.okstoragecore.upgrades.crafting.CraftingSlotUpdate;
import ruiseki.okstoragecore.upgrades.crafting.ItemCraftingUpgrade;

public class ItemCraftingUpgradeTest extends ItemConfig {

    public static ItemCraftingUpgradeTest _instance;

    public ItemCraftingUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "crafting_upgrade_test",
            null,
            config -> new ItemCraftingUpgrade(GeneralConfig.maxUpgradesPerStorageConfig));
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        UpgradeSlotUpdateRegistry.register(new CraftingSlotUpdate());
    }
}
