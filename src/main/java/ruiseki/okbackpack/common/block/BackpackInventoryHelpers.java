package ruiseki.okbackpack.common.block;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.item.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.item.ItemStackHandler;

public class BackpackInventoryHelpers {

    public static void sortInventory(BackpackWrapper handler, boolean reverse) {
        for (int i = 0; i < handler.getBackpackSlots() - 1; i++) {
            if (handler.isSlotLocked(i)) continue;
            boolean isMem = handler.isSlotMemorized(i);
            ItemStack baseStack = handler.getStackInSlot(i);
            if (baseStack == null) continue;
            int slotMaxSize = baseStack.getMaxStackSize() * handler.getTotalStackMultiplier();

            for (int j = i + 1; j < handler.getBackpackSlots(); j++) {
                if (isMem != handler.isSlotMemorized(j) || handler.isSlotLocked(j)) continue;
                ItemStack stack = handler.getStackInSlot(j);
                if (!ItemHandlerHelpers.canItemStacksStack(baseStack, stack)) continue;
                if (stack.stackSize <= 0) continue;

                int diff = Math.min(stack.stackSize, slotMaxSize - baseStack.stackSize);

                if (diff > 0) {
                    baseStack.stackSize += diff;
                    stack.stackSize -= diff;
                    if (stack.stackSize <= 0) {
                        handler.getBackpackHandler()
                            .setStackInSlot(j, null);
                    }
                } else if (diff == 0) break;
            }
        }

        List<ItemStack> sorted = new ArrayList<>();
        List<Map.Entry<ItemStack, Integer>> inPlace = new ArrayList<>();

        for (int i = 0; i < handler.getBackpackSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (handler.isSlotMemorized(i) || handler.isSlotLocked(i)) {
                inPlace.add(new AbstractMap.SimpleEntry<>(stack, i));
            } else {
                sorted.add(stack);
            }
        }

        sorted.sort((a, b) -> {
            if (a == null || a.stackSize <= 0) {
                if (b == null || b.stackSize <= 0) return 0;
                return 1;
            }
            if (b == null || b.stackSize <= 0) return -1;

            switch (handler.getSortType()) {
                case BY_NAME:
                    if (reverse) {
                        return a.getDisplayName()
                            .compareTo(b.getDisplayName());
                    } else {
                        return b.getDisplayName()
                            .compareTo(a.getDisplayName());
                    }
                case BY_MOD_ID:
                    String aItem = Item.itemRegistry.getNameForObject(a.getItem());
                    String bItem = Item.itemRegistry.getNameForObject(b.getItem());
                    String aDomain = aItem.substring(0, aItem.indexOf(":"));
                    String bDomain = bItem.substring(0, bItem.indexOf(":"));
                    if (reverse) {
                        return aDomain.compareTo(bDomain);
                    } else {
                        return bDomain.compareTo(aDomain);
                    }
                case BY_COUNT:
                    if (reverse) {
                        return Integer.compare(a.stackSize, b.stackSize);
                    } else {
                        return Integer.compare(b.stackSize, a.stackSize);
                    }
                case BY_ORE_DICT:
                    List<String> ore1 = oreNames(a);
                    List<String> ore2 = oreNames(b);
                    if (reverse) {
                        return compareLists(ore1, ore2);
                    } else {
                        return compareLists(ore2, ore1);
                    }
            }
            return 0;
        });

        for (Map.Entry<ItemStack, Integer> entry : inPlace) {
            sorted.add(entry.getValue(), entry.getKey());
        }

        handler.getBackpackHandler()
            .setSize(handler.getBackpackSlots());

        for (int i = 0; i < sorted.size(); i++) {
            handler.getBackpackHandler()
                .insertItem(i, sorted.get(i), false);
        }
    }

    private static List<String> oreNames(ItemStack stack) {
        int[] ids = OreDictionary.getOreIDs(stack);
        List<String> names = new ArrayList<String>();
        for (int id : ids) names.add(OreDictionary.getOreName(id));
        return names;
    }

