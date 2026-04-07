package ruiseki.okbackpack.common.item.refill;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.cleanroommc.modularui.utils.item.IItemHandler;

import lombok.Getter;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.IRefillUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;
import ruiseki.okcore.helper.ItemStackHelpers;

@Getter
public class RefillUpgradeWrapper extends UpgradeWrapperBase implements IRefillUpgrade {

    private static final String ENABLED_TAG = "Enabled";
    private static final int COOLDOWN_TICKS = 20;
    private static final int BLOCK_RANGE = 5;

    protected final int filterSlotCount;
    protected BaseItemStackHandler filterHandler;

    public RefillUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        this(upgrade, storage, upgradeConsumer, 8);
    }

    protected RefillUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer,
        int filterSlotCount) {
        super(upgrade, storage, upgradeConsumer);
        this.filterSlotCount = filterSlotCount;

        this.filterHandler = new BaseItemStackHandler(filterSlotCount) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(IBasicFilterable.FILTER_ITEMS_TAG, this.serializeNBT());
                save();
                onFilterSlotChanged(slot);
            }
        };

        NBTTagCompound handlerTag = ItemNBTHelpers.getCompound(upgrade, IBasicFilterable.FILTER_ITEMS_TAG, false);
        if (handlerTag != null) filterHandler.deserializeNBT(handlerTag);
    }

    protected void onFilterSlotChanged(int slot) {
        // Override in advanced to manage target slots
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.refill_settings";
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
        save();
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }

    @Override
    public BaseItemStackHandler getFilterItems() {
        return filterHandler;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.WHITELIST;
    }

    @Override
    public void setFilterType(FilterType type) {
        // Refill upgrades always operate as whitelist
    }

    @Override
    public boolean allowsTargetSlotSelection() {
        return false;
    }

    @Override
    public boolean supportsBlockPick() {
        return false;
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (!isEnabled()) return false;
        if (player.worldObj.isRemote) return false;
        if (player.ticksExisted % COOLDOWN_TICKS != 0) return false;
        if (!hasAnyFilter()) return false;

        return refillForPlayer(player);
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (!isEnabled()) return false;
        if (world.isRemote) return false;
        if (world.getWorldTime() % COOLDOWN_TICKS != 0) return false;
        if (!hasAnyFilter()) return false;

        double range = BLOCK_RANGE;
        AxisAlignedBB aabb = AxisAlignedBB
            .getBoundingBox(pos.x - range, pos.y - range, pos.z - range, pos.x + range, pos.y + range, pos.z + range);

        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
        if (players.isEmpty()) return false;

        boolean changed = false;
        for (EntityPlayer player : players) {
            changed |= refillForPlayer(player);
        }
        return changed;
    }

    protected boolean refillForPlayer(EntityPlayer player) {
        boolean changed = false;

        for (int filterSlot = 0; filterSlot < filterSlotCount; filterSlot++) {
            ItemStack filter = filterHandler.getStackInSlot(filterSlot);
            if (filter == null) continue;

            TargetSlot targetSlot = getTargetSlotForFilter(filterSlot);
            changed |= tryRefillFilter(player, filter, targetSlot);
        }

        if (changed) {
            player.inventory.markDirty();
        }
        return changed;
    }

    protected TargetSlot getTargetSlotForFilter(int filterSlot) {
        return TargetSlot.ANY;
    }

    /**
     * Attempts to refill the target slot for a filter item from the backpack.
     * Always simulates first to prevent item duplication.
     */
    private boolean tryRefillFilter(EntityPlayer player, ItemStack filter, TargetSlot targetSlot) {
        // Check if player already has a full stack's worth of this item in the relevant scope
        int total = countMatchingInScope(player, filter, targetSlot);
        int maxStack = filter.getMaxStackSize();
        if (total >= maxStack) return false;

        int missingCount = targetSlot.getMissingCount(player, filter);
        if (missingCount <= 0) return false;

        // Cap extraction so total doesn't exceed one full stack
        missingCount = Math.min(missingCount, maxStack - total);

        IItemHandler backpackInv = storage;

        // Phase 1: Simulate extraction to find how much we can get
        int extractable = simulateExtract(backpackInv, filter, missingCount);
        if (extractable <= 0) return false;

        // Phase 2: Actually extract and fill
        ItemStack extracted = actualExtract(backpackInv, filter, extractable);
        if (extracted == null || extracted.stackSize <= 0) return false;

        ItemStack remaining = targetSlot.fill(player, extracted);

        // Phase 3: If any items couldn't be placed, put them back into the backpack
        if (remaining != null && remaining.stackSize > 0) {
            insertBack(backpackInv, remaining);
        }

        return true;
    }

    /**
     * Counts the total number of matching items in the relevant inventory scope.
     * For ANY target: counts entire mainInventory (slots 0-35) since items can go anywhere.
     * For hotbar-specific targets: counts only hotbar (slots 0-8).
     */
    private int countMatchingInScope(EntityPlayer player, ItemStack filter, TargetSlot targetSlot) {
        int total = 0;
        int range = (targetSlot == TargetSlot.ANY) ? player.inventory.mainInventory.length : 9;
        for (int i = 0; i < range; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null && ItemStackHelpers.areItemsEqualIgnoreDurability(stack, filter)) {
                total += stack.stackSize;
            }
        }
        return total;
    }

    /**
     * Simulates extracting items from the backpack to determine how many can be taken.
     */
    private int simulateExtract(IItemHandler handler, ItemStack filter, int maxCount) {
        int totalAvailable = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!ItemStackHelpers.areItemsEqualIgnoreDurability(stack, filter)) continue;
            totalAvailable += stack.stackSize;
            if (totalAvailable >= maxCount) return maxCount;
        }
        return totalAvailable;
    }

    /**
     * Actually extracts items from the backpack. Only removes the specific amount needed.
     */
    private ItemStack actualExtract(IItemHandler handler, ItemStack filter, int amount) {
        ItemStack result = filter.copy();
        result.stackSize = 0;
        int remaining = amount;

        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!ItemStackHelpers.areItemsEqualIgnoreDurability(stack, filter)) continue;

            int toTake = Math.min(remaining, stack.stackSize);
            ItemStack extracted = handler.extractItem(slot, toTake, false);
            if (extracted != null) {
                result.stackSize += extracted.stackSize;
                remaining -= extracted.stackSize;
            }
        }

        return result.stackSize > 0 ? result : null;
    }

    /**
     * Returns items back into the backpack if they couldn't be placed in the player's inventory.
     */
    private void insertBack(IItemHandler handler, ItemStack stack) {
        for (int slot = 0; slot < handler.getSlots() && stack.stackSize > 0; slot++) {
            stack = handler.insertItem(slot, stack, false);
            if (stack == null || stack.stackSize <= 0) return;
        }
    }

    protected boolean hasAnyFilter() {
        for (int i = 0; i < filterSlotCount; i++) {
            if (filterHandler.getStackInSlot(i) != null) return true;
        }
        return false;
    }
}
