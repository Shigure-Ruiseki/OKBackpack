package ruiseki.okbackpack.common.item.refill;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.ClientProxy;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.AdvancedRefillUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvancedRefillUpgrade extends ItemUpgrade<AdvancedRefillUpgradeWrapper> {

    public ItemAdvancedRefillUpgrade() {
        super("advanced_refill_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "advanced_refill_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.advanced_refill_upgrade"));
        list.add(LangHelpers.localize("tooltip.backpack.advanced_refill_upgrade.1"));
        int keyCode = ClientProxy.keyBackpackPickBlock.getKeyCode();
        String keyName = keyCode == 0 ? "null" : GameSettings.getKeyDisplayString(keyCode);
        list.add(LangHelpers.localize("tooltip.backpack.advanced_refill_upgrade.2", keyName));
    }

    @Override
    public AdvancedRefillUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> consumer) {
        return new AdvancedRefillUpgradeWrapper(stack, storage, consumer);
    }

    @Override
    public void updateWidgetDelegates(AdvancedRefillUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("adv_refill_filter_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getFilterItems);
        handler.syncToServer(DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_FILTERABLE));
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, AdvancedRefillUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new AdvancedRefillUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
