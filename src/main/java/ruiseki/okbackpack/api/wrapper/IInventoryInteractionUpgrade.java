package ruiseki.okbackpack.api.wrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Triggered when a player sneak-right-clicks a container block while holding a backpack.
 */
public interface IInventoryInteractionUpgrade {

    /**
     * Interact with the target container.
     *
     * @param inventory the target container
     * @param player    the player performing the interaction
     * @param side      the face of the container that was clicked
     * @return true if at least one item was transferred
     */
    boolean onInteract(IInventory inventory, EntityPlayer player, ForgeDirection side);
}
