package ruiseki.okstoragecore.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.stats.AchievementList;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.inventory.InventoryCraftingWrapper;
import ruiseki.okstoragecore.upgrades.crafting.CraftingUpgradeWrapper;

public class ModularCraftingSlot extends IndexedModularSlot {

    private InventoryCraftingWrapper craftMatrix;
    private int amountCrafted;

    public ModularCraftingSlot(StoragePanel storagePanel, int upgradeIndex) {
        this(new ItemStackHandler(1), storagePanel, upgradeIndex);
    }

    private ModularCraftingSlot(ItemStackHandler resultHandler, StoragePanel storagePanel, int upgradeIndex) {
        super(resultHandler, 0, storagePanel, upgradeIndex);
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
        this.amountCrafted += Math.min(amount, ItemHelpers.getCount(getStack()));
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
    protected void onCrafting(ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return;

        EntityPlayer player = getPlayer();
        stack.onCrafting(player.worldObj, player, this.amountCrafted);
        this.amountCrafted = 0;

        Item item = ItemHelpers.getItem(stack);

        if (item == Item.getItemFromBlock(Blocks.crafting_table)) {
            player.addStat(AchievementList.buildWorkBench, 1);
        }

        if (item instanceof ItemPickaxe) {
            player.addStat(AchievementList.buildPickaxe, 1);

            if (((ItemPickaxe) item).func_150913_i() != Item.ToolMaterial.WOOD) {
                player.addStat(AchievementList.buildBetterPickaxe, 1);
            }
        }

        if (item == Item.getItemFromBlock(Blocks.furnace)) {
            player.addStat(AchievementList.buildFurnace, 1);
        }

        if (item instanceof ItemHoe) {
            player.addStat(AchievementList.buildHoe, 1);
        }

        if (item == Items.bread) {
            player.addStat(AchievementList.makeBread, 1);
        }

        if (item == Items.cake) {
            player.addStat(AchievementList.bakeCake, 1);
        }

        if (item instanceof ItemSword) {
            player.addStat(AchievementList.buildSword, 1);
        }

        if (item == Item.getItemFromBlock(Blocks.enchanting_table)) {
            player.addStat(AchievementList.enchantments, 1);
        }

        if (item == Item.getItemFromBlock(Blocks.bookshelf)) {
            player.addStat(AchievementList.bookcase, 1);
        }
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        if (this.getCraftMatrix() == null || ItemHelpers.isEmpty(stack)) return;

        CraftingUpgradeWrapper upgrade = getUpgradeWrapper();
        boolean refill = upgrade.shouldRefillCraftingGrid();

        FMLCommonHandler.instance()
            .firePlayerCraftingEvent(player, stack, this.getCraftMatrix());

        onCrafting(stack);

        for (int i = 0; i < this.getCraftMatrix()
            .getSizeInventory() - 1; i++) {
            ItemStack ingredient = this.getCraftMatrix()
                .getStackInSlot(i);
            if (ItemHelpers.isEmpty(ingredient)) continue;

            ItemStack original = ItemHelpers.copy(ingredient);
            ItemStack containerItem = ItemHelpers.getContainerItem(original);

            boolean refilled = ItemHelpers.isEmpty(containerItem) && refill
                && upgrade.extractFromStorageOrPlayer(player, original);

            if (!refilled) {
                ItemStack remaining = ItemHelpers.copy(ingredient);
                ItemHelpers.shrink(remaining, 1);

                this.getCraftMatrix()
                    .setSlot(i, ItemHelpers.isEmpty(remaining) ? ItemHelpers.EMPTY : remaining, false);
            }

            if (ItemHelpers.isEmpty(containerItem)) continue;

            ItemStack current = this.getCraftMatrix()
                .getStackInSlot(i);

            if (ItemHelpers.isEmpty(current)) {
                this.getCraftMatrix()
                    .setSlot(i, containerItem, false);
            } else if (ItemHandlerHelpers.canItemStacksStack(current, containerItem)
                && ItemHelpers.getCount(current) + ItemHelpers.getCount(containerItem)
                    <= ItemHelpers.getItemStackSize(current)) {

                        ItemHelpers.grow(current, ItemHelpers.getCount(containerItem));
                        this.getCraftMatrix()
                            .setSlot(i, current, false);
                    } else
                if (!player.inventory.addItemStackToInventory(containerItem)) {
                    player.dropPlayerItemWithRandomChoice(containerItem, false);
                }
        }

        craftMatrix.notifyContainer();
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
