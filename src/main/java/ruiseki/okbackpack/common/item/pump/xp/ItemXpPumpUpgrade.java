package ruiseki.okbackpack.common.item.pump.xp;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.XpPumpUpgradeWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemXpPumpUpgrade extends ItemUpgrade<XpPumpUpgradeWrapper> {

    public ItemXpPumpUpgrade() {
        super();
        setMaxStackSize(1);
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
        list.add(LangHelpers.localize("tooltip.backpack.xp_pump_upgrade"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public XpPumpUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new XpPumpUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, XpPumpUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new XpPumpUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }

    @Override
    public Widget<?> getSlotWidget(int slotIndex, XpPumpUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        return null;
    }

    @Override
    public void updateWidgetDelegates(XpPumpUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {

    }

    @Override
    public void updateSlotWidgetDelegates(XpPumpUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {

    }
}
