package ruiseki.backpack.upgrades.traverler.ghast;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.api.IAttackEntityResponseUpgrade;
import ruiseki.backpack.api.ILivingHurtResponseUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.EntityHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class GhastUpgradeWrapper extends UpgradeWrapperBase<GhastUpgradeWrapper, ItemGhastUpgrade>
    implements ITravelersUpgrade, ILivingHurtResponseUpgrade, IAttackEntityResponseUpgrade, ITickableUpgrade {

    private static final double GHAST_RANGE = 32.0D;

    public GhastUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }

    @Override
    public boolean onLivingHurt(LivingHurtEvent event) {
        if (event.entityLiving instanceof EntityGhast ghast) {
            EntityPlayer attacker = EntityHelpers.getAttackingPlayer(event.source);
            GhastUpgradeHelpers.markGhastRetaliation(ghast, attacker);
        }
        return false;
    }

    @Override
    public boolean onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (!(event.target instanceof EntityLivingBase livingTarget)) return false;
        if (livingTarget instanceof EntityGhast ghast) {
            GhastUpgradeHelpers.markGhastRetaliation(ghast, player);
        }
        return false;
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (!(entity instanceof EntityPlayer player)) return;
        long worldTime = world.getTotalWorldTime();
        List<EntityGhast> ghasts = world
            .getEntitiesWithinAABB(EntityGhast.class, player.boundingBox.expand(GHAST_RANGE, 32.0D, GHAST_RANGE));
        for (EntityGhast ghast : ghasts) {
            if (!GhastUpgradeHelpers.isGhastAggressiveToPlayer(ghast, player, worldTime)) {
                if (ghast.targetedEntity == player) {
                    ghast.targetedEntity = null;
                }
                if (ghast.getAITarget() == player) {
                    ghast.setRevengeTarget(null);
                }
            }
        }
    }
}
