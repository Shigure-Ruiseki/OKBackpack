package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.AdvancedSmeltingUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.item.wrapper.AutoBlastingUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAutoBlastingUpgrade extends ItemUpgrade<AutoBlastingUpgradeWrapper> {

    public ItemAutoBlastingUpgrade() {
        super("auto_blasting_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "auto_blasting_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.auto_blasting_upgrade"));
    }

    @Override
    public AutoBlastingUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new AutoBlastingUpgradeWrapper(stack, storage);
    }

    @Override
    public void updateWidgetDelegates(AutoBlastingUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("smelting_filter_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getFilterItems);
        handler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE);

        DelegatedStackHandlerSH oreDictHandler = group.get("smelting_ore_dict_handler");
        if (oreDictHandler == null) return;
        oreDictHandler.setDelegatedStackHandler(wrapper::getOreDictItem);
        oreDictHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_ORE_DICT);

        DelegatedStackHandlerSH smeltingHandler = group.get("smelting_inv_handler");
        if (smeltingHandler == null) return;
        smeltingHandler.setDelegatedStackHandler(wrapper::getSmeltingInventory);
        smeltingHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_SMELTING);

        DelegatedStackHandlerSH fuelFilterHandler = group.get("fuel_filter_handler");
        if (fuelFilterHandler == null) return;
        fuelFilterHandler.setDelegatedStackHandler(wrapper::getFuelFilterItems);
        fuelFilterHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_FUEL_FILTER);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, AutoBlastingUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new AdvancedSmeltingUpgradeWidget<>(slotIndex, wrapper, stack, panel, titleKey);
    }
}
