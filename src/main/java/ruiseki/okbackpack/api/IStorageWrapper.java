package ruiseki.okbackpack.api;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;

import ruiseki.okcore.persist.nbt.INBTSerializable;

public interface IStorageWrapper extends IItemHandlerModifiable, IItemHandler, ITintable, INBTSerializable {

    String getDisplayName();

    @Nullable
    ItemStack insertItem(@Nullable ItemStack stack, boolean simulate);

    @Nullable
    ItemStack extractItem(ItemStack wanted, int amount, boolean simulate);
}
