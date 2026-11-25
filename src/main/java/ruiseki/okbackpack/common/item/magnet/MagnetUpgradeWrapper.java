package ruiseki.okbackpack.common.item.magnet;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IMagnetUpgrade;
import ruiseki.okbackpack.common.item.pickup.PickupUpgradeWrapper;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class MagnetUpgradeWrapper extends PickupUpgradeWrapper implements IMagnetUpgrade {

    public MagnetUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.magnet_settings";
    }

    @Override
    public boolean isCollectItem() {
        return ItemNBTHelpers.getBoolean(upgrade, MAG_ITEM_TAG, true);
    }

    @Override
    public void setCollectItem(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, MAG_ITEM_TAG, enabled);
        save();
    }

    @Override
    public boolean isCollectExp() {
        return ItemNBTHelpers.getBoolean(upgrade, MAG_EXP_TAG, true);
    }

    @Override
    public void setCollectExp(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, MAG_EXP_TAG, enabled);
        save();
    }

    @Override
    public boolean canCollectItem(ItemStack stack) {
        return checkFilter(stack);
    }

    @Override
    public boolean canPickup(ItemStack stack) {
        return isCollectItem() && canCollectItem(stack);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        return MagnetUpgradeHelpers.tickPlayer(this, player);
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return MagnetUpgradeHelpers.tickStorage(this, storage, world, pos);
    }
}
