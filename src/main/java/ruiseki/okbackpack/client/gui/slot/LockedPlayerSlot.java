package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okstoragecore.inventory.PlayerInvWrapperExtended;

public class LockedPlayerSlot extends ModularSlot {

    public LockedPlayerSlot(InventoryPlayer inventory, int index) {
        super(new PlayerInvWrapperExtended(inventory), index);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }
}
