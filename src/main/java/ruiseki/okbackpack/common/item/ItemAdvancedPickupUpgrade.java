package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.wrapper.AdvancedPickupUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvancedPickupUpgrade extends ItemUpgrade<AdvancedPickupUpgradeWrapper> {

    public ItemAdvancedPickupUpgrade() {
        super("advanced_pickup_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "advanced_pickup_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.advanced_pickup_upgrade"));
    }

    @Override
    public AdvancedPickupUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage) {
        return new AdvancedPickupUpgradeWrapper(stack, storage);
    }
}
