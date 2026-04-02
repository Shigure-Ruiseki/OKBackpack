package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IVoidUpgrade;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedVoidUpgradeWrapper extends AdvancedUpgradeWrapper implements IVoidUpgrade {

    public AdvancedVoidUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_void_settings";
    }

    @Override
    public boolean canVoid(ItemStack stack) {
        return checkFilter(stack);
    }

    @Override
    public VoidType getVoidType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, VOID_TYPE_TAG, VoidType.OVERFLOW.ordinal());
        VoidType[] types = VoidType.values();
        if (ordinal < 0 || ordinal >= types.length) return VoidType.OVERFLOW;
        return types[ordinal];
    }

    @Override
    public void setVoidType(VoidType type) {
        if (type == null) type = VoidType.OVERFLOW;
        ItemNBTHelpers.setInt(upgrade, VOID_TYPE_TAG, type.ordinal());
        markDirty();
    }

    @Override
    public VoidInput getVoidInput() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, VOID_INPUT_TAG, VoidInput.ALL.ordinal());
        VoidInput[] types = VoidInput.values();
        if (ordinal < 0 || ordinal >= types.length) return VoidInput.ALL;
        return types[ordinal];
    }

    @Override
    public void setVoidInput(VoidInput type) {
        if (type == null) type = VoidInput.ALL;
        ItemNBTHelpers.setInt(upgrade, VOID_INPUT_TAG, type.ordinal());
        markDirty();
    }

    @Override
    public ItemStack onInsert(int slot, ItemStack stack, boolean simulate) {
        if (stack == null) return null;

        // Step 0: filter
        if (!canVoid(stack)) {
            return stack;
        }

        VoidType type = getVoidType();

        // VOID ALL
        if (type == VoidType.ANY) {
            return simulate ? stack : null;
        }

        // OVERFLOW LOGIC (FIXED)
        if (type == VoidType.OVERFLOW) {

            int remaining = stack.stackSize;

            // 1. Try to fill existing stacks
            for (int i = 0; i < storage.getSlots(); i++) {
                ItemStack existing = storage.getStackInSlot(i);
                if (existing == null) continue;

                if (!existing.isItemEqual(stack)) continue;

                int limit = storage.getSlotLimit(i);
                int space = limit - existing.stackSize;

                if (space <= 0) continue;

                int toInsert = Math.min(space, remaining);

                if (!simulate) {
                    existing.stackSize += toInsert;
                    storage.setStackInSlot(i, existing);
                }

                remaining -= toInsert;

                if (remaining <= 0) {
                    return null; // fully handled
                }
            }

            // 2. If ANY matching slot existed → remaining gets voided
            for (int i = 0; i < storage.getSlots(); i++) {
                ItemStack existing = storage.getStackInSlot(i);
                if (existing != null && existing.isItemEqual(stack)) {
                    // we found at least one matching slot earlier → void rest
                    return simulate ? stack : null;
                }
            }

            // 3. No matching stack → allow normal insertion
            return stack;
        }

        return stack;
    }

    @Override
    public ItemStack onSet(int slot, @Nullable ItemStack stack) {
        if (stack == null) return null;

        // Step 0: filter
        if (!canVoid(stack)) {
            return stack;
        }

        VoidType type = getVoidType();

        // VOID ALL
        if (type == VoidType.ANY) {
            return null;
        }

        // OVERFLOW LOGIC (FIXED)
        if (type == VoidType.OVERFLOW) {

            int remaining = stack.stackSize;
            boolean foundMatch = false;

            // 1. Try to fill existing stacks
            for (int i = 0; i < storage.getSlots(); i++) {
                if (i == slot) continue;

                ItemStack existing = storage.getStackInSlot(i);
                if (existing == null) continue;

                if (!existing.isItemEqual(stack)) continue;

                foundMatch = true;

                int limit = storage.getSlotLimit(i);
                int space = limit - existing.stackSize;

                if (space <= 0) continue;

                int toInsert = Math.min(space, remaining);

                existing.stackSize += toInsert;
                storage.setStackInSlot(i, existing);

                remaining -= toInsert;

                if (remaining <= 0) {
                    return null; // fully distributed
                }
            }

            if (foundMatch) {
                return null;
            }

            return stack;
        }

        return stack;
    }
}
