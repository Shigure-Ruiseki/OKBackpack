package ruiseki.okbackpack.mixins.late.bogosorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
import ruiseki.okbackpack.common.block.TEBackpack;
import tconstruct.tools.gui.ChestSlot;
import tconstruct.tools.inventory.CraftingStationContainer;

@Mixin(value = SortHandler.class, remap = false)
public abstract class MixinSortHandler {

    @Shadow
    private Container container;

    @Shadow
    private Int2ObjectMap<ClientSortData> clientSortData;

    @Shadow
    private EntityPlayer player;

    @Shadow
    private Comparator<ItemSortContainer> containerComparator;

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
        List<SlotAccessor> slots = getSortableSlots(slotGroup);
        boolean hasBackpackSlot = false;
        for (SlotAccessor slot : slots) {
            if (isBackpackChestSlot(slot)) {
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
                slot.callPutStack(null);
                continue;
            }

            ItemStack stack = current.getItemStack();
            int limit = getSortingLimit(slot, stack);
            if (limit <= 0) continue;

            if (preventSplit(stack)) {
                slot.callPutStack(stack);
                current = items.pollFirst();
                continue;
            }

            slot.callPutStack(current.makeStack(limit));
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
        if (!(container instanceof CraftingStationContainer)) return;

        List<SlotAccessor> slots = getSortableSlots(slotGroup);
        boolean hasBackpackSlot = false;
        for (SlotAccessor slot : slots) {
            if (isBackpackChestSlot(slot)) {
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
                if (existing == null) {
                    merged.put(stack, current);
                    result.add(current);
                } else {
                    existing.grow(stack.stackSize);
                }
            }
        }

        cir.setReturnValue(result);
    }

    private ItemStack getStack(SlotAccessor slot) {
        if (!isBackpackChestSlot(slot)) {
            return slot.callGetStack();
        }
        return slot.getInventory()
            .getStackInSlot(slot.callGetSlotIndex());
    }

    private boolean isBackpackChestSlot(SlotAccessor slot) {
        Slot actualSlot = container.getSlot(slot.getSlotNumber());
        return actualSlot instanceof ChestSlot && slot.getInventory() instanceof TEBackpack;
    }

    private static boolean preventSplit(ItemStack stack) {
        return BogoSorterConfig.preventSplit && stack.getMaxStackSize() == 1;
    }

    private int getSortingLimit(SlotAccessor slot, ItemStack stack) {
        if (isBackpackChestSlot(slot)) {
            return Math.max(
                0,
                slot.getInventory()
                    .getInventoryStackLimit());
        }
        return Math.min(slot.callGetSlotStackLimit(), stack.getMaxStackSize());
    }
}
