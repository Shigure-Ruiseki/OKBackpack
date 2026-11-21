package ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane;

import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.backpack.modcompat.thaumcraft.helper.ThaumcraftHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.container.UpgradeContainer;
import ruiseki.okstoragecore.client.gui.slot.ModularCraftingSlot;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.inventory.InventoryCraftingWrapper;
import ruiseki.okstoragecore.upgrades.crafting.CraftingContainer;
import thaumcraft.api.crafting.IArcaneRecipe;

public class ArcaneContainer extends UpgradeContainer {

    private final InventoryCraftingWrapper inventory;
    private ModularCraftingSlot craftingSlot;

    public ArcaneContainer(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        Supplier<StorageContainer> containerSupplier, EntityPlayer player, int upgradeIndex, int width, int height,
        IItemHandlerModifiable craftingInventory) {
        this(
            storageWrapper,
            storagePanel,
            containerSupplier,
            player,
            upgradeIndex,
            width,
            height,
            craftingInventory,
            0);
    }

    public ArcaneContainer(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        Supplier<StorageContainer> containerSupplier, EntityPlayer player, int upgradeIndex, int width, int height,
        IItemHandlerModifiable craftingInventory, int startIndex) {
        super(storageWrapper, storagePanel, player, upgradeIndex);
        this.inventory = new InventoryCraftingWrapper(containerSupplier, width, height, craftingInventory, startIndex);
    }

    @Override
    public void detectAndSendChanges() {
        this.inventory.detectChanges();
    }

    @Override
    public void onAdded(UpgradeSlotUpdate update) {
        ModularCraftingSlot slot = update.get("arcane_result_slot");
        if (this.craftingSlot != null && this.craftingSlot != slot) {
            throw new IllegalStateException("Only one crafting output slot is supported with ArcaneContainer!");
        }
        this.craftingSlot = slot;
        slot.setContainer(this);
        this.inventory.notifyContainer();
    }

    @Override
    public void onCraftMatrixChanged(@NotNull IInventory inventoryIn) {
        if (!this.getPlayer().worldObj.isRemote) {
            this.craftingSlot.updateResult(this.findCraftingResult());
        }
    }

    @Override
    public void onTakeResult(EntityPlayer player, ItemStack stack) {
        this.consumeArcaneVis(player);

        ArcaneCraftingUpgradeWrapper upgrade = getUpgradeWrapper();
        boolean refill = upgrade.shouldRefillCraftingGrid();

        FMLCommonHandler.instance()
            .firePlayerCraftingEvent(player, stack, this.inventory);

        this.craftingSlot.onCrafting(stack);

        for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
            ItemStack ingredient = this.inventory.getStackInSlot(i);
            if (ItemHelpers.isEmpty(ingredient)) continue;

            ItemStack original = ItemHelpers.copy(ingredient);
            ItemStack containerItem = ItemHelpers.getContainerItem(original);

            boolean refilled = ItemHelpers.isEmpty(containerItem) && refill
                && upgrade.extractFromStorageOrPlayer(player, original);

            if (!refilled) {
                ItemStack remaining = ItemHelpers.copy(ingredient);
                ItemHelpers.shrink(remaining, 1);

                this.inventory.setSlot(i, ItemHelpers.isEmpty(remaining) ? ItemHelpers.EMPTY : remaining, false);
            }

            if (ItemHelpers.isEmpty(containerItem)) continue;

            ItemStack current = this.inventory.getStackInSlot(i);

            if (ItemHelpers.isEmpty(current)) {
                this.inventory.setSlot(i, containerItem, false);
            } else if (ItemHandlerHelpers.canItemStacksStack(current, containerItem)
                && ItemHelpers.getCount(current) + ItemHelpers.getCount(containerItem)
                    <= ItemHelpers.getMaxStackSize(current)) {

                        ItemHelpers.grow(current, ItemHelpers.getCount(containerItem));
                        this.inventory.setSlot(i, current, false);
                    } else
                if (!player.inventory.addItemStackToInventory(containerItem)) {
                    player.dropPlayerItemWithRandomChoice(containerItem, false);
                }
        }

        this.inventory.notifyContainer();
    }

    private void consumeArcaneVis(EntityPlayer player) {
        ArcaneCraftingUpgradeWrapper arcane = this.getUpgradeWrapper();
        Map<String, Integer> requiredAspects = arcane.getRequiredAspects();
        if (requiredAspects == null || requiredAspects.isEmpty()) return;
        ItemStack wandStack = arcane.getStorage()
            .getStackInSlot(9);
        if (wandStack == null) return;
        ThaumcraftHelpers.consumeAllVisCrafting(wandStack, player, requiredAspects);
    }

    protected ItemStack findCraftingResult() {
        EntityPlayer player = this.getPlayer();
        ArcaneCraftingUpgradeWrapper arcane = this.getUpgradeWrapper();

        ItemStack wand = arcane.getStorage()
            .getStackInSlot(9);

        boolean hasWand = ThaumcraftHelpers.isWand(wand);
        arcane.setHasWand(hasWand);

        if (hasWand) {
            IArcaneRecipe recipe = ThaumcraftHelpers.findArcaneRecipeIgnoringResearch(this.inventory, player);

            if (recipe != null) {
                Map<String, Integer> aspects = ThaumcraftHelpers.getArcaneRecipeAspects(recipe);

                arcane.setRequiredAspects(aspects);

                String research = recipe.getResearch();
                boolean researchComplete = research == null || research.isEmpty()
                    || ThaumcraftHelpers.isResearchComplete(player, research);

                if (!researchComplete) {
                    arcane.setMissingResearch(research);
                    arcane.setMissingResearchName(ThaumcraftHelpers.getResearchDisplayName(research));

                    return ItemHelpers.EMPTY;
                }

                arcane.setMissingResearch("");
                arcane.setMissingResearchName("");

                return recipe.getCraftingResult(this.inventory);
            }
        }

        arcane.setRequiredAspects(null);
        arcane.setMissingResearch("");
        arcane.setMissingResearchName("");

        return CraftingContainer.findCraftingResult(this.inventory, this.getPlayer().worldObj);
    }

    public ArcaneCraftingUpgradeWrapper getUpgradeWrapper() {
        return (ArcaneCraftingUpgradeWrapper) super.getUpgradeWrapper();
    }
}
