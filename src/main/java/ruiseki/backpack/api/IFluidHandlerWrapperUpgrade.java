package ruiseki.backpack.api;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okstoragecore.api.IStorageFluidHandler;

public interface IFluidHandlerWrapperUpgrade {

    @Nullable
    IStorageFluidHandler wrapHandler(@Nullable IStorageFluidHandler fluidHandler, ItemStack backpack);
}
