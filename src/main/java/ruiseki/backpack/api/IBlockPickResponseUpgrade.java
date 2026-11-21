package ruiseki.backpack.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBlockPickResponseUpgrade {

    boolean pickBlock(EntityPlayer player, ItemStack filter);
}
