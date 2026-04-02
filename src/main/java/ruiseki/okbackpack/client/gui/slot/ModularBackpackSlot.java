package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.common.block.BackpackWrapper;

public class ModularBackpackSlot extends ModularSlot {

    protected final BackpackWrapper wrapper;

    public ModularBackpackSlot(BackpackWrapper wrapper, int index) {
        super(wrapper, index);
        this.wrapper = wrapper;
    }

    public ItemStack getMemoryStack() {
        return wrapper.getMemorizedStack(getSlotIndex());
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return stack.getMaxStackSize() * wrapper.applyStackLimitModifiers(1, getSlotIndex(), stack);
    }

    @Override
    public int getSlotStackLimit() {
        return 64 * wrapper.applySlotLimitModifiers(1, getSlotIndex());
    }
}
