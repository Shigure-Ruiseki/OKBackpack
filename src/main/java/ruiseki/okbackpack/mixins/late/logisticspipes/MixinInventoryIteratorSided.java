package ruiseki.okbackpack.mixins.late.logisticspipes;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import ruiseki.okbackpack.api.mixinHelper.IExtendedLogisticsSlot;

@Mixin(targets = "logisticspipes.utils.transactor.InventoryIteratorSided$InvSlot", remap = false)
public abstract class MixinInventoryIteratorSided implements IExtendedLogisticsSlot {

    @Final
    @Shadow
    private int slot;

    @Override
    public int okbackpack$getSlotIndex() {
        return slot;
    }
}
