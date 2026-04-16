package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okcore.helper.ItemHandlerHelpers;

public class ModularBackpackSlot extends ModularSlot {

    protected final IStorageWrapper wrapper;

    public ModularBackpackSlot(IStorageWrapper wrapper, int index) {
        super(wrapper, index);
        this.wrapper = wrapper;
    }

    public ItemStack getMemoryStack() {
        return wrapper.getMemoryStack(getSlotIndex());
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ItemStack current = getStack();
        if (current != null) {
            ItemStack cursor = player.inventory.getItemStack();
            if (cursor != null) {
                // Different item and oversized stack → block swap
                if (current.stackSize > current.getMaxStackSize()
                    && !ItemHandlerHelpers.canItemStacksStack(current, cursor)) {
                    return false;
                }
            }
        }
        return super.canTakeStack(player);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        ItemStack current = getStack();
        if (current != null && current.stackSize > current.getMaxStackSize()) {
            // Allow stacking same item, block placing different item
            if (!ItemHandlerHelpers.canItemStacksStack(current, stack)) {
                return false;
            }
        }
        return super.isItemValid(stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        double mod = wrapper.applyStackLimitModifiers();
        double raw = stack.getMaxStackSize() * mod;
        if (raw >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) Math.ceil(raw);
    }

    @Override
    public int getSlotStackLimit() {
        double mod = wrapper.applySlotLimitModifiers();
        double raw = 64.0 * mod;
        if (raw >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) Math.ceil(raw);
    }
}
