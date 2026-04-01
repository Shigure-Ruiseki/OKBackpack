package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.wrapper.FeedingUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemFeedingUpgrade extends ItemUpgrade<FeedingUpgradeWrapper> {

    public ItemFeedingUpgrade() {
        super("feeding_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "feeding_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.feeding_upgrade"));
    }

    @Override
    public FeedingUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new FeedingUpgradeWrapper(stack, storage);
    }
}
