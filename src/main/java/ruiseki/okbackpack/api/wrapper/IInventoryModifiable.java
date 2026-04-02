package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * Interface for upgrades that can modify inventory behavior.
 * <p>
 * Implementations can override these hooks to customize how items are inserted, extracted,
 * retrieved, or set in a modifiable inventory.
 */
public interface IInventoryModifiable {

    /**
     * Called when inserting an item into a slot.
     *
     * @param slot     the index of the slot
     * @param stack    the ItemStack to insert
     * @param simulate if true, the operation should be simulated without modifying the inventory
     * @return the remaining ItemStack that could not be inserted, or {@code null} if fully inserted
     */
    @Nullable
    default ItemStack onInsert(int slot, @Nullable ItemStack stack, boolean simulate) {
        return stack; // default returns the original stack unchanged
    }

    /**
     * Called when extracting items from a slot.
     *
     * @param slot      the index of the slot
     * @param extracted the ItemStack that was extracted
     * @param simulate  if true, the operation should be simulated without modifying the inventory
     * @return the ItemStack after modifications by the upgrade, or {@code null} if cancelled
     */
    @Nullable
    default ItemStack onExtract(int slot, @Nullable ItemStack extracted, boolean simulate) {
        return extracted; // default returns the stack unchanged
    }

    /**
     * Called when retrieving the ItemStack from a slot.
     *
     * @param slot         the index of the slot
     * @param currentStack the current ItemStack in the slot
     * @return the ItemStack after modification by the upgrade, or the original stack by default
     */
    @Nullable
    default ItemStack onGet(int slot, @Nullable ItemStack currentStack) {
        return currentStack; // default returns the stack unchanged
    }

    /**
     * Called when setting an ItemStack into a slot.
     *
     * @param slot  the index of the slot
     * @param stack the ItemStack to set
     */
    default ItemStack onSet(int slot, @Nullable ItemStack stack) {
        return stack; // default does nothing
    }
}
