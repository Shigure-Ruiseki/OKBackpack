package ruiseki.okbackpack.api.upgrade;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;

public interface IUpgradeItem<W extends IUpgradeWrapper> {

    W createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer);

    void updateWidgetDelegates(W wrapper, UpgradeSlotUpdateGroup group);

    ExpandedTabWidget getExpandedTabWidget(int slotIndex, W wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey);
}
