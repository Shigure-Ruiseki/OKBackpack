package ruiseki.storagecore.upgrades;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IPickupResponseUpgrade {

    ItemStack pickup(World world, ItemStack stack, boolean simulate);
}
