package ruiseki.okbackpack.api.upgrade;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;

public class UpgradeType<T extends IUpgradeWrapper> {

    private final IFactory<T> factory;

    public UpgradeType(IFactory<T> factory) {
        this.factory = factory;
    }

    public T create(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        return factory.create(storageWrapper, upgrade, upgradeSaveHandler);
    }

    public interface IFactory<T extends IUpgradeWrapper> {

        T create(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler);
    }
}
