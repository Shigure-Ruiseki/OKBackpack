package ruiseki.okbackpack.common.item;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okbackpack.OKBCreativeTab;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.ItemOK;

public class ItemUpgrade<T extends IUpgradeWrapper> extends ItemOK implements IUpgradeItem<T> {

    public ItemUpgrade(String name) {
        super(name);
        setNoRepair();
        setTextureName(Reference.PREFIX_MOD + "upgrade_base");
        this.setCreativeTab(OKBCreativeTab.INSTANCE);
    }

    public ItemUpgrade() {
        this("upgrade_base");
    }

    public boolean hasTab() {
        return false;
    }

    @Override
    public boolean hasSlotWidget() {
        return false;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.upgrade_base"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public T createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        return (T) new UpgradeWrapperBase(stack, storage, upgradeConsumer);
    }

    @Override
    public void updateWidgetDelegates(T wrapper, UpgradeSlotUpdateGroup group) {

    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, T wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        return null;
    }

    @Override
    public void updateSlotWidgetDelegates(T wrapper, UpgradeSlotUpdateGroup group) {

    }

    @Override
    public Widget<?> getSlotWidget(int slotIndex, T wrapper, ItemStack stack, IStoragePanel<?> panel, String titleKey) {
        return null;
    }
}
