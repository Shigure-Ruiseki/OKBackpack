package ruiseki.okbackpack.mixins.late.Thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import thaumcraft.common.tiles.TileMagicWorkbenchCharger;

@Mixin(value = TileMagicWorkbenchCharger.class, remap = false)
public class MixinTileMagicWorkbenchCharger {

    @WrapOperation(
        method = "updateEntity",
        at = @At(value = "CONSTANT", args = "classValue=thaumcraft/common/tiles/TileMagicWorkbench"))
    private boolean wrapInstanceOfAEBaseTile(Object obj, Operation<Boolean> original) {
        if (obj instanceof xxxx) {

            return false;
        }

        return original.call(obj);
    }
}
