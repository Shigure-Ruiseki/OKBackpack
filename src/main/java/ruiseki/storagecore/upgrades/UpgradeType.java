package ruiseki.storagecore.upgrades;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.ExpandedTabWidget;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.client.gui.widget.update.UpgradeSlotUpdate;

public class UpgradeType<T extends IUpgradeWrapper> {

    private final IFactory<T> factory;

    public UpgradeType(IFactory<T> factory) {
        this.factory = factory;
    }

    public T create(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        return factory.create(storageWrapper, upgrade, upgradeSaveHandler);
    }

    public void updateWidgetDelegates(T wrapper, UpgradeSlotUpdate group) {
        factory.updateWidgetDelegates(wrapper, group);
    }

    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, T wrapper, ItemStack stack,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey) {
        return factory.getExpandedTabWidget(slotIndex, wrapper, stack, storageWrapper, storagePanel, titleKey);
    }

    public interface IFactory<T extends IUpgradeWrapper> {

        T create(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler);

        default void updateWidgetDelegates(T wrapper, UpgradeSlotUpdate group) {

        }

        default ExpandedTabWidget getExpandedTabWidget(int slotIndex, T wrapper, ItemStack stack,
            IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey) {
            return null;
        }
    }
}
