package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.wrapper.InceptionUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemInceptionUpgrade extends ItemUpgrade<InceptionUpgradeWrapper> {

    public ItemInceptionUpgrade() {
        super("inception_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "inception_upgrade");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.inception_upgrade"));
    }

    @Override
    public InceptionUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new InceptionUpgradeWrapper(stack, storage);
    }
}
