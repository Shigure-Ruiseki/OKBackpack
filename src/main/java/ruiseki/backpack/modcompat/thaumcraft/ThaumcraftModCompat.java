package ruiseki.backpack.modcompat.thaumcraft;

import ruiseki.backpack.modcompat.thaumcraft.client.gui.sync.ThaumcraftSHRegistries;
import ruiseki.backpack.modcompat.thaumcraft.item.ItemArcaneCraftingUpgradeConfig;
import ruiseki.backpack.modcompat.thaumcraft.upgrades.energizednode.ItemEnergizedNodeUpgradeConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigHandler;
import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.modcompat.IModCompat;

public class ThaumcraftModCompat implements IModCompat {

    /**
     * If the modcompat can be used.
     */
    public static boolean canBeUsed = false;

    public ThaumcraftModCompat() {

    }

    @Override
    public String getModID() {
        return Mods.Thaumcraft.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Integration for Thaumcraft upgrades.";
    }

    @Override
    public void onInit(Step initStep) {
        if (initStep == Step.PREINIT) {
            canBeUsed = OKBackpack._instance.getModCompatLoader()
                .shouldLoadModCompat(this);

            ThaumcraftSHRegistries.load();

            ConfigHandler configHandler = OKBackpack._instance.getConfigHandler();
            configHandler.add(new ItemArcaneCraftingUpgradeConfig());
            configHandler.add(new ItemEnergizedNodeUpgradeConfig());
        }
    }
}
