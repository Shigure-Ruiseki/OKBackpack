package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okcore.item.IItemHandler;

public class LockedPlayerSlot extends ModularSlot {

    public LockedPlayerSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }
}
