package ruiseki.okbackpack.mixins.late.logisticspipes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.item.ItemIdentifier;
import ruiseki.okbackpack.common.block.TEBackpack;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;

@Mixin(value = InventoryUtil.class, remap = false)
public abstract class MixinInventoryUtil {

    @Final
    @Shadow
    protected IInventory _inventory;

    @Final
    @Shadow
    private int _cropStart;

    @Final
    @Shadow
    private int _cropEnd;

    @Inject(
        method = "roomForItem(Llogisticspipes/utils/item/ItemIdentifier;I)I",
        at = @At("HEAD"),
        cancellable = true,
        remap = false)
    private void okbackpack$roomForItem(ItemIdentifier item, int count, CallbackInfoReturnable<Integer> cir) {
        if (!(_inventory instanceof TEBackpack backpack)) return;

        long totalRoom = 0;
        int size = backpack.getSizeInventory();
        for (int slot = _cropStart; slot < size - _cropEnd && totalRoom < count; slot++) {
            ItemStack stored = backpack.getStackInSlot(slot);
            ItemStack candidate = stored == null ? item.unsafeMakeNormalStack(1) : stored;
            if (stored == null) {
                if (!backpack.isItemValidForSlot(slot, candidate)) continue;
            } else if (!ItemIdentifier.get(stored)
                .equals(item)) {
                    continue;
                }

            int capacity = BackpackInventoryHelpers.getExtendedStackLimit(backpack, slot, candidate);
            totalRoom += Math.max(0, capacity - (stored == null ? 0 : stored.stackSize));
        }
        cir.setReturnValue((int) Math.min(Integer.MAX_VALUE, Math.min(totalRoom, count)));
    }

    @Inject(method = "addToSlot(Lnet/minecraft/item/ItemStack;I)I", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$addToSlot(ItemStack stack, int slot, CallbackInfoReturnable<Integer> cir) {
        if (!(_inventory instanceof TEBackpack backpack)) return;
        if (stack == null || !backpack.isItemValidForSlot(slot, stack)) {
            cir.setReturnValue(0);
            return;
        }

        int maximum = BackpackInventoryHelpers.getExtendedStackLimit(backpack, slot, stack);
        ItemStack stored = backpack.getStackInSlot(slot);
        if (stored == null) {
            int wanted = Math.min(stack.stackSize, maximum);
            if (wanted > 0) {
                ItemStack placed = stack.copy();
                placed.stackSize = wanted;
                backpack.getWrapper()
                    .getStackHandler()
                    .setStackInSlot(slot, placed);
            }
            cir.setReturnValue(wanted);
            return;
        }

        if (!okbackpack$canStacksMerge(stored, stack)) {
            cir.setReturnValue(0);
            return;
        }

        int wanted = Math.min(stack.stackSize, Math.max(0, maximum - stored.stackSize));
        if (wanted > 0) {
            ItemStack merged = stored.copy();
            merged.stackSize += wanted;
            backpack.getWrapper()
                .getStackHandler()
                .setStackInSlot(slot, merged);
        }
        cir.setReturnValue(wanted);
    }

    @Unique
    private static boolean okbackpack$canStacksMerge(ItemStack first, ItemStack second) {
        return first.isItemEqual(second) && ItemStack.areItemStackTagsEqual(first, second);
    }
}
