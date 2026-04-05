package ruiseki.okbackpack.common.item.everlasting;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemEverlastingUpgrade extends ItemUpgrade<EverlastingUpgradeWrapper> {

    public ItemEverlastingUpgrade() {
        super("everlasting_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "everlasting_upgrade");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.everlasting_upgrade"));
    }

    @Override
    public EverlastingUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> consumer) {
        return new EverlastingUpgradeWrapper(stack, storage, consumer);
    }
}
