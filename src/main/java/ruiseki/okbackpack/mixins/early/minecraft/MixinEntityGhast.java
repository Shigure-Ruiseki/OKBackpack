package ruiseki.okbackpack.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ruiseki.okbackpack.common.item.travelers.ghast.GhastUpgradeSupport;

@Mixin(EntityGhast.class)
public abstract class MixinEntityGhast {

    @Redirect(
        method = "updateEntityActionState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getClosestVulnerablePlayerToEntity(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/EntityPlayer;"))
    private EntityPlayer okbackpack$findClosestValidTarget(World world, Entity entity, double maxDistance) {
        var ghast = (EntityGhast) entity;
        ghast.aggroCooldown = 20;
        return GhastUpgradeSupport.findClosestValidTarget(ghast, maxDistance);
    }
}
