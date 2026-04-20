package ruiseki.okbackpack.common.item.travelers.chicken;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class ChickenUpgradeWrapper extends UpgradeWrapperBase implements ITickable, ITravelersUpgrade {

    private static final String NEXT_EGG_TICK_TAG = "NextEggTick";
    private static final int MIN_INTERVAL = 20 * 120;
    private static final int MAX_INTERVAL = 20 * 300;

    public ChickenUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        return tryGenerateEgg(player.worldObj);
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return tryGenerateEgg(world);
    }

    private boolean tryGenerateEgg(World world) {
        long worldTime = world.getTotalWorldTime();
        long nextEggTick = ItemNBTHelpers.getLong(upgrade, NEXT_EGG_TICK_TAG, 0L);

        if (nextEggTick <= 0L) {
            scheduleNextEgg(world, worldTime);
            return true;
        }

        if (worldTime < nextEggTick) {
            return false;
        }

        storage.insertItem(new ItemStack(Items.egg), false);
        scheduleNextEgg(world, worldTime);
        return true;
    }

    private void scheduleNextEgg(World world, long worldTime) {
        int interval = MathHelper.getRandomIntegerInRange(world.rand, MIN_INTERVAL, MAX_INTERVAL);
        ItemNBTHelpers.setLong(upgrade, NEXT_EGG_TICK_TAG, worldTime + interval);
        save();
    }
}
