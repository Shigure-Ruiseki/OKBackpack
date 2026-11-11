package ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ISlotClickUpgrade;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class ArcaneCraftingUpgradeWrapper
    extends UpgradeWrapperBase<ArcaneCraftingUpgradeWrapper, ItemArcaneCraftingUpgrade>
    implements IStorageUpgrade, ISlotClickUpgrade {

    String ARCANE_ASPECTS_TAG = "ArcaneAspects";

    private final ItemStackHandler inventory;
    private boolean hasWand;
    private String missingResearch = "";
    private String missingResearchName = "";

    public ArcaneCraftingUpgradeWrapper(IStorageWrapper storage, ItemStack upgrade,
        Consumer<ItemStack> upgradeConsumer) {
        super(storage, upgrade, upgradeConsumer);
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
    public IItemHandlerModifiable getStorage() {
        return this.inventory;
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
    public ItemStack transferItem(EntityPlayer player, ItemStack stack) {
        if (ItemHelpers.isEmpty(stack) || !this.shouldShiftClickIntoStorage()) return stack;
        stack = InventoryHelpers.insertIntoInventory(stack, storageWrapper.getInventoryHandler(), false);
        return stack;
    }

    public Map<String, Integer> getRequiredAspects() {
        Map<String, Integer> result = new LinkedHashMap<>();
        NBTHelpers.getCompoundTag(upgrade, ARCANE_ASPECTS_TAG)
            .ifPresent(tag -> {
                for (String aspectTag : tag.func_150296_c()) {
                    result.put(aspectTag, tag.getInteger(aspectTag));
                }
            });
        return result;
    }

    public void setRequiredAspects(Map<String, Integer> aspects) {
        NBTTagCompound tag = new NBTTagCompound();
        if (aspects != null) {
            for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
                tag.setInteger(entry.getKey(), entry.getValue());
            }
        }
        NBTHelpers.setCompoundTag(upgrade, ARCANE_ASPECTS_TAG, tag);
        this.save();
    }

    public boolean hasWand() {
        return hasWand;
    }

    public void setHasWand(boolean hasWand) {
        this.hasWand = hasWand;
    }

    public String getMissingResearch() {
        return missingResearch;
    }

    public void setMissingResearch(String researchKey) {
        this.missingResearch = researchKey;
    }

    public String getMissingResearchName() {
        return missingResearchName;
    }

    public void setMissingResearchName(String name) {
        this.missingResearchName = name;
    }

    public Iterable<ItemStack> getVisChargeableStacks() {
        ItemStack wandStack = this.inventory.getStackInSlot(9);
        return ItemHelpers.isEmpty(wandStack) ? Collections.emptyList() : Collections.singletonList(wandStack);
    }
}
