package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedFloatSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.SmeltingUpgradeWidget;
import ruiseki.okbackpack.common.item.wrapper.SmeltingUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemSmeltingUpgrade extends ItemUpgrade<SmeltingUpgradeWrapper> {

    public ItemSmeltingUpgrade() {
        super("smelting_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "smelting_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.smelting_upgrade"));
    }

    @Override
    public SmeltingUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new SmeltingUpgradeWrapper(stack, storage);
    }

    @Override
    public void updateWidgetDelegates(SmeltingUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH smeltingHandler = group.get("smelting_inv_handler");
        if (smeltingHandler == null) return;
        smeltingHandler.setDelegatedStackHandler(wrapper::getStorage);
        smeltingHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_STORAGE);

        DelegatedFloatSH progressHandler = group.get("smelting_progress_handler");
        if (progressHandler == null) return;
        progressHandler.setDelegatedSupplier(() -> wrapper::getProgress);
        progressHandler.syncToServer(DelegatedFloatSH.UPDATE_PROGRESS);

        DelegatedFloatSH fuelHandler = group.get("smelting_fuel_handler");
        if (fuelHandler == null) return;
        fuelHandler.setDelegatedSupplier(() -> wrapper::getBurnProgress);
        fuelHandler.syncToServer(DelegatedFloatSH.UPDATE_FUEL);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, SmeltingUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new SmeltingUpgradeWidget<>(slotIndex, wrapper, stack, panel, titleKey);
    }
}