    private static int compareLists(List<String> a, List<String> b) {
        int size = Math.min(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            int cmp = a.get(i)
                .compareTo(b.get(i));
            if (cmp != 0) return cmp;
        }
        return Integer.compare(a.size(), b.size());
    }

    private static boolean hasMatchingSlot(BackpackWrapper handler, ItemStack stack) {
        for (int i = 0; i < handler.getBackpackSlots(); i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (ItemHandlerHelpers.canItemStacksStack(inSlot, stack)) {
                return true;
            }
        }
        return false;
    }

    public static void transferPlayerInventoryToBackpack(BackpackWrapper handler, PlayerMainInvWrapper playerInv,
        boolean transferMatched) {
        for (int i = 9; i < playerInv.getSlots(); i++) {
            ItemStack stack = playerInv.getStackInSlot(i);
            if (stack == null) continue;
            if (stack.getItem() instanceof BlockBackpack.ItemBackpack backpack) {

                BackpackWrapper other = new BackpackWrapper(stack, backpack);
                if (other == handler) continue;
                if (!handler.canNestBackpack()) continue;
            }

            if (transferMatched && !hasMatchingSlot(handler, stack)) {
                continue;
            }

            ItemStack remaining = handler.insertItem(stack, false);
            playerInv.setStackInSlot(i, remaining);

        }
    }

    public static void transferBackpackToPlayerInventory(BackpackWrapper handler, PlayerMainInvWrapper playerInv,
        boolean transferMatched) {
        for (int i = 0; i < handler.getBackpackSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            for (int j = 9; j < playerInv.getSlots(); j++) {

                if (transferMatched && playerInv.getStackInSlot(j) == null) continue;

                stack = playerInv.insertItem(j, stack, false);
            }

            handler.setStackInSlot(i, stack);
        }
    }

    public static void rotated(ItemStackHandler stackHandler, boolean clockwise) {
        ItemStack[] old = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            old[i] = stackHandler.getStackInSlot(i);
        }

