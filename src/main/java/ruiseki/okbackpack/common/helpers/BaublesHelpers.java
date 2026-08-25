package ruiseki.okbackpack.common.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import baubles.api.BaublesApi;
import baubles.api.expanded.BaubleExpandedSlots;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.compat.Mods;

public class BaublesHelpers {

    private static Class<?> cachedBaublesClass = null;
    private static boolean hasCheckedClass = false;

    public static boolean checkAndRegisterType(String slotType) {
        if (slotType == null || slotType.isEmpty() || !Mods.BaublesExpanded.isModLoaded()) return false;
        return BaubleExpandedSlots.isTypeRegistered(slotType) || BaubleExpandedSlots.tryRegisterType(slotType);
    }

    public static void assignSlot(String slotType) {
        if (!Mods.BaublesExpanded.isModLoaded()) return;
        BaubleExpandedSlots.tryAssignSlotOfType(slotType);
    }

    public static void assignSlotsUpToMinimum(String slotType, int size) {
        if (!Mods.BaublesExpanded.isModLoaded()) return;
        BaubleExpandedSlots.tryAssignSlotsUpToMinimum(slotType, size);
    }

    public static void unassignSlotsDownToMaximum(String slotType, int size) {
        if (!Mods.BaublesExpanded.isModLoaded()) return;
        BaubleExpandedSlots.tryUnassignSlotsDownToMaximum(slotType, size);
    }

    public static IInventory getBaubles(EntityPlayer player) {
        if (player == null || !Mods.Baubles.isModLoaded()) return null;
        return BaublesApi.getBaubles(player);
    }

    public static boolean isBaubles(Slot slot) {
        return slot != null && isBaubles(slot.inventory);
    }

    public static boolean isBaubles(IInventory inventory) {
        if (inventory == null || !Mods.Baubles.isModLoaded()) return false;

        if (!hasCheckedClass) {
            try {
                cachedBaublesClass = Class.forName("baubles.common.container.InventoryBaubles");
            } catch (ClassNotFoundException ignored) {
                OKBackpack.okLog(Mods.Baubles.modid + "not loaded");
            }
            hasCheckedClass = true;
        }

        return cachedBaublesClass != null && cachedBaublesClass.isInstance(inventory);
    }
}
