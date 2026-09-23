package ruiseki.okbackpack.mixins.late.bogosorter;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.ShortcutHandler;
import com.cleanroommc.bogosorter.api.ICustomInsertable;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import ruiseki.okbackpack.common.block.TEBackpack;
import tconstruct.tools.gui.ChestSlot;
import tconstruct.tools.inventory.CraftingStationContainer;

@Mixin(value = BogoSortAPI.class, remap = false)
public abstract class MixinBogoSortAPI {

    @Unique
    private static final ICustomInsertable OKBACKPACK$CRAFTING_STATION_INSERTABLE = MixinBogoSortAPI::okbackpack$insert;

    @Inject(method = "getInsertable", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$getInsertable(Container container, boolean player,
        CallbackInfoReturnable<ICustomInsertable> cir) {
        if (!player && container instanceof CraftingStationContainer) {
            cir.setReturnValue(OKBACKPACK$CRAFTING_STATION_INSERTABLE);
        }
    }

    @Unique
    private static ItemStack okbackpack$insert(Container container, List<SlotAccessor> slots, ItemStack stack,
        boolean emptyOnly) {
        if (!(container instanceof CraftingStationContainer)) return stack;

        for (SlotAccessor slot : slots) {
            if (stack == null || stack.stackSize <= 0) break;

            Slot actualSlot = container.getSlot(slot.getSlotNumber());
            if (actualSlot instanceof ChestSlot && slot.getInventory() instanceof TEBackpack backpack) {
                stack = insertIntoBackpackSlot(slot, backpack, stack, emptyOnly);
            } else {
                stack = ShortcutHandler.insert(slot, stack, emptyOnly);
            }
        }
        return stack;
    }

    @Unique
    private static ItemStack insertIntoBackpackSlot(SlotAccessor slot, TEBackpack backpack, ItemStack stack,
        boolean emptyOnly) {
        ItemStack stored = backpack.getStackInSlot(slot.callGetSlotIndex());
        boolean hasStoredStack = stored != null && stored.stackSize > 0;

        if (emptyOnly) {
            if (hasStoredStack || !slot.callIsItemValid(stack)) return stack;

            int amount = Math.min(stack.stackSize, backpack.getInventoryStackLimit());
            if (amount <= 0) return stack;

            ItemStack placed = stack.copy();
            placed.stackSize = amount;
            stack.stackSize -= amount;
            slot.callPutStack(placed);
            return stack.stackSize == 0 ? null : stack;
        }

        if (!hasStoredStack || !ItemHandlerHelper.canItemStacksStack(stored, stack) || !slot.callIsItemValid(stack)) {
            return stack;
        }

        int available = Math.max(0, backpack.getInventoryStackLimit() - stored.stackSize);
        int amount = Math.min(stack.stackSize, available);
        if (amount <= 0) return stack;

        ItemStack merged = stored.copy();
        merged.stackSize += amount;
        stack.stackSize -= amount;
        slot.callPutStack(merged);
        return stack.stackSize == 0 ? null : stack;
    }
}
