package ruiseki.okbackpack.common.item.jukebox;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.JukeboxUpgradeWidget;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemJukeboxUpgrade extends ItemUpgrade<JukeboxUpgradeWrapper> {

    public ItemJukeboxUpgrade() {
        super("jukebox_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "jukebox_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.jukebox_upgrade"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem
            .findConflictSlots(wrapper, targetSlot, ItemJukeboxUpgrade.class, ItemAdvancedJukeboxUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult.fail(
                "gui.backpack.error.add.only_single_upgrade_allowed",
                conflicts,
                LangHelpers.localize("item.jukebox_upgrade.name"),
                wrapper.getDisplayName());
        }
        return UpgradeSlotChangeResult.success();
    }

    @Override
    public JukeboxUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new JukeboxUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    @Override
    public void updateWidgetDelegates(JukeboxUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("jukebox_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getStorage);
        handler.syncToServer(DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_STORAGE));
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, JukeboxUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new JukeboxUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
