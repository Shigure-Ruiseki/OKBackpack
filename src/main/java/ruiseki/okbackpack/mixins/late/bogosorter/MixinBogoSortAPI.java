package ruiseki.okbackpack.mixins.late.bogosorter;

import org.spongepowered.asm.mixin.Mixin;

import com.cleanroommc.bogosorter.BogoSortAPI;

@Mixin(value = BogoSortAPI.class, remap = false)
public abstract class MixinBogoSortAPI {
    //
    // @Unique
    // private static final ICustomInsertable OKBACKPACK$CRAFTING_STATION_INSERTABLE =
    // MixinBogoSortAPI::okbackpack$insert;
    //
    // @Inject(method = "getInsertable", at = @At("HEAD"), cancellable = true, remap = false)
    // private void okbackpack$getInsertable(Container container, boolean player,
    // CallbackInfoReturnable<ICustomInsertable> cir) {
    // if (!player && container instanceof CraftingStationContainer) {
    // cir.setReturnValue(OKBACKPACK$CRAFTING_STATION_INSERTABLE);
    // }
    // }
    //
    // @Unique
    // private static ItemStack okbackpack$insert(Container container, List<SlotAccessor> slots, ItemStack stack,
    // boolean emptyOnly) {
    // if (!(container instanceof CraftingStationContainer)) return stack;
    //
    // for (SlotAccessor slot : slots) {
    // if (stack == null || stack.stackSize <= 0) break;
    //
    // Slot actualSlot = container.getSlot(slot.getSlotNumber());
    // if (actualSlot instanceof ModularBackpackSlot
    // || ExtendedStackLimitHelper.hasExtendedStackLimit(actualSlot)) {
    // stack = insertIntoBackpackSlot(container, actualSlot, slot, stack, emptyOnly);
    // } else {
    // stack = ShortcutHandler.insert(slot, stack, emptyOnly);
    // }
    // }
    // return stack;
    // }
    //
    // @Unique
    // private static ItemStack insertIntoBackpackSlot(Container container, Slot actualSlot, SlotAccessor slot,
    // ItemStack stack, boolean emptyOnly) {
    // ItemStack stored = slot.callGetStack();
    // boolean hasStoredStack = stored != null && stored.stackSize > 0;
    //
    // if (emptyOnly) {
    // if (hasStoredStack || !slot.callIsItemValid(stack)) return stack;
    //
    // int amount = Math.min(stack.stackSize, getBackpackStackLimit(container, actualSlot, stack));
    // if (amount <= 0) return stack;
    //
    // ItemStack placed = stack.copy();
    // placed.stackSize = amount;
    // stack.stackSize -= amount;
    // putStack(container, actualSlot, placed);
    // return stack.stackSize == 0 ? null : stack;
    // }
    //
    // if (!hasStoredStack || !ItemHandlerHelper.canItemStacksStack(stored, stack) || !slot.callIsItemValid(stack)) {
    // return stack;
    // }
    //
    // int available = Math.max(0, getBackpackStackLimit(container, actualSlot, stored) - stored.stackSize);
    // int amount = Math.min(stack.stackSize, available);
    // if (amount <= 0) return stack;
    //
    // ItemStack merged = stored.copy();
    // merged.stackSize += amount;
    // stack.stackSize -= amount;
    // putStack(container, actualSlot, merged);
    // return stack.stackSize == 0 ? null : stack;
    // }
    //
    // @Unique
    // private static int getBackpackStackLimit(Container container, Slot slot, ItemStack stack) {
    // if (slot instanceof ModularBackpackSlot backpackSlot
    // && container instanceof BackPackContainer backpackContainer) {
    // return backpackContainer.wrapper.getStackHandler()
    // .getStackLimit(backpackSlot.getSlotIndex(), stack);
    // }
    // return ExtendedStackLimitHelper.getStackLimit(slot, stack);
    // }
    //
    // @Unique
    // private static void putStack(Container container, Slot slot, ItemStack stack) {
    // if (slot instanceof ModularBackpackSlot backpackSlot
    // && container instanceof BackPackContainer backpackContainer) {
    // backpackContainer.wrapper.getStackHandler()
    // .setStackInSlot(backpackSlot.getSlotIndex(), stack);
    // return;
    // }
    // slot.putStack(stack);
    // }
}
