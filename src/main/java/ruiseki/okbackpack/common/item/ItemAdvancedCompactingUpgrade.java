package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.AdvancedCompactingUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.item.wrapper.AdvancedCompactingUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvancedCompactingUpgrade extends ItemUpgrade<AdvancedCompactingUpgradeWrapper> {

    public ItemAdvancedCompactingUpgrade() {
        super("advanced_compacting_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "advanced_compacting_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.advanced_compacting_upgrade"));
    }

    @Override
    public AdvancedCompactingUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new AdvancedCompactingUpgradeWrapper(stack, storage);
    }

    @Override
    public void updateWidgetDelegates(AdvancedCompactingUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("adv_common_filter_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getFilterItems);
        handler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE);

        DelegatedStackHandlerSH oreDictHandler = group.get("ore_dict_handler");
        if (oreDictHandler == null) return;
        oreDictHandler.setDelegatedStackHandler(wrapper::getOreDictItem);
        oreDictHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_ORE_DICT);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, AdvancedCompactingUpgradeWrapper wrapper,
        ItemStack stack, BackpackPanel panel, String titleKey) {
        return new AdvancedCompactingUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
