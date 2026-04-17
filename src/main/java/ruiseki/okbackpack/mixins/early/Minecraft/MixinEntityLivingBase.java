package ruiseki.okbackpack.mixins.early.Minecraft;

import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ruiseki.okbackpack.common.item.travelers.spider.SpiderUpgradeSupport;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    @Inject(method = "isOnLadder", at = @At("HEAD"), cancellable = true)
    private void okbackpack$enableSpiderClimb(CallbackInfoReturnable<Boolean> cir) {
        if (SpiderUpgradeSupport.shouldTreatAsOnLadder((EntityLivingBase) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
