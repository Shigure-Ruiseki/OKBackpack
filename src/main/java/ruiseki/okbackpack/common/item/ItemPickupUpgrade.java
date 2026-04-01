package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.BasicExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.item.wrapper.PickupUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemPickupUpgrade extends ItemUpgrade<PickupUpgradeWrapper> {

    public ItemPickupUpgrade() {
        super("pickup_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "pickup_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.pickup_upgrade"));
    }

    @Override
    public PickupUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new PickupUpgradeWrapper(stack, storage);
    }

    @Override
    public void updateWidgetDelegates(PickupUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("common_filter_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getFilterItems);
        handler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, PickupUpgradeWrapper wrapper, ItemStack stack,
        BackpackPanel panel, String titleKey) {
        return new BasicExpandedTabWidget<>(slotIndex, wrapper, stack, titleKey);
    }
}
