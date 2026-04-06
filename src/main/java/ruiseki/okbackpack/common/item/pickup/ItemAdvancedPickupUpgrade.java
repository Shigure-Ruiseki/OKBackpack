package ruiseki.okbackpack.common.item.pickup;

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
import ruiseki.okbackpack.client.gui.widget.upgrade.AdvancedExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvancedPickupUpgrade extends ItemUpgrade<AdvancedPickupUpgradeWrapper> {

    public ItemAdvancedPickupUpgrade() {
        super("advanced_pickup_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "advanced_pickup_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.advanced_pickup_upgrade"));
    }

    @Override
    public AdvancedPickupUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new AdvancedPickupUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    @Override
    public void updateWidgetDelegates(AdvancedPickupUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("adv_common_filter_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getFilterItems);
        handler.syncToServer(DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_FILTERABLE));

        DelegatedStackHandlerSH oreDictHandler = group.get("ore_dict_handler");
        if (oreDictHandler == null) return;
        oreDictHandler.setDelegatedStackHandler(wrapper::getOreDictItem);
        oreDictHandler.syncToServer(DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_ORE_DICT));
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, AdvancedPickupUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new AdvancedExpandedTabWidget<>(slotIndex, wrapper, stack, titleKey);
    }
}
