package ruiseki.okbackpack.common.item.cow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class CowUpgradeWrapper extends UpgradeWrapperBase implements ITickable {

    private static final String LAST_CLEAR_TICK_TAG = "LastClearTick";
    private static final int CLEAR_INTERVAL = 12000;

    public CowUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        long worldTime = player.worldObj.getTotalWorldTime();
        NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
        long lastClear = tag.getLong(LAST_CLEAR_TICK_TAG);

        if (lastClear == 0 || worldTime - lastClear >= CLEAR_INTERVAL) {
            Collection<PotionEffect> activeEffects = new ArrayList<>(player.getActivePotionEffects());
            boolean cleared = false;

            for (PotionEffect effect : activeEffects) {
                Potion potion = Potion.potionTypes[effect.getPotionID()];
                if (potion != null && potion.isBadEffect()) {
                    player.removePotionEffect(effect.getPotionID());
                    cleared = true;
                }
            }

            tag.setLong(LAST_CLEAR_TICK_TAG, worldTime);
            save();
            return cleared;
        }

        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return false;
    }
}
