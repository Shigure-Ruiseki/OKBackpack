package ruiseki.backpack.modcompat.thaumcraft.item;

import ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane.ArcaneSlotUpdate;
import ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane.ItemArcaneCraftingUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdateRegistry;

public class ItemArcaneCraftingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemArcaneCraftingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemArcaneCraftingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "arcane_crafting_upgrade",
            null,
            config -> new ItemArcaneCraftingUpgrade(GeneralConfig.maxUpgradesPerStorageConfig));
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        UpgradeSlotUpdateRegistry.register(new ArcaneSlotUpdate());
    }
}
