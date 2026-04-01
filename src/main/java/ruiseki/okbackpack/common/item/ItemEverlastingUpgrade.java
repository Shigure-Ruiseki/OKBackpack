package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;
import ruiseki.okcore.helper.LangHelpers;

public class ItemEverlastingUpgrade extends ItemUpgrade<UpgradeWrapperBase> {

    public ItemEverlastingUpgrade() {
        super("everlasting_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "everlasting_upgrade");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.everlasting_upgrade"));
    }
}
