package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.FilterUpgradeWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.item.wrapper.FilterUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemFilterUpgrade extends ItemUpgrade<FilterUpgradeWrapper> {

    public ItemFilterUpgrade() {
        super("filter_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "filter_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.filter_upgrade"));
    }

    @Override
    public FilterUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new FilterUpgradeWrapper(stack, storage);
    }

    @Override
    public void updateWidgetDelegates(FilterUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("common_filter_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getFilterItems);
        handler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, FilterUpgradeWrapper wrapper, ItemStack stack,
        BackpackPanel panel, String titleKey) {
        return new FilterUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
