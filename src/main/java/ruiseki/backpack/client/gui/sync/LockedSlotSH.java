package ruiseki.backpack.client.gui.sync;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;

public class LockedSlotSH extends ItemSlotSH {

    private final IBackpackWrapper wrapper;
    private final BackpackPanel panel;

    public LockedSlotSH(ModularSlot slot, IBackpackWrapper wrapper, BackpackPanel panel) {
        super(slot);
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    protected void onSlotUpdate(ItemStack stack, boolean onlyAmountChanged, boolean client, boolean init) {
        super.onSlotUpdate(stack, onlyAmountChanged, client, init);
    }

    public IBackpackWrapper getWrapper() {
        return wrapper;
    }

    public BackpackPanel getPanel() {
        return panel;
    }
}
