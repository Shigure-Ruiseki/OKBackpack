package ruiseki.okstoragecore.upgrades;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.UpgradeContainer;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

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

    public void updateContainer(T wrapper, UpgradeSlotUpdate group) {
        factory.updateContainer(wrapper, group);
    }

    @NotNull
    public ExpandedUpgradeTab<T> createExpandedTab(int slotIndex, T wrapper, ItemStack stack,
        StoragePanel storagePanel) {
        return factory.createExpandedTab(slotIndex, wrapper, stack, storagePanel);
    }

    @NotNull
    public Widget<?> createSlotWidget(int slotIndex, T wrapper, ItemStack stack, StoragePanel storagePanel) {
        return factory.createSlotWidget(slotIndex, wrapper, stack, storagePanel);
    }

    public interface IFactory<T extends IUpgradeWrapper> {

        T create(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler);

        default void updateTabDelegates(T wrapper, UpgradeSlotUpdate group) {

        }

        default void updateContainer(T wrapper, UpgradeSlotUpdate group) {
            group.setUpgradeContainer(new UpgradeContainer(group));
        }

        @NotNull
        default ExpandedUpgradeTab<T> createExpandedTab(int slotIndex, T wrapper, ItemStack stack,
            StoragePanel storagePanel) {
            return new ExpandedUpgradeTab<>(slotIndex, wrapper, stack, storagePanel, "");
        }

        @NotNull
        default Widget<?> createSlotWidget(int slotIndex, T wrapper, ItemStack stack, StoragePanel storagePanel) {
            return new Widget<>();
        }
    }
}
