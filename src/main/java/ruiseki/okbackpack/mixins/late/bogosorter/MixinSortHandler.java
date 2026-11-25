package ruiseki.okbackpack.mixins.late.bogosorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.common.McUtils;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.ItemSortContainer;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
import tconstruct.api.ExtendedStackLimitHelper;

@Mixin(value = SortHandler.class, remap = false)
public abstract class MixinSortHandler {

    @Final
    @Shadow
    private Container container;

    @Final
    @Shadow
    private Int2ObjectMap<ClientSortData> clientSortData;

    @Final
    @Shadow
    private EntityPlayer player;

    @Final
    @Shadow
    private Comparator<ItemSortContainer> containerComparator;

    @Final
    @Shadow
    private List<NbtSortRule> nbtSortRules;

    @Shadow
    private static List<ItemStack> prepareDropList(List<ItemSortContainer> sortedList) {
        return null;
    }

    @Shadow
    public abstract List<SlotAccessor> getSortableSlots(SlotGroup slotGroup);

    @Shadow
    public abstract LinkedList<ItemSortContainer> gatherItems(SlotGroup slotGroup);

    @Inject(method = "sortHorizontal", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$sortBackpackStacks(SlotGroup slotGroup, CallbackInfo ci) {
        List<SlotAccessor> slots = getEffectiveSortableSlots(slotGroup);
        boolean hasBackpackSlot = false;
        for (SlotAccessor slot : slots) {
            if (hasExtendedStackLimit(slot)) {
                hasBackpackSlot = true;
                break;
            }
        }
        if (!hasBackpackSlot) return;

        LinkedList<ItemSortContainer> items = gatherItems(slotGroup);
        if (items.isEmpty()) {
            ci.cancel();
            return;
        }

        SortHandler.currentNbtSortRules.set(nbtSortRules);
        try {
            items.sort(containerComparator);
        } finally {
            SortHandler.currentNbtSortRules.set(Collections.emptyList());
        }

        ItemSortContainer current = items.pollFirst();
        for (SlotAccessor slot : slots) {
            if (current == null) {
                putStack(slot, null);
                continue;
            }

            ItemStack stack = current.getItemStack();
            int limit = getSortingLimit(slot, stack);
            if (limit <= 0) continue;

            if (preventSplit(stack)) {
                putStack(slot, stack);
                current = items.pollFirst();
                continue;
            }

            putStack(slot, current.makeStack(limit));
            if (!current.canMakeStack()) {
                current = items.pollFirst();
            }
        }

        if (current != null) items.addFirst(current);
        if (!items.isEmpty()) {
            McUtils.giveItemsToPlayer(player, prepareDropList(items));
        }
        ci.cancel();
    }

    @Inject(method = "gatherItems", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$gatherBackpackItems(SlotGroup slotGroup,
        CallbackInfoReturnable<LinkedList<ItemSortContainer>> cir) {
        List<SlotAccessor> slots = getEffectiveSortableSlots(slotGroup);
        boolean hasBackpackSlot = false;
        for (SlotAccessor slot : slots) {
            if (hasExtendedStackLimit(slot)) {
                hasBackpackSlot = true;
                break;
            }
        }
        if (!hasBackpackSlot) return;

        LinkedList<ItemSortContainer> result = new LinkedList<>();
        Object2ObjectOpenCustomHashMap<ItemStack, ItemSortContainer> merged = new Object2ObjectOpenCustomHashMap<>(
            BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);

        for (SlotAccessor slot : slots) {
            ItemStack stack = getStack(slot);
            if (stack == null || stack.stackSize <= 0) continue;

            ItemSortContainer current = new ItemSortContainer(stack, clientSortData.get(slot.getSlotNumber()));
            if (preventSplit(stack)) {
                result.add(current);
            } else {
                ItemSortContainer existing = merged.get(stack);
                if (existing == null || existing.getAmount() > Integer.MAX_VALUE - stack.stackSize) {
                    merged.put(stack, current);
                    result.add(current);
                } else {
                    existing.grow(stack.stackSize);
                }
            }
        }

        cir.setReturnValue(result);
    }

    @Unique
    private ItemStack getStack(SlotAccessor slot) {
        Slot actualSlot = getActualSlot(slot);
        if (actualSlot instanceof ModularBackpackSlot backpackSlot
            && container instanceof BackPackContainer backpackContainer) {
            return backpackContainer.wrapper.getStackHandler()
                .getStackInSlot(backpackSlot.getSlotIndex());
        }
        return hasExtendedStackLimit(slot) ? actualSlot.getStack() : slot.callGetStack();
    }

    @Unique
    private boolean hasExtendedStackLimit(SlotAccessor slot) {
        Slot actualSlot = getActualSlot(slot);
        return actualSlot instanceof ModularBackpackSlot || ExtendedStackLimitHelper.hasExtendedStackLimit(actualSlot);
    }

    @Unique
    private static boolean preventSplit(ItemStack stack) {
        return BogoSorterConfig.preventSplit && stack.getMaxStackSize() == 1;
    }

    @Unique
    private int getSortingLimit(SlotAccessor slot, ItemStack stack) {
        Slot actualSlot = getActualSlot(slot);
        if (actualSlot instanceof ModularBackpackSlot backpackSlot
            && container instanceof BackPackContainer backpackContainer) {
            return backpackContainer.wrapper.getStackHandler()
                .getStackLimit(backpackSlot.getSlotIndex(), stack);
        }
        if (hasExtendedStackLimit(slot)) {
            return ExtendedStackLimitHelper.getStackLimit(actualSlot, stack);
        }
        return Math.min(slot.callGetSlotStackLimit(), stack.getMaxStackSize());
    }

    @Unique
    private List<SlotAccessor> getEffectiveSortableSlots(SlotGroup slotGroup) {
        List<SlotAccessor> slots = getSortableSlots(slotGroup);
        if (!(container instanceof BackPackContainer backpackContainer)) return slots;

        List<SlotAccessor> result = new LinkedList<>();
        for (SlotAccessor slot : slots) {
            Slot actualSlot = getActualSlot(slot);
            if (actualSlot instanceof ModularBackpackSlot backpackSlot
                && (backpackContainer.wrapper.isSlotLocked(backpackSlot.getSlotIndex())
                    || backpackContainer.wrapper.isSlotMemorized(backpackSlot.getSlotIndex()))) {
                continue;
            }
            result.add(slot);
        }
        return result;
    }

    @Unique
    private void putStack(SlotAccessor slot, ItemStack stack) {
        Slot actualSlot = getActualSlot(slot);
        if (actualSlot instanceof ModularBackpackSlot backpackSlot
            && container instanceof BackPackContainer backpackContainer) {
            BackpackItemStackHandler storage = backpackContainer.wrapper.getStackHandler();
            storage.setStackInSlot(backpackSlot.getSlotIndex(), stack);
            return;
        }
        slot.callPutStack(stack);
    }

    @Unique
    private Slot getActualSlot(SlotAccessor slot) {
        return container.getSlot(slot.getSlotNumber());
    }
}
