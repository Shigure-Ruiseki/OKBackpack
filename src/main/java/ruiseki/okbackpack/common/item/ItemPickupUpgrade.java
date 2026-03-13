package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.common.item.wrapper.PickupUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class ItemPickupUpgrade extends ItemUpgrade<PickupUpgradeWrapper> {

    public ItemPickupUpgrade() {
        super("pickup_upgrade");
        setMaxStackSize(1);
        setTextureName("pickup_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.pickup_upgrade"));
    }

    @Override
    public PickupUpgradeWrapper createWrapper(ItemStack stack) {
        return new PickupUpgradeWrapper(stack);
    }
}
