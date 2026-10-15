package ruiseki.storagecore.upgrades;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.storagecore.api.IStorageWrapper;

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
