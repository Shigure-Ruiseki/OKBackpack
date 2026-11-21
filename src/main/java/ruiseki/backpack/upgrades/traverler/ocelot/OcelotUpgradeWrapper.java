package ruiseki.backpack.upgrades.traverler.ocelot;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class OcelotUpgradeWrapper extends UpgradeWrapperBase<OcelotUpgradeWrapper, ItemOcelotUpgrade>
    implements ITickableUpgrade, ITravelersUpgrade {

    private static final double HOSTILE_RANGE = 8.0D;
    private static final int SPEED_DURATION = 40;

    public OcelotUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (!(entity instanceof EntityPlayer player)) return;
        if (hasNearbyHostile(player)) {
            player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, SPEED_DURATION, 0, true));
        }
    }

    private boolean hasNearbyHostile(EntityPlayer player) {
        List<EntityLivingBase> entities = player.worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class,
            player.boundingBox.expand(HOSTILE_RANGE, 4.0D, HOSTILE_RANGE));
        for (EntityLivingBase entity : entities) {
            if (entity == player || !entity.isEntityAlive()) continue;
            if (entity instanceof IMob) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }
}
