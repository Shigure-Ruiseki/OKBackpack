package ruiseki.okstoragecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.crafting.CraftingUpgradeWrapper;

public class UpgradeSlotSHRegistries {

    public static final StorageSHRegistry.Entry<UpgradeSlotSH> REGISTRY = StorageSHRegistry.get(UpgradeSlotSH.class);

    public static final ResourceLocation ENABLE = new ResourceLocation(Reference.MOD_ID, "storage_enable");
    public static final ResourceLocation CRAFTING_UPGRADE = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_upgrade");

    public static void load() {

        REGISTRY.registerServer(ENABLE, (slot, buf) -> {
            boolean enable = buf.readBoolean();
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            wrapper.setEnabled(enable);
        });

        REGISTRY.registerServer(CRAFTING_UPGRADE, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setRefillCraftingGrid(buf.readBoolean());
            upgradeWrapper.setShiftClickIntoStorage(buf.readBoolean());
        });
    }
}
