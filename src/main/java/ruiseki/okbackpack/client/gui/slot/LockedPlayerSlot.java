package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.item.handler.IItemHandler;

public class LockedPlayerSlot extends ModularItemSlot {

    public LockedPlayerSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }
}
