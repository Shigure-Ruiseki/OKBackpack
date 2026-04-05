package ruiseki.okbackpack.common.item.feeding;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.FeedingUpgradeWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemFeedingUpgrade extends ItemUpgrade<FeedingUpgradeWrapper> {

    public ItemFeedingUpgrade() {
        super("feeding_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "feeding_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.feeding_upgrade"));
    }

    @Override
    public FeedingUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new FeedingUpgradeWrapper(stack, storage, consumer);
    }

    @Override
    public void updateWidgetDelegates(FeedingUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("common_filter_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getFilterItems);
        handler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, FeedingUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new FeedingUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
