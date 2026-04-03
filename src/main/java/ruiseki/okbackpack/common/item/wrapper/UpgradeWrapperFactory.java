package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapperFactory;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;

public class UpgradeWrapperFactory {

    @SuppressWarnings("unchecked")
    public static <W extends UpgradeWrapperBase> W createWrapper(ItemStack stack, IStorageWrapper storage) {
        if (stack == null || stack.getItem() == null) return null;
        if (!(stack.getItem() instanceof IUpgradeWrapperFactory<?>factory)) return null;
        return (W) factory.createWrapper(stack, storage);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void updateWidgetDelegates(ItemStack stack, UpgradeWrapperBase wrapper,
        UpgradeSlotUpdateGroup group) {
        if (stack == null || stack.getItem() == null || wrapper == null) return;
        if (!(stack.getItem() instanceof IUpgradeWrapperFactory factory)) return;
        factory.updateWidgetDelegates(wrapper, group);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ExpandedTabWidget getExpandedTabWidget(ItemStack stack, int slotIndex, UpgradeWrapperBase wrapper,
        BackpackPanel panel, String titleKey) {
        if (stack == null || stack.getItem() == null || wrapper == null) return null;
        if (!(stack.getItem() instanceof IUpgradeWrapperFactory factory)) return null;
        return factory.getExpandedTabWidget(slotIndex, wrapper, stack, panel, titleKey);
    }

}
