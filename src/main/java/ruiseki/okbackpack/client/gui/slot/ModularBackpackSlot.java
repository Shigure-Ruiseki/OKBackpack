package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.api.IStorageWrapper;

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
    public int getItemStackLimit(ItemStack stack) {
        return stack.getMaxStackSize() * wrapper.applyStackLimitModifiers();
    }

    @Override
    public int getSlotStackLimit() {
        return 64 * wrapper.applySlotLimitModifiers();
    }
}
