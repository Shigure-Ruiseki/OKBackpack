package ruiseki.okbackpack.mixins.late.thaumcraft;

import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import ruiseki.okbackpack.compat.thaumcraft.IVisChargeTarget;
import ruiseki.okbackpack.compat.thaumcraft.ThaumcraftChargeHelper;
import thaumcraft.common.tiles.TileMagicWorkbenchCharger;
import thaumcraft.common.tiles.TileVisRelay;

@Mixin(value = TileMagicWorkbenchCharger.class, priority = 999)
public abstract class MixinTileMagicWorkbenchCharger extends TileVisRelay {

    /**
     * @author OKBackpack
     * @reason Use an interface to simplify extension and improve flexibility
     */
    @Overwrite
    public void updateEntity() {
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
    }
}
