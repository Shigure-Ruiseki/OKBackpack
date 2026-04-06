package ruiseki.okbackpack.common.item.smelter;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedFloatSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.SmeltingUpgradeWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
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
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem.findConflictSlots(
            wrapper,
            targetSlot,
            ItemSmeltingUpgrade.class,
            ItemAutoSmeltingUpgrade.class,
            ItemSmokingUpgrade.class,
            ItemAutoSmokingUpgrade.class,
            ItemBlastingUpgrade.class,
            ItemAutoBlastingUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult.fail(
                "gui.backpack.error.add.only_single_upgrade_allowed",
                conflicts,
                LangHelpers.localize("item.smelting_upgrade.name"),
                wrapper.getDisplayName());
        }
        return UpgradeSlotChangeResult.success();
    }

    @Override
    public SmeltingUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new SmeltingUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    @Override
    public void updateWidgetDelegates(SmeltingUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH smeltingHandler = group.get("smelting_inv_handler");
        if (smeltingHandler == null) return;
        smeltingHandler.setDelegatedStackHandler(wrapper::getStorage);
        smeltingHandler.syncToServer(DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_STORAGE));

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
