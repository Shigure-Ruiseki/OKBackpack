package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.IItemHandler;

public interface IFeedingUpgrade {

    ItemStack getFeedingStack(IItemHandler handler, int foodLevel, float health, float maxHealth);
}
