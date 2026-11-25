package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;

public interface IUpgradeWrapperFactory<W extends IUpgradeWrapper> {

    W createWrapper(ItemStack stack, IStorageWrapper storage);

    void updateWidgetDelegates(W wrapper, UpgradeSlotUpdateGroup group);

    ExpandedTabWidget getExpandedTabWidget(int slotIndex, W wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey);

}
