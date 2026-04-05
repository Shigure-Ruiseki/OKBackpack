package ruiseki.okbackpack.common.item.inception;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.ItemUpgrade;
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
    public InceptionUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new InceptionUpgradeWrapper(stack, storage, upgradeConsumer);
    }
}
