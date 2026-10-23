package ruiseki.okstoragecore.upgrades;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

public interface ISlotClickUpgrade {

    /**
     * Handles shift-click transfer for a slot owned by this upgrade.
     *
     * @return true if the click has been handled; false to use default logic.
     */
    default boolean transferItem(EntityPlayer player, ModularSlot slot, ItemStack stack) {
        return false;
    };
}
