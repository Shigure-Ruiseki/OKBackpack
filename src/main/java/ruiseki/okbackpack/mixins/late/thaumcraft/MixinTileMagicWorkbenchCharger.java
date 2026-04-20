package ruiseki.okbackpack.mixins.late.thaumcraft;

import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ruiseki.okbackpack.compat.thaumcraft.IVisChargeTarget;
import ruiseki.okbackpack.compat.thaumcraft.ThaumcraftChargeHelper;
import thaumcraft.common.tiles.TileMagicWorkbenchCharger;
import thaumcraft.common.tiles.TileVisRelay;

@Mixin(value = TileMagicWorkbenchCharger.class, remap = true)
public abstract class MixinTileMagicWorkbenchCharger extends TileVisRelay {

    @Inject(method = "updateEntity", at = @At("HEAD"), cancellable = true)
    private void okbackpack$updateEntity(CallbackInfo ci) {
        super.updateEntity();
        if (!this.worldObj.isRemote) {
            TileEntity tile = this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);
            if (tile instanceof IVisChargeTarget chargeTarget) {
                boolean charged = ThaumcraftChargeHelper
                    .chargeStacks(chargeTarget.getVisChargeableStacks(), this::consumeVis);
                if (charged) {
                    chargeTarget.onVisCharged();
                }
            }
        }

        ci.cancel();
    }
}
