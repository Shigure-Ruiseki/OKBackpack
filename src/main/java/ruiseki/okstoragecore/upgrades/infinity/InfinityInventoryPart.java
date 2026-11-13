package ruiseki.okstoragecore.upgrades.infinity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.TriFunction;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.PlayerHelpers;
import ruiseki.okstoragecore.inventory.IInventoryPartHandler;
import ruiseki.okstoragecore.inventory.InventoryHandler;
import ruiseki.okstoragecore.inventory.InventoryPartitioner.SlotRange;

public abstract class InfinityInventoryPart implements IInventoryPartHandler {

    private final InventoryHandler parent;
    private final SlotRange slotRange;
    private final int permissionLevel;
    private final Map<Integer, ItemStack> cachedStacks = new HashMap<>();

    protected InfinityInventoryPart(InventoryHandler parent, SlotRange slotRange, int permissionLevel) {
        this.parent = parent;
        this.slotRange = slotRange;
        this.permissionLevel = permissionLevel;
        parent.addListener(this::onParentSlotChanged);
    }

    private void onParentSlotChanged(int slot) {
        if (slotRange.isInRange(slot)) {
            cachedStacks.remove(slot);
        }
    }

    @Override
    public boolean isInfinite(int slot) {
        return !ItemHelpers.isEmpty(parent.getSlotStack(slot));
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack, @Nullable EntityPlayer player,
        BiPredicate<Integer, ItemStack> isItemValidSuper) {
        return player != null && PlayerHelpers.hasPermissions(player, permissionLevel)
            && ItemHelpers.isEmpty(parent.getSlotStack(slot))
            && isItemValidSuper.test(slot, stack);
    }

    @Override
    public boolean isSlotAccessible(int slot) {
        return true;
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemHelpers.copyWithSize(parent.getSlotStack(slot), amount);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate,
        TriFunction<Integer, ItemStack, Boolean, ItemStack> insertSuper) {
        if (!ItemHelpers.isEmpty(parent.getSlotStack(slot))) {
            return stack;
        }
        cachedStacks.remove(slot);
        return insertSuper.apply(slot, stack, simulate);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack, BiConsumer<Integer, ItemStack> setStackInSlotSuper) {
        if (ItemHelpers.isEmpty(parent.getSlotStack(slot))) {
            parent.setSlotStack(slot, stack);
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot, IntFunction<ItemStack> getStackInSlotSuper) {
        if (cachedStacks.containsKey(slot)
            && ItemHelpers.isEmpty(cachedStacks.get(slot)) != ItemHelpers.isEmpty(parent.getSlotStack(slot))) {
            cachedStacks.remove(slot);
        }

        return cachedStacks
            .computeIfAbsent(slot, s -> ItemHelpers.copyWithSize(parent.getSlotStack(s), Integer.MAX_VALUE));
    }

    @Override
    public int getSlots() {
        return slotRange.numberOfSlots();
    }

    public static class Admin extends InfinityInventoryPart {

        public static final String NAME = "infinity";

        protected Admin(InventoryHandler parent, SlotRange slotRange) {
            super(parent, slotRange, 2);
        }

        @Override
        public String getName() {
            return NAME;
        }
    }

    public static class Survival extends InfinityInventoryPart {

        public static final String NAME = "survival_infinity";

        protected Survival(InventoryHandler parent, SlotRange slotRange) {
            super(parent, slotRange, 0);
        }

        @Override
        public String getName() {
            return NAME;
        }
    }
}
