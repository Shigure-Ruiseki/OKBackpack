package ruiseki.okbackpack.compat.bogosorter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.sort.ClientItemSortRule;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.ItemCompareHelper;
import com.cleanroommc.bogosorter.common.sort.ItemSortContainer;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SortHandler;

import cpw.mods.fml.common.Optional;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.BackpackSHRegistry;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;

public class BackpackBogoSorterServerCompat {

    private BackpackBogoSorterServerCompat() {}

    @Optional.Method(modid = "bogosorter")
    public static void register() {
        BackpackSHRegistry.registerServer(BackpackSHRegisters.UPDATE_BOGO_SORT_INV, (handler, buf) -> {
            sort(handler.wrapper, buf);
            handler.wrapper.markDirty();
        });
    }

    @Optional.Method(modid = "bogosorter")
    private static void sort(IStorageWrapper wrapper, PacketBuffer buf) throws IOException {
        List<SortRule<ItemStack>> itemRules = readItemRules(buf);
        List<NbtSortRule> nbtRules = readNbtRules(buf);
        Int2ObjectMap<ClientSortData> clientData = readClientData(buf);

        IntList sortableSlots = collectSortableSlots(wrapper);
        if (sortableSlots.size() < 2) return;

        List<ItemSortContainer> sortedItems = collectItems(wrapper, sortableSlots, clientData);
        if (sortedItems.isEmpty()) return;

        SortHandler.currentNbtSortRules.set(nbtRules);
        try {
            sortedItems.sort(createComparator(itemRules));
        } finally {
            SortHandler.currentNbtSortRules.set(Collections.emptyList());
        }

        rebuildSlots(wrapper, sortableSlots, sortedItems);
    }

    private static List<SortRule<ItemStack>> readItemRules(PacketBuffer buf) {
        int size = buf.readVarIntFromBuffer();
        List<SortRule<ItemStack>> rules = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            SortRule<ItemStack> rule = BogoSortAPI.INSTANCE.getItemSortRule(buf.readVarIntFromBuffer());
            boolean inverted = buf.readBoolean();
            if (rule == null || rule.isEmpty()) continue;
            rule.setInverted(inverted);
            rules.add(rule);
        }
        return rules;
    }

    private static List<NbtSortRule> readNbtRules(PacketBuffer buf) {
        int size = buf.readVarIntFromBuffer();
        List<NbtSortRule> rules = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            NbtSortRule rule = BogoSortAPI.INSTANCE.getNbtSortRule(buf.readVarIntFromBuffer());
            boolean inverted = buf.readBoolean();
            if (rule == null || rule.isEmpty()) continue;
            rule.setInverted(inverted);
            rules.add(rule);
        }
        return rules;
    }

    private static Int2ObjectMap<ClientSortData> readClientData(PacketBuffer buf) throws IOException {
        int size = buf.readVarIntFromBuffer();
        Int2ObjectMap<ClientSortData> result = new Int2ObjectOpenHashMap<>(size);
        for (int i = 0; i < size; i++) {
            int slotIndex = buf.readVarIntFromBuffer();
            String name = buf.readStringFromBuffer(32767);
            int color = buf.readVarIntFromBuffer();
            result.put(slotIndex, new ClientSortData(color, name));
        }
        return result;
    }

    private static IntList collectSortableSlots(IStorageWrapper wrapper) {
        IntList slots = new IntArrayList(wrapper.getSlots());
        for (int i = 0; i < wrapper.getSlots(); i++) {
            if (!wrapper.isSlotMemorized(i) && !wrapper.isSlotLocked(i)) {
                slots.add(i);
            }
        }
        return slots;
    }

    private static List<ItemSortContainer> collectItems(IStorageWrapper wrapper, IntList sortableSlots,
        Int2ObjectMap<ClientSortData> clientData) {
        List<ItemSortContainer> result = new ArrayList<>(sortableSlots.size());
        Object2ObjectOpenCustomHashMap<ItemStack, ItemSortContainer> merged = new Object2ObjectOpenCustomHashMap<>(
            BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);

        for (int i = 0; i < sortableSlots.size(); i++) {
            int slotIndex = sortableSlots.getInt(i);
            ItemStack stack = wrapper.getStackInSlot(slotIndex);
            if (stack == null || stack.stackSize <= 0) continue;

            ItemSortContainer existing = merged.get(stack);
            if (existing == null) {
                ItemSortContainer container = new ItemSortContainer(stack, clientData.get(slotIndex));
                merged.put(stack, container);
                result.add(container);
            } else {
                existing.grow(stack.stackSize);
            }
        }
        return result;
    }

    private static Comparator<ItemSortContainer> createComparator(List<SortRule<ItemStack>> itemRules) {
        return (left, right) -> {
            for (SortRule<ItemStack> rule : itemRules) {
                int result = rule instanceof ClientItemSortRule clientRule ? clientRule.compareServer(left, right)
                    : rule.compare(left.getItemStack(), right.getItemStack());
                if (result != 0) return result;
            }

            int result = ItemCompareHelper.compareRegistryOrder(left.getItemStack(), right.getItemStack());
            if (result != 0) return result;
            return ItemCompareHelper.compareMeta(left.getItemStack(), right.getItemStack());
        };
    }

    private static void rebuildSlots(IStorageWrapper wrapper, IntList sortableSlots,
        List<ItemSortContainer> sortedItems) {
        int itemIndex = 0;
        ItemSortContainer current = sortedItems.get(itemIndex);

        for (int i = 0; i < sortableSlots.size(); i++) {
            int slotIndex = sortableSlots.getInt(i);
            if (current == null) {
                wrapper.setStackInSlot(slotIndex, null);
                continue;
            }

            int limit = getStackLimit(wrapper, current.getItemStack());
            if (limit <= 0) {
                wrapper.setStackInSlot(slotIndex, null);
                continue;
            }

            wrapper.setStackInSlot(slotIndex, current.makeStack(limit));
            if (!current.canMakeStack()) {
                itemIndex++;
                current = itemIndex < sortedItems.size() ? sortedItems.get(itemIndex) : null;
            }
        }
    }

    private static int getStackLimit(IStorageWrapper wrapper, ItemStack stack) {
        double rawLimit = stack.getMaxStackSize() * wrapper.applyStackLimitModifiers();
        if (rawLimit >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) Math.ceil(rawLimit);
    }

}
