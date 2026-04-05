package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.JukeboxUpgradeWidget;
import ruiseki.okbackpack.common.item.wrapper.JukeboxUpgradeWrapper;
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
    public JukeboxUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new JukeboxUpgradeWrapper(stack, storage);
    }

    @Override
    public void updateWidgetDelegates(JukeboxUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH handler = group.get("jukebox_handler");
        if (handler == null) return;
        handler.setDelegatedStackHandler(wrapper::getStorage);
        handler.syncToServer(DelegatedStackHandlerSH.UPDATE_STORAGE);
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, JukeboxUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new JukeboxUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
