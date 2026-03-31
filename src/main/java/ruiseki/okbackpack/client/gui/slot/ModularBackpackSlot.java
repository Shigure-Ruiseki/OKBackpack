package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.common.block.BackpackWrapper;

public class ModularBackpackSlot extends ModularSlot {

    protected final BackpackWrapper wrapper;

    public ModularBackpackSlot(BackpackWrapper wrapper, int index) {
        super(wrapper.backpackHandler, index);
        this.wrapper = wrapper;
    }

    public ItemStack getMemoryStack() {
        return wrapper.getMemorizedStack(getSlotIndex());
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        int multiplier = wrapper.getTotalStackMultiplier();
        return stack.getMaxStackSize() * multiplier;
    }

    @Override
    public int getSlotStackLimit() {
        int multiplier = wrapper.getTotalStackMultiplier();
        return 64 * multiplier;
    }
}
