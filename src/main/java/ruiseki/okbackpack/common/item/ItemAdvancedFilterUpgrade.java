package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.wrapper.AdvancedFilterUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvancedFilterUpgrade extends ItemUpgrade<AdvancedFilterUpgradeWrapper> {

    public ItemAdvancedFilterUpgrade() {
        super("advanced_filter_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "advanced_filter_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.advanced_filter_upgrade"));
    }

    @Override
    public AdvancedFilterUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new AdvancedFilterUpgradeWrapper(stack, storage);
    }
}
