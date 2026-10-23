package ruiseki.okstoragecore.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.stats.AchievementList;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.inventory.InventoryCraftingWrapper;
import ruiseki.okstoragecore.upgrades.crafting.CraftingUpgradeWrapper;

public class ModularCraftingSlot extends IndexedModularSlot {

    private InventoryCraftingWrapper craftMatrix;
    private int amountCrafted;

    public ModularCraftingSlot(IStorageWrapper storageWrapper, StoragePanel storagePanel, int upgradeIndex) {
        this(new ItemStackHandler(1), storageWrapper, storagePanel, upgradeIndex);
    }

    private ModularCraftingSlot(ItemStackHandler resultHandler, IStorageWrapper storageWrapper,
        StoragePanel storagePanel, int upgradeIndex) {
        super(resultHandler, 0, storageWrapper, storagePanel, upgradeIndex);
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        return false;
    }

    @Override
    public boolean canDragIntoSlot() {
        return false;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack decrStackSize(int amount) {
        if (this.getHasStack()) {
            this.amountCrafted += Math.min(amount, this.getStack().stackSize);
        }
        return super.decrStackSize(amount);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        this.amountCrafted += amount;
        this.onCrafting(stack);
    }

    @Override
    public void onCraftShiftClick(EntityPlayer player, ItemStack stack) {
        if (!ItemHelpers.isEmpty(stack)) {
            player.dropPlayerItemWithRandomChoice(stack, false);
        }
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    @Override
    protected void onCrafting(ItemStack p_75208_1_) {
        p_75208_1_.onCrafting(getPlayer().worldObj, getPlayer(), this.amountCrafted);
        this.amountCrafted = 0;

        if (p_75208_1_.getItem() == Item.getItemFromBlock(Blocks.crafting_table)) {
            getPlayer().addStat(AchievementList.buildWorkBench, 1);
        }

        if (p_75208_1_.getItem() instanceof ItemPickaxe) {
            getPlayer().addStat(AchievementList.buildPickaxe, 1);
        }

        if (p_75208_1_.getItem() == Item.getItemFromBlock(Blocks.furnace)) {
            getPlayer().addStat(AchievementList.buildFurnace, 1);
        }

        if (p_75208_1_.getItem() instanceof ItemHoe) {
            getPlayer().addStat(AchievementList.buildHoe, 1);
        }

        if (p_75208_1_.getItem() == Items.bread) {
            getPlayer().addStat(AchievementList.makeBread, 1);
        }

        if (p_75208_1_.getItem() == Items.cake) {
            getPlayer().addStat(AchievementList.bakeCake, 1);
        }

        if (p_75208_1_.getItem() instanceof ItemPickaxe
            && ((ItemPickaxe) p_75208_1_.getItem()).func_150913_i() != Item.ToolMaterial.WOOD) {
            getPlayer().addStat(AchievementList.buildBetterPickaxe, 1);
        }

        if (p_75208_1_.getItem() instanceof ItemSword) {
            getPlayer().addStat(AchievementList.buildSword, 1);
        }

        if (p_75208_1_.getItem() == Item.getItemFromBlock(Blocks.enchanting_table)) {
            getPlayer().addStat(AchievementList.enchantments, 1);
        }

        if (p_75208_1_.getItem() == Item.getItemFromBlock(Blocks.bookshelf)) {
            getPlayer().addStat(AchievementList.bookcase, 1);
        }
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        if (this.craftMatrix == null || ItemHelpers.isEmpty(stack)) {
            return;
        }

        CraftingUpgradeWrapper upgrade = this.getUpgradeWrapper();
        boolean refill = upgrade.shouldRefillCraftingGrid();

        FMLCommonHandler.instance()
            .firePlayerCraftingEvent(player, stack, craftMatrix);

        this.onCrafting(stack);

        for (int i = 0; i < craftMatrix.getSizeInventory() - 1; i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);

            if (ItemHelpers.isEmpty(ingredient)) {
                continue;
            }

            ItemStack original = ingredient.copy();
            Item item = original.getItem();

            // Determine the container item before consuming.
            ItemStack containerItem = item.hasContainerItem(original) ? item.getContainerItem(original) : null;

            // Refill from storage/player only when no container item must be returned.
            boolean refilled = ItemHelpers.isEmpty(containerItem) && refill
                && upgrade.extractFromStorageOrPlayer(player, original);

            if (!refilled) {
                ItemStack remaining = ingredient.copy();
                ItemHelpers.shrink(remaining, 1);

                if (remaining.stackSize <= 0) {
                    remaining = ItemHelpers.EMPTY;
                }

                this.craftMatrix.setSlot(i, remaining, false);
            }

            if (!ItemHelpers.isEmpty(containerItem)) {
                ItemStack current = this.craftMatrix.getStackInSlot(i);

                if (ItemHelpers.isEmpty(current)) {
                    this.craftMatrix.setSlot(i, containerItem, false);
                } else if (ItemHandlerHelpers.canItemStacksStack(current, containerItem)
                    && current.stackSize + containerItem.stackSize <= current.getMaxStackSize()) {
                        ItemHelpers.grow(current, containerItem.stackSize);
                        this.craftMatrix.setSlot(i, current, false);
                    } else if (!player.inventory.addItemStackToInventory(containerItem)) {
                        player.dropPlayerItemWithRandomChoice(containerItem, false);
                    }
            }
        }
        this.craftMatrix.notifyContainer();
    }

    public void updateResult(ItemStack stack) {
        this.putStack(stack);
        this.getSyncHandler()
            .forceSyncItem();
    }

    public void setCraftMatrix(InventoryCraftingWrapper craftMatrix) {
        this.craftMatrix = craftMatrix;
    }

    public InventoryCraftingWrapper getCraftMatrix() {
        return craftMatrix;
    }

    public CraftingUpgradeWrapper getUpgradeWrapper() {
        return (CraftingUpgradeWrapper) super.getUpgradeWrapper();
    }
}
