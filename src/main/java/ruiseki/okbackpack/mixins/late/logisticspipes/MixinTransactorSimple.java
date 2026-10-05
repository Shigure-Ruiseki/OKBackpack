package ruiseki.okbackpack.mixins.late.logisticspipes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import logisticspipes.utils.transactor.IInvSlot;
import logisticspipes.utils.transactor.TransactorSimple;
import ruiseki.okbackpack.api.mixinHelper.IExtendedLogisticsSlot;
import ruiseki.okbackpack.common.block.TEBackpack;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;

@Mixin(value = TransactorSimple.class, remap = false)
public abstract class MixinTransactorSimple {

    @Shadow
    protected IInventory inventory;

    @Inject(method = "addToSlot", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$addToSlot(IInvSlot slot, ItemStack stack, int injected, boolean doAdd,
        CallbackInfoReturnable<Integer> cir) {
        if (!(okbackpack$isBackpackInventory() && slot instanceof IExtendedLogisticsSlot indexedSlot)) return;

        TEBackpack backpack = (TEBackpack) inventory;
        int slotIndex = indexedSlot.okbackpack$getSlotIndex();
        int available = stack.stackSize - injected;
        int maximum = BackpackInventoryHelpers.getExtendedStackLimit(backpack, slotIndex, stack);
        ItemStack stored = slot.getStackInSlot();

        if (stored == null) {
            int wanted = Math.min(available, maximum);
            if (doAdd && wanted > 0) {
                ItemStack placed = stack.copy();
                placed.stackSize = wanted;
                backpack.getWrapper()
                    .getStackHandler()
                    .setStackInSlot(slotIndex, placed);
            }
            cir.setReturnValue(Math.max(0, wanted));
            return;
        }

        if (!okbackpack$canStacksMerge(stored, stack)) {
            cir.setReturnValue(0);
            return;
        }

        int wanted = Math.min(available, Math.max(0, maximum - stored.stackSize));
        if (doAdd && wanted > 0) {
            ItemStack merged = stored.copy();
            merged.stackSize += wanted;
            backpack.getWrapper()
                .getStackHandler()
                .setStackInSlot(slotIndex, merged);
        }
        cir.setReturnValue(Math.max(0, wanted));
    }

    @Unique
    private boolean okbackpack$isBackpackInventory() {
        return inventory instanceof TEBackpack;
    }

    @Unique
    private static boolean okbackpack$canStacksMerge(ItemStack first, ItemStack second) {
        return first.isItemEqual(second) && ItemStack.areItemStackTagsEqual(first, second);
    }
}
