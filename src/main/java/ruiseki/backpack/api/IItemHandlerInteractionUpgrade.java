package ruiseki.backpack.api;

import net.minecraft.entity.player.EntityPlayer;

import ruiseki.okcore.item.handler.IItemHandler;

public interface IItemHandlerInteractionUpgrade {

    void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player);
}
