package ruiseki.okstoragecore.upgrades;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.ExpandedTab;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class UpgradeType<T extends IUpgradeWrapper> {

    private final IFactory<T> factory;

    public UpgradeType(IFactory<T> factory) {
        this.factory = factory;
    }

    public T create(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        return factory.create(storageWrapper, upgrade, upgradeSaveHandler);
    }

    public void updateWidgetDelegates(T wrapper, UpgradeSlotUpdate group) {
        factory.updateTabDelegates(wrapper, group);
    }

    public ExpandedTab createExpandedTab(int slotIndex, T wrapper, ItemStack stack, StoragePanel storagePanel) {
        return factory.createExpandedTab(slotIndex, wrapper, stack, storagePanel);
    }

    public Widget<?> createSlotWidget(int slotIndex, T wrapper, ItemStack stack, StoragePanel storagePanel) {
        return factory.createSlotWidget(slotIndex, wrapper, stack, storagePanel);
    }

    public interface IFactory<T extends IUpgradeWrapper> {

        T create(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler);

        default void updateTabDelegates(T wrapper, UpgradeSlotUpdate group) {

        }

        default ExpandedTab createExpandedTab(int slotIndex, T wrapper, ItemStack stack, StoragePanel storagePanel) {
            return null;
        }

        default Widget<?> createSlotWidget(int slotIndex, T wrapper, ItemStack stack, StoragePanel storagePanel) {
            return null;
        }
    }
}
