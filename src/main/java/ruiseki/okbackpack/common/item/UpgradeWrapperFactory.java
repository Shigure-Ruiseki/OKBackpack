package ruiseki.okbackpack.common.item;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;

public class UpgradeWrapperFactory {

    @SuppressWarnings("unchecked")
    public static <W extends IUpgradeWrapper> W createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        if (stack == null || stack.getItem() == null) return null;
        if (!(stack.getItem() instanceof IUpgradeItem<?>factory)) return null;
        return (W) factory.createWrapper(stack, storage, upgradeConsumer);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void updateWidgetDelegates(ItemStack stack, IUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        if (stack == null || stack.getItem() == null || wrapper == null) return;
        if (!(stack.getItem() instanceof IUpgradeItem factory)) return;
        factory.updateWidgetDelegates(wrapper, group);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ExpandedTabWidget getExpandedTabWidget(ItemStack stack, int slotIndex, IUpgradeWrapper wrapper,
        IStoragePanel<?> panel, String titleKey) {
        if (stack == null || stack.getItem() == null || wrapper == null) return null;
        if (!(stack.getItem() instanceof IUpgradeItem factory)) return null;
        return factory.getExpandedTabWidget(slotIndex, wrapper, stack, panel, titleKey);
    }

}
