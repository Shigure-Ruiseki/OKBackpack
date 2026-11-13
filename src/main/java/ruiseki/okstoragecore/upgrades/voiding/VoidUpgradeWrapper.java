package ruiseki.okstoragecore.upgrades.voiding;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;
import ruiseki.okstoragecore.api.ISlotChangeResponseUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.inventory.InventoryHandler;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.IInsertResponseUpgrade;
import ruiseki.okstoragecore.upgrades.IOverflowResponseUpgrade;
import ruiseki.okstoragecore.upgrades.ISlotLimitUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.PrimaryMatch;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class VoidUpgradeWrapper extends UpgradeWrapperBase<VoidUpgradeWrapper, ItemVoidUpgrade>
    implements IInsertResponseUpgrade, IFilteredUpgrade, ISlotChangeResponseUpgrade, ITickableUpgrade,
    IOverflowResponseUpgrade, ISlotLimitUpgrade {

    private final FilterLogic filterLogic;
    private final int slotCount;
    private final Set<Integer> slotsToVoid = new HashSet<>();
    private boolean shouldVoidOverflow;

    public VoidUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount());
        this.slotCount = upgradeItem.getFilterSlotCount();
        filterLogic.setAllowByDefault(true);
        setShouldVoidOverflowDefaultOrLoadFromNbt(false);
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public ItemStack onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemStack stack,
        boolean simulate) {
        if (shouldVoidOverflow && ItemHelpers.isEmpty(inventoryHandler.getStackInSlot(slot))
            && (!filterLogic.shouldMatchNbt() || !filterLogic.shouldMatchDurability()
                || filterLogic.getPrimaryMatch() != PrimaryMatch.ITEM)
            && filterLogic.matchesFilter(stack)) {
            for (int s = 0; s < inventoryHandler.getSlots(); s++) {
                if (s == slot) {
                    continue;
                }
                if (stackMatchesFilterStack(inventoryHandler.getStackInSlot(s), stack)) {
                    return ItemHelpers.EMPTY;
                }
            }
            return stack;
        }

        return !shouldVoidOverflow && filterLogic.matchesFilter(stack) ? ItemHelpers.EMPTY : stack;
    }

    @Override
    public void onAfterInsert(IItemHandlerSimpleInserter inventoryHandler, int slot) {
        // noop
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    public void setShouldWorkInGUI(boolean shouldWorkdInGUI) {
        NBTHelpers.setBoolean(upgrade, "shouldWorkInGUI", shouldWorkdInGUI);
        save();
    }

    public boolean shouldWorkInGUI() {
        return NBTHelpers.getBoolean(upgrade, "shouldWorkInGUI")
            .orElse(false);
    }

    public void setShouldVoidOverflow(boolean shouldVoidOverflow) {
        if (!shouldVoidOverflow && !upgradeItem.isVoidAnythingEnabled()) {
            return;
        }

        this.shouldVoidOverflow = shouldVoidOverflow;
        NBTHelpers.setBoolean(upgrade, "shouldVoidOverflow", shouldVoidOverflow);
        save();
    }

    public void setShouldVoidOverflowDefaultOrLoadFromNbt(boolean shouldVoidOverflowDefault) {
        shouldVoidOverflow = !upgradeItem.isVoidAnythingEnabled()
            || NBTHelpers.getBoolean(upgrade, "shouldVoidOverflow")
                .orElse(shouldVoidOverflowDefault);
    }

    public boolean shouldVoidOverflow() {
        return !upgradeItem.isVoidAnythingEnabled() || shouldVoidOverflow;
    }

    @Override
    public void onSlotChange(IItemHandler inventoryHandler, int slot) {
        if (!shouldWorkInGUI() || shouldVoidOverflow()) {
            return;
        }

        ItemStack slotStack = inventoryHandler.getStackInSlot(slot);
        if (filterLogic.matchesFilter(slotStack)) {
            slotsToVoid.add(slot);
        }
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (slotsToVoid.isEmpty()) {
            return;
        }

        InventoryHandler storageInventory = storageWrapper.getInventoryHandler();
        for (int slot : slotsToVoid) {
            storageInventory.extractItem(slot, ItemHelpers.getCount(storageInventory.getStackInSlot(slot)), false);
        }

        slotsToVoid.clear();
    }

    @Override
    public boolean worksInGui() {
        return shouldWorkInGUI();
    }

    @Override
    public ItemStack onOverflow(ItemStack stack) {
        return filterLogic.matchesFilter(stack) ? ItemHelpers.EMPTY : stack;
    }

    @Override
    public boolean stackMatchesFilter(ItemStack stack) {
        return filterLogic.matchesFilter(stack);
    }

    public boolean isVoidAnythingEnabled() {
        return upgradeItem.isVoidAnythingEnabled();
    }

    @Override
    public int getSlotLimit() {
        return Integer.MAX_VALUE;
    }
}
