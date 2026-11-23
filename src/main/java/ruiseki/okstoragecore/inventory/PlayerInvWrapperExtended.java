package ruiseki.okstoragecore.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import com.cleanroommc.modularui.utils.item.PlayerInvWrapper;

public class PlayerInvWrapperExtended extends PlayerInvWrapper {

    public PlayerInvWrapperExtended(InventoryPlayer inv) {
        super(inv);
    }

    @Override
    public boolean isSlotFromInventory(int index, IInventory inventory, int invIndex) {
        int handlerIndex = getIndexForSlot(index);
        if (handlerIndex < 0) return false;
        int localSlot = getSlotFromIndex(index, handlerIndex);
        return getHandlerFromIndex(handlerIndex).isSlotFromInventory(localSlot, inventory, invIndex);
    }
}
