package ruiseki.okstoragecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.helper.StorageInventoryHelpers;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.crafting.CraftingUpgradeWrapper;

public class UpgradeSlotSHRegistries {

    public static final StorageSHRegistry.Entry<UpgradeSlotSH> REGISTRY = StorageSHRegistry.get(UpgradeSlotSH.class);

    public static final ResourceLocation ENABLE = new ResourceLocation(Reference.MOD_ID, "storage_enable");
    public static final ResourceLocation CRAFTING_UPGRADE = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_upgrade");
    public static final ResourceLocation CRAFTING_ROTATED = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_rotated");
    public static final ResourceLocation CRAFTING_BALANCE = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_balance");
    public static final ResourceLocation CRAFTING_SPREAD = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_spread");
    public static final ResourceLocation CRAFTING_CLEAR = new ResourceLocation(
        Reference.MOD_ID,
        "storage_crafting_clear");
    public static final ResourceLocation FILTER_LOGIC = new ResourceLocation(Reference.MOD_ID, "storage_filter_logic");

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
        REGISTRY.registerServer(CRAFTING_ROTATED, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            StorageInventoryHelpers.rotated(upgradeWrapper.getStorage(), buf.readBoolean());
        });
        REGISTRY.registerServer(CRAFTING_BALANCE, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            StorageInventoryHelpers.balance(upgradeWrapper.getStorage());
        });
        REGISTRY.registerServer(CRAFTING_SPREAD, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            StorageInventoryHelpers.spread(upgradeWrapper.getStorage());
        });
        REGISTRY.registerServer(CRAFTING_CLEAR, (slot, buf) -> {
            boolean intoInventory = buf.readBoolean();
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof CraftingUpgradeWrapper upgradeWrapper)) return;
            StorageInventoryHelpers
                .clear(slot.getPanel(), slot.getWrapper(), upgradeWrapper.getStorage(), intoInventory);
        });
        REGISTRY.registerServer(FILTER_LOGIC, (slot, buf) -> {

        });
    }
}
