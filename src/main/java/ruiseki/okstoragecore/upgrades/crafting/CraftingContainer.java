package ruiseki.okstoragecore.upgrades.crafting;

import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.common.FMLCommonHandler;
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

public class CraftingContainer extends UpgradeContainer {

    private final InventoryCraftingWrapper inventory;
    private ModularCraftingSlot craftingSlot;

    public CraftingContainer(IStorageWrapper storageWrapper, StoragePanel storagePanel,
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

    public CraftingContainer(IStorageWrapper storageWrapper, StoragePanel storagePanel,
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
        ModularCraftingSlot slot = update.get("crafting_result_slot");
        if (this.craftingSlot != null && this.craftingSlot != slot) {
            throw new IllegalStateException(
                "Only one crafting output slot is supported with CraftingModularContainer!");
        }
        this.craftingSlot = slot;
        slot.setContainer(this);
    }

    @Override
    public void onCraftMatrixChanged(@NotNull IInventory inventoryIn) {
        if (!this.getPlayer().worldObj.isRemote) {
            this.craftingSlot.updateResult(
                CraftingManager.getInstance()
                    .findMatchingRecipe(this.inventory, this.getPlayer().worldObj));
        }
    }

    @Override
    public void onTakeResult(EntityPlayer player, ItemStack stack) {
        CraftingUpgradeWrapper upgrade = getUpgradeWrapper();
        boolean refill = upgrade.shouldRefillCraftingGrid();

        FMLCommonHandler.instance()
            .firePlayerCraftingEvent(player, stack, this.inventory);

        this.craftingSlot.onCrafting(stack);

        for (int i = 0; i < this.inventory.getSizeInventory() - 1; i++) {
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
                    <= ItemHelpers.getItemStackSize(current)) {

                        ItemHelpers.grow(current, ItemHelpers.getCount(containerItem));
                        this.inventory.setSlot(i, current, false);
                    } else
                if (!player.inventory.addItemStackToInventory(containerItem)) {
                    player.dropPlayerItemWithRandomChoice(containerItem, false);
                }
        }

        inventory.notifyContainer();
    }

    public CraftingUpgradeWrapper getUpgradeWrapper() {
        return (CraftingUpgradeWrapper) super.getUpgradeWrapper();
    }
}
