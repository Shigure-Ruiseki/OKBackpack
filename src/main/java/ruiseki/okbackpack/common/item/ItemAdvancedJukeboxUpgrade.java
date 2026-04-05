package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.AdvancedJukeboxUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.item.wrapper.AdvancedJukeboxUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvancedJukeboxUpgrade extends ItemUpgrade<AdvancedJukeboxUpgradeWrapper> {

    public ItemAdvancedJukeboxUpgrade() {
        super("advanced_jukebox_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "advanced_jukebox_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.advanced_jukebox_upgrade"));
        list.add(LangHelpers.localize("tooltip.backpack.advanced_jukebox_upgrade.1"));
    }

    @Override
    public AdvancedJukeboxUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new AdvancedJukeboxUpgradeWrapper(stack, storage);
    }

    @Override
    public void updateWidgetDelegates(AdvancedJukeboxUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("adv_jukebox_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getStorage);
        handler.syncToServer(DelegatedStackHandlerSH.UPDATE_STORAGE);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, AdvancedJukeboxUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new AdvancedJukeboxUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
