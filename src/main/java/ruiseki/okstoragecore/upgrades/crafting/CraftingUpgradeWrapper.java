package ruiseki.okstoragecore.upgrades.crafting;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.upgrades.ISlotClickUpgrade;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class CraftingUpgradeWrapper extends UpgradeWrapperBase<CraftingUpgradeWrapper, ItemCraftingUpgrade>
    implements IStorageUpgrade, ISlotClickUpgrade {

    private final ItemStackHandler inventory;

    protected CraftingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);

        this.inventory = new ItemStackHandler(10) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                NBTHelpers.setCompoundTag(upgrade, "craftingInventory", this.serializeNBT());
                save();
            }
        };
        NBTHelpers.getCompoundTag(upgrade, "craftingInventory")
            .ifPresent(this.inventory::deserializeNBT);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.okstoragecore.upgrades.crafting";
    }

    @Override
    public ItemStackHandler getStorage() {
        return inventory;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    public boolean shouldShiftClickIntoStorage() {
        return NBTHelpers.getBoolean(upgrade, "shiftClickIntoStorage")
            .orElse(true);
    }

    public void setShiftClickIntoStorage(boolean shiftClickIntoStorage) {
        NBTHelpers.setBoolean(upgrade, "shiftClickIntoStorage", shiftClickIntoStorage);
        this.save();
    }

    public boolean shouldRefillCraftingGrid() {
        return NBTHelpers.getBoolean(upgrade, "refill_crafting_grid")
            .orElse(false);
    }

    public void setRefillCraftingGrid(boolean replenish) {
        NBTHelpers.setBoolean(upgrade, "refill_crafting_grid", replenish);
        this.save();
    }

    public boolean extractFromStorageOrPlayer(EntityPlayer player, ItemStack stack) {
        return this.extractFromStorage(stack) || this.extractFromPlayer(player, stack);
    }

    private boolean extractFromPlayer(EntityPlayer player, ItemStack stack) {
        int playerInvMatchingIndex = InventoryHelpers.findSlotMatchingItem(player, stack);
        if (playerInvMatchingIndex >= 0) {
            player.inventory.decrStackSize(playerInvMatchingIndex, 1);
            return true;
        }
        return false;
    }

    private boolean extractFromStorage(ItemStack stack) {
        return !ItemHelpers.isEmpty(
            InventoryHelpers.extractFromInventory(
                s -> ItemHandlerHelpers.canItemStacksStack(s, stack),
                1,
                storageWrapper.getInventoryHandler(),
                false));
    }

    @Override
    public boolean transferItem(EntityPlayer player, ModularSlot slot, ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return false;

        if (this.shouldShiftClickIntoStorage()) {
            ItemStack remaining = InventoryHelpers
                .insertIntoInventory(stack, storageWrapper.getInventoryHandler(), false);
            int remainingSize = ItemHelpers.isEmpty(remaining) ? 0 : remaining.stackSize;
            stack.stackSize = remainingSize;
            StorageContainer.updateSlotStack(slot, remainingSize <= 0 ? ItemHelpers.EMPTY : stack);
            return remainingSize <= 0;
        }
        return false;
    }
}
