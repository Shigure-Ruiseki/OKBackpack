package ruiseki.okbackpack.mixins.late.thaumcraft;

import java.util.Collections;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import ruiseki.okbackpack.compat.thaumcraft.IVisChargeTarget;
import thaumcraft.common.tiles.TileMagicWorkbench;

@Mixin(value = TileMagicWorkbench.class, remap = false)
public abstract class MixinTileMagicWorkbench implements IVisChargeTarget {

    @Shadow(remap = true)
    public abstract ItemStack getStackInSlot(int slot);

    @Shadow
    public Container eventHandler;

    @Override
    public Iterable<ItemStack> getVisChargeableStacks() {
        return Collections.singletonList(getStackInSlot(10));
    }

    @Override
    public void onVisCharged() {
        eventHandler.onCraftMatrixChanged((TileMagicWorkbench) (Object) this);
    }
}
