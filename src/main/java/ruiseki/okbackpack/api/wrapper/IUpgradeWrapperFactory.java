package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;

public interface IUpgradeWrapperFactory<W extends UpgradeWrapperBase> {

    W createWrapper(ItemStack stack, IStorageWrapper storage);
}
