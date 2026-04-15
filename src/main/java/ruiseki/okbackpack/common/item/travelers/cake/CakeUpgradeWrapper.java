package ruiseki.okbackpack.common.item.travelers.cake;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class CakeUpgradeWrapper extends UpgradeWrapperBase implements ITickable, ITravelersUpgrade {

    private static final String LAST_TRIGGER_TICK_TAG = "LastCakeTick";
    private static final int INTERVAL_TICKS = 6000;

    public CakeUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        long now = player.worldObj.getTotalWorldTime();
        long last = ItemNBTHelpers.getLong(upgrade, LAST_TRIGGER_TICK_TAG, 0L);

        if (now - last < INTERVAL_TICKS) return false;

        player.getFoodStats()
            .addStats(3, 1.0f);
        player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0, true));

        ItemNBTHelpers.setLong(upgrade, LAST_TRIGGER_TICK_TAG, now);
        save();
        return true;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return false;
    }
}
