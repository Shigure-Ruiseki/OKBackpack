package ruiseki.okbackpack.common.helpers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import ruiseki.okbackpack.common.block.BackpackWrapper;

public final class BackpackHandSwapHelper {

    private BackpackHandSwapHelper() {}

    public static boolean canReplaceHandWithBackpackItem(BackpackWrapper wrapper, int slot, int extractCount,
        @Nullable ItemStack currentHand) {
        if (wrapper == null || slot < 0 || extractCount <= 0) return false;

        BackpackWrapper simulation = snapshotOf(wrapper);
        ItemStack extracted = simulation.extractItem(slot, extractCount, false);
        return canStoreHandAfterExtraction(simulation, extracted, currentHand);
    }

    public static boolean canReplaceHandWithBackpackItem(BackpackWrapper wrapper, ItemStack wanted, int extractCount,
        @Nullable ItemStack currentHand) {
        if (wrapper == null || wanted == null || extractCount <= 0) return false;

        BackpackWrapper simulation = snapshotOf(wrapper);
        ItemStack extracted = simulation.extractItem(wanted, extractCount, false);
        return canStoreHandAfterExtraction(simulation, extracted, currentHand);
    }

    private static boolean canStoreHandAfterExtraction(BackpackWrapper simulation, @Nullable ItemStack extracted,
        @Nullable ItemStack currentHand) {
        if (extracted == null || extracted.stackSize <= 0) return false;
        if (currentHand == null || currentHand.stackSize <= 0) return true;

        ItemStack remaining = simulation.insertItem(currentHand.copy(), false);
        return remaining == null || remaining.stackSize <= 0;
    }

    private static BackpackWrapper snapshotOf(BackpackWrapper wrapper) {
        BackpackWrapper snapshot = new BackpackWrapper();
        NBTTagCompound data = wrapper.serializeNBT();
        snapshot.deserializeNBT(data == null ? null : (NBTTagCompound) data.copy());
        return snapshot;
    }
}
