package ruiseki.backpack.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.backpack.upgrades.anvil.AnvilUpgradeWrapper;
import ruiseki.backpack.upgrades.deposit.DepositFilterType;
import ruiseki.backpack.upgrades.deposit.DepositUpgradeWrapper;
import ruiseki.backpack.upgrades.inception.InceptionUpgradeWrapper;
import ruiseki.backpack.upgrades.inception.InventoryOrder;
import ruiseki.backpack.upgrades.refill.RefillUpgradeWrapper;
import ruiseki.backpack.upgrades.toolswapper.ToolSwapMode;
import ruiseki.backpack.upgrades.toolswapper.ToolSwapperUpgradeWrapper;
import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegistry;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class OBUpgradeSlotSHRegistries {

    public static final StorageSHRegistry.Entry<UpgradeSlotSH> REGISTRY = StorageSHRegistry.get(UpgradeSlotSH.class);

    public static final ResourceLocation INVENTORY_ORDER = new ResourceLocation(
        Reference.MOD_ID,
        "backpack_inventory_order");
    public static final ResourceLocation DEPOSIT_TYPE = new ResourceLocation(Reference.MOD_ID, "backpack_deposit_type");
    public static final ResourceLocation SHIFT_INTO_STORAGE = new ResourceLocation(
        Reference.MOD_ID,
        "backpack_shift_into_storage");

    public static final ResourceLocation REFILL_TARGET_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "backpack_refill_target_slot");

    public static final ResourceLocation WEAPON_SWAP = new ResourceLocation(Reference.MOD_ID, "backpack_weapon_swap");
    public static final ResourceLocation TOOL_SWAP = new ResourceLocation(Reference.MOD_ID, "backpack_tool_swap");

    public static void load() {
        REGISTRY.registerServer(INVENTORY_ORDER, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof InceptionUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setInventoryOrder(InventoryOrder.fromOrdinal(buf.readInt()));
        });
        REGISTRY.registerServer(DEPOSIT_TYPE, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof DepositUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.getFilterLogic()
                .setDepositFilterType(DepositFilterType.fromOrdinal(buf.readInt()));
        });
        REGISTRY.registerServer(SHIFT_INTO_STORAGE, (slot, buf) -> {
            boolean shiftIntoStorage = buf.readBoolean();
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof AnvilUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setShiftClickIntoStorage(shiftIntoStorage);
        });
        REGISTRY.registerServer(REFILL_TARGET_SLOT, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof RefillUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setTargetSlot(buf.readInt(), RefillUpgradeWrapper.TargetSlot.fromOrdinal(buf.readInt()));
        });
        REGISTRY.registerServer(WEAPON_SWAP, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof ToolSwapperUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setSwapWeapon(buf.readBoolean());
        });
        REGISTRY.registerServer(TOOL_SWAP, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof ToolSwapperUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setToolSwapMode(ToolSwapMode.fromOrdinal(buf.readInt()));
        });
    }
}
