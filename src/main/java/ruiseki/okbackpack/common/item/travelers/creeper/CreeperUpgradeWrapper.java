package ruiseki.okbackpack.common.item.travelers.creeper;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class CreeperUpgradeWrapper extends UpgradeWrapperBase implements ITravelersUpgrade {

    private static final String NEXT_RESCUE_TICK_TAG = "NextRescueTick";
    public static final int COOLDOWN_TICKS = 20 * 60 * 15;

    public CreeperUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    public boolean isReady(long worldTime) {
        return worldTime >= ItemNBTHelpers.getLong(upgrade, NEXT_RESCUE_TICK_TAG, 0L);
    }

    public void trigger(long worldTime) {
        ItemNBTHelpers.setLong(upgrade, NEXT_RESCUE_TICK_TAG, worldTime + COOLDOWN_TICKS);
        save();
    }
}
