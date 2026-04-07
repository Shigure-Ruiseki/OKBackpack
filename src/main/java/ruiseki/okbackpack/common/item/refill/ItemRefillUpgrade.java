package ruiseki.okbackpack.common.item.refill;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.RefillUpgradeWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemRefillUpgrade extends ItemUpgrade<RefillUpgradeWrapper> {

    public ItemRefillUpgrade() {
        super("refill_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "refill_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.refill_upgrade"));
    }

    @Override
    public RefillUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new RefillUpgradeWrapper(stack, storage, consumer);
    }

    @Override
    public void updateWidgetDelegates(RefillUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("refill_filter_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getFilterItems);
        handler.syncToServer(DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_FILTERABLE));
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, RefillUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new RefillUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
