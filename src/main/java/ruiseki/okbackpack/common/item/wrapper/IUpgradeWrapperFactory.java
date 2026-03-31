package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;

public interface IUpgradeWrapperFactory<W extends UpgradeWrapper> {

    W createWrapper(ItemStack stack, IStorageWrapper storage);
}
