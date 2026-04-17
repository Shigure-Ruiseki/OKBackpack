package ruiseki.okbackpack.common.item.travelers.spider;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;

public class SpiderUpgradeWrapper extends UpgradeWrapperBase implements ITickable, ITravelersUpgrade {

    public SpiderUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.isCollidedHorizontally && !player.isOnLadder()) {
            player.motionY = Math.max(player.motionY, player.isSneaking() ? 0.0D : 0.2D);
            player.fallDistance = 0.0F;
            player.onGround = false;
            player.isAirBorne = true;
        }
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return false;
    }
}
