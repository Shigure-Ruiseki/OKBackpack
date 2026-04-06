package ruiseki.okbackpack.common.item.infinity;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.Nullable;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IInfinityUpgrade;
import ruiseki.okbackpack.api.wrapper.IInventoryModifiable;
import ruiseki.okbackpack.api.wrapper.ISlotModifiable;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class InfinityUpgradeWrapper extends UpgradeWrapperBase
    implements IInventoryModifiable, ISlotModifiable, IInfinityUpgrade {

    private static final String SAVED_STACK_SIZES_TAG = "SavedStackSizes";

    private final boolean admin;
    private boolean active = true;

    public InfinityUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer,
        boolean admin) {
        super(upgrade, storage, upgradeConsumer);
        this.admin = admin;
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }

    @Override
    public @Nullable ItemStack onInsert(int slot, @Nullable ItemStack stack, boolean simulate) {
        if (stack == null || !active) return stack;

        // Occupied slot: reject insertion (already infinite)
        ItemStack existing = storage.getStackInSlot(slot);
        if (existing != null) {
            return stack;
        }

        // Empty slot: inflate the incoming stack to MAX_VALUE
        ItemStack inflated = stack.copy();
        inflated.stackSize = Integer.MAX_VALUE;
        return inflated;
    }

    @Override
    public @Nullable ItemStack onExtract(int slot, @Nullable ItemStack extracted, boolean simulate) {
        if (extracted == null || !active) return extracted;

        if (!simulate) {
            // Replenish the slot back to MAX_VALUE after the raw handler depleted it
            ItemStack replenish = extracted.copy();
            replenish.stackSize = Integer.MAX_VALUE;
            storage.setStackInSlot(slot, replenish);
        }

        return extracted;
    }

    @Override
    public @Nullable ItemStack onGet(int slot, @Nullable ItemStack currentStack) {
        if (currentStack == null || !active) return currentStack;

        ItemStack copy = currentStack.copy();
        copy.stackSize = Integer.MAX_VALUE;
        return copy;
    }

    @Override
    public ItemStack onSet(int slot, @Nullable ItemStack stack) {
        if (stack == null || !active) return stack;

        // Inflate any item being set to MAX_VALUE
        if (stack.stackSize < Integer.MAX_VALUE) {
            ItemStack copy = stack.copy();
            copy.stackSize = Integer.MAX_VALUE;
            return copy;
        }

        return stack;
    }

    @Override
    public boolean canAddUpgrade(int slot, ItemStack stack) {
        // Allow swapping between infinity upgrade variants
        if (stack != null && (stack.getItem() instanceof ItemInfinityUpgrade
            || stack.getItem() instanceof ItemSurvivalInfinityUpgrade)) {
            return true;
        }
        // No other upgrades can be added when infinity upgrade is installed
        return false;
    }

    @Override
    public boolean canRemoveUpgrade(int slotIndex) {
        // Admin check is handled in ModularUpgradeSlot.canTakeStack where player context is available
        return true;
    }

    @Override
    public boolean canReplaceUpgrade(int slotIndex, ItemStack replacement) {
        // Admin check is handled in ModularUpgradeSlot.canTakeStack where player context is available
        return true;
    }

    @Override
    public void onAdded() {
        active = false; // Bypass interceptors to access raw data

        int slots = storage.getSlots();
        int[] savedSizes = new int[slots];

        // Record original stack sizes
        for (int i = 0; i < slots; i++) {
            ItemStack stack = storage.getStackInSlot(i);
            savedSizes[i] = stack != null ? stack.stackSize : 0;
        }

        // Persist to upgrade NBT
        NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
        tag.setIntArray(SAVED_STACK_SIZES_TAG, savedSizes);
        save();

        // Inflate all items to MAX_VALUE
        for (int i = 0; i < slots; i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (stack != null) {
                stack.stackSize = Integer.MAX_VALUE;
                storage.setStackInSlot(i, stack);
            }
        }

        active = true;
        storage.markDirty();
    }

    @Override
    public void onBeforeRemoved() {
        active = false; // Bypass interceptors to access raw data

        NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
        int[] savedSizes = tag.hasKey(SAVED_STACK_SIZES_TAG) ? tag.getIntArray(SAVED_STACK_SIZES_TAG) : null;

        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (stack != null) {
                if (savedSizes != null && i < savedSizes.length && savedSizes[i] > 0) {
                    stack.stackSize = savedSizes[i];
                } else {
                    stack.stackSize = stack.getMaxStackSize();
                }
                storage.setStackInSlot(i, stack);
            }
        }

        storage.markDirty();
    }

    public static boolean isAdmin(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) return true;
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return false;
        return server.getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }
}
