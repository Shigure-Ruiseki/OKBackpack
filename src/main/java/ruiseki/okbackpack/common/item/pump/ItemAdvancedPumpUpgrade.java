package ruiseki.okbackpack.common.item.pump;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.AdvancedPumpUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvancedPumpUpgrade extends ItemUpgrade<AdvancedPumpUpgradeWrapper> {

    public ItemAdvancedPumpUpgrade() {
        super("advanced_pump_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "advanced_pump_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public boolean hasSlotWidget() {
        return false;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.advanced_pump_upgrade"));
        list.add(LangHelpers.localize("tooltip.backpack.advanced_pump_upgrade.1"));
        list.add(LangHelpers.localize("tooltip.backpack.advanced_pump_upgrade.2"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public AdvancedPumpUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new AdvancedPumpUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, AdvancedPumpUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new AdvancedPumpUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }

    @Override
    public Widget<?> getSlotWidget(int slotIndex, AdvancedPumpUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return null;
    }

    @Override
    public void updateWidgetDelegates(AdvancedPumpUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        // FluidFilterTank looks up the upgrade wrapper dynamically via the storage wrapper, so
        // no rebinding is needed here.
    }

    @Override
    public void updateSlotWidgetDelegates(AdvancedPumpUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {

    }
}
