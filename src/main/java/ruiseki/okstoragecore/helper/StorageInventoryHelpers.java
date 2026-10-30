package ruiseki.okstoragecore.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.capability.wrapper.PlayerMainInvWrapper;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.capabilities.CapabilityStorageWrapperConfig;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.inventory.InventoryHandler;
import ruiseki.okstoragecore.upgrades.IPickupResponseUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeHandler;

public class StorageInventoryHelpers {

    public static void transferPlayerToStorage(IStorageWrapper wrapper, PlayerMainInvWrapper playerInv,
        boolean transferMatched) {
        InventoryHandler handler = wrapper.getInventoryHandler();
        for (int i = 9; i < playerInv.getSlots(); i++) {
            ItemStack stack = playerInv.getStackInSlot(i);
            if (ItemHelpers.isEmpty(stack)) continue;
            if (transferMatched && !hasMatchingSlot(wrapper, stack)) {
                continue;
            }
            IStorageWrapper stackWrapper = CapabilityHelpers
                .getCapability(stack, CapabilityStorageWrapperConfig.CAPABILITY)
                .getOrNull();
            if (stackWrapper != null && stackWrapper.getContentsUuid()
                .equals(wrapper.getContentsUuid())) {
                continue;
            }

            ItemStack remaining = handler.insertItem(stack, false);
            playerInv.setStackInSlot(i, remaining);
        }
    }

    public static boolean hasMatchingSlot(IStorageWrapper wrapper, ItemStack stack) {
        InventoryHandler handler = wrapper.getInventoryHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (ItemHandlerHelpers.canItemStacksStack(inSlot, stack)) {
                return true;
            }
        }
        return false;
    }

    public static void transferStorageToPlayer(IStorageWrapper wrapper, PlayerMainInvWrapper playerInv,
        boolean transferMatched) {
        InventoryHandler handler = wrapper.getInventoryHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            for (int j = 9; j < playerInv.getSlots(); j++) {
                if (transferMatched && ItemHelpers.isEmpty(playerInv.getStackInSlot(j))) continue;
                stack = playerInv.insertItem(j, stack, false);
            }

            handler.setStackInSlot(i, stack);
        }
    }

    public static void rotated(ItemStackHandler stackHandler, boolean clockwise) {
        ItemStack[] old = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ItemStack stack = stackHandler.getStackInSlot(i);
            old[i] = stack == null ? null : stack.copy();
        }
        // Source index for each destination slot.
        final int[] clockwiseMap = { 6, 3, 0, 7, 4, 1, 8, 5, 2 };
        final int[] counterClockwiseMap = { 2, 5, 8, 1, 4, 7, 0, 3, 6 };
        int[] map = clockwise ? clockwiseMap : counterClockwiseMap;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = old[map[i]];
            stackHandler.setStackInSlot(i, stack == null ? null : stack.copy());
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

    public static void clear(StoragePanel panel, IStorageWrapper wrapper, ItemStackHandler stackHandler,
        boolean intoInventory) {
        PlayerMainInvWrapper playerInv = panel.getPlayerInventory();
        if (intoInventory) {
            for (int i = 0; i < stackHandler.getSlots() - 1; i++) {
                if (i == 9) continue;
                ItemStack original = stackHandler.getStackInSlot(i);
                if (original == null || original.stackSize <= 0) continue;
                ItemStack moving = original.copy();
                for (int j = 9; j < playerInv.getSlots(); j++) {
                    if (moving == null || moving.stackSize <= 0) break;
                    moving = playerInv.insertItem(j, moving, false);
                }
                stackHandler.setStackInSlot(i, moving);
            }
        } else {
            for (int i = 0; i < stackHandler.getSlots() - 1; i++) {
                if (i == 9) continue;
                ItemStack original = stackHandler.getStackInSlot(i);
                if (original == null || original.stackSize <= 0) continue;
                ItemStack remaining = wrapper.getInventoryHandler()
                    .insertItem(original.copy(), false);
                stackHandler.setStackInSlot(i, remaining);
            }
        }

    }

    public static ItemStack runPickupOnPickupResponseUpgrades(World level, UpgradeHandler upgradeHandler,
        ItemStack remainingStack, boolean simulate) {
        return runPickupOnPickupResponseUpgrades(level, null, upgradeHandler, remainingStack, simulate);
    }

    public static ItemStack runPickupOnPickupResponseUpgrades(World level, @Nullable EntityPlayer player,
        UpgradeHandler upgradeHandler, ItemStack remainingStack, boolean simulate) {
        if (ItemHelpers.isEmpty(remainingStack) || remainingStack.getMaxStackSize() <= 0) {
            return remainingStack;
        }

        List<IPickupResponseUpgrade> pickupUpgrades = upgradeHandler
            .getWrappersThatImplement(IPickupResponseUpgrade.class);

        for (IPickupResponseUpgrade pickupUpgrade : pickupUpgrades) {
            int countBeforePickup = ItemHelpers.getCount(remainingStack);

            remainingStack = pickupUpgrade.pickup(level, remainingStack, simulate);

            int countAfterPickup = ItemHelpers.getCount(remainingStack);

            if (!simulate && player != null && countAfterPickup < countBeforePickup) {
                playPickupSound(level, player);
            }

            if (ItemHelpers.isEmpty(remainingStack)) {
                return ItemHelpers.EMPTY;
            }
        }

        return remainingStack;
    }

    private static void playPickupSound(World world, @Nonnull EntityPlayer player) {
        world.playSoundAtEntity(
            player,
            "random.pop",
            0.2F,
            ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }

}
