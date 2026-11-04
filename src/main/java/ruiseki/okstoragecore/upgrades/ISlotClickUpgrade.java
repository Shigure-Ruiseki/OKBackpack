package ruiseki.okstoragecore.upgrades;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface ISlotClickUpgrade {

    /**
     * Handles shift-click transfer for a slot owned by this upgrade.
     */
    @Nullable
    default ItemStack transferItem(EntityPlayer player, ItemStack stack) {
        return stack;
    }
}