        if (clockwise) {
            stackHandler.setStackInSlot(0, old[3]);
            stackHandler.setStackInSlot(1, old[0]);
            stackHandler.setStackInSlot(2, old[1]);

            stackHandler.setStackInSlot(3, old[6]);
            stackHandler.setStackInSlot(4, old[4]);
            stackHandler.setStackInSlot(5, old[2]);

            stackHandler.setStackInSlot(6, old[7]);
            stackHandler.setStackInSlot(7, old[8]);
            stackHandler.setStackInSlot(8, old[5]);
        } else {
            stackHandler.setStackInSlot(0, old[2]);
            stackHandler.setStackInSlot(0, old[1]);
            stackHandler.setStackInSlot(1, old[2]);
            stackHandler.setStackInSlot(2, old[5]);

            stackHandler.setStackInSlot(3, old[0]);
            stackHandler.setStackInSlot(4, old[4]);
            stackHandler.setStackInSlot(5, old[8]);

            stackHandler.setStackInSlot(6, old[3]);
            stackHandler.setStackInSlot(7, old[6]);
            stackHandler.setStackInSlot(8, old[7]);
        }
    }

    public static void balance(ItemStackHandler handler) {

        Map<ItemStack, List<Integer>> groups = new HashMap<>();

        for (int i = 0; i < 9; i++) {
            ItemStack s = handler.getStackInSlot(i);
            if (s == null) continue;

            boolean found = false;
            for (ItemStack key : groups.keySet()) {
                if (ItemHandlerHelpers.canItemStacksStack(s, key)) {
                    groups.get(key)
                        .add(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                ItemStack key = s.copy();
                key.stackSize = 1;
                List<Integer> list = new ArrayList<>();
                list.add(i);
                groups.put(key, list);
            }
        }

        for (Map.Entry<ItemStack, List<Integer>> entry : groups.entrySet()) {

            ItemStack target = entry.getKey();
            List<Integer> slots = entry.getValue();
            int slotCount = slots.size();

            if (slotCount <= 1) continue;

            int total = 0;
            for (int slot : slots) {
                total += handler.getStackInSlot(slot).stackSize;
            }

            int base = total / slotCount;
            int remain = total % slotCount;

            for (int i = 0; i < slotCount; i++) {
                int slot = slots.get(i);
                ItemStack old = handler.getStackInSlot(slot);

                int want = base + (i < remain ? 1 : 0);
                int give = Math.min(want, old.getMaxStackSize());

                ItemStack out = target.copy();
                out.stackSize = give;
                handler.setStackInSlot(slot, out);
            }
        }
    }

    public static void spread(ItemStackHandler handler) {

        ItemStack source = null;
        for (int i = 0; i < 9; i++) {
            ItemStack s = handler.getStackInSlot(i);
            if (s != null && s.stackSize > 0) {
                source = s;
                break;
            }
        }
        if (source == null) return;

        List<Integer> slots = new ArrayList<>();
        int totalCount = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = handler.getStackInSlot(i);
            if (s == null) {
                slots.add(i);
            } else if (ItemHandlerHelpers.canItemStacksStack(s, source)) {
                slots.add(i);
                totalCount += s.stackSize;
            }
        }
        if (slots.isEmpty()) return;

        int slotCount = slots.size();
        int base = totalCount / slotCount;
        int remain = totalCount % slotCount;

        for (int i = 0; i < slotCount; i++) {
            int slot = slots.get(i);
            int give = base + (i < remain ? 1 : 0);
            give = Math.min(give, source.getMaxStackSize());

            ItemStack out = source.copy();
            out.stackSize = give;
            handler.setStackInSlot(slot, out);
        }
    }

    public static void clear(BackpackPanel panel, ItemStackHandler stackHandler, int ordinal) {
        ICraftingUpgrade.CraftingDestination type = ICraftingUpgrade.CraftingDestination.values()[ordinal];

        EntityPlayer player = panel.getPlayer();
        PlayerMainInvWrapper playerInv = new PlayerMainInvWrapper(player.inventory);
        BackpackWrapper wrapper = panel.getWrapper();

        switch (type) {
            case INVENTORY:
                for (int i = 0; i < stackHandler.getSlots() - 1; i++) {
                    ItemStack original = stackHandler.getStackInSlot(i);
                    if (original == null || original.stackSize <= 0) continue;

                    ItemStack moving = original.copy();

                    for (int j = 9; j < playerInv.getSlots(); j++) {
                        if (moving == null || moving.stackSize <= 0) break;
                        moving = playerInv.insertItem(j, moving, false);
                    }

                    stackHandler.setStackInSlot(i, moving);
                }
                break;

            case BACKPACK:
                for (int i = 0; i < stackHandler.getSlots() - 1; i++) {
                    ItemStack original = stackHandler.getStackInSlot(i);
                    if (original == null || original.stackSize <= 0) continue;

                    ItemStack remaining = wrapper.insertItem(original.copy(), false);

                    stackHandler.setStackInSlot(i, remaining);
                }
                break;
        }
    }

    public static ItemStack getQuickDrawStack(IInventory inventory, ItemStack wanted, InventoryType type) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack backpackStack = inventory.getStackInSlot(i);
            if (backpackStack == null || backpackStack.stackSize <= 0) continue;
            if (!(backpackStack.getItem() instanceof BlockBackpack.ItemBackpack backpack)) continue;

            BackpackWrapper handler = new BackpackWrapper(backpackStack, backpack);
            ItemStack extracted = handler.extractItem(wanted, wanted.getMaxStackSize(), false);

            OKBackpack.instance.getPacketHandler()
                .sendToServer(new PacketBackpackNBT(i, handler.getTagCompound(), type));
            if (extracted != null && extracted.stackSize > 0) {
                return extracted;
            }
        }
        return null;
    }
}
