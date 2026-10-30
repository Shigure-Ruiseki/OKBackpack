package ruiseki.okstoragecore.upgrades.crafting;

import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.CraftingManager;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.container.UpgradeContainer;
import ruiseki.okstoragecore.client.gui.slot.ModularCraftingSlot;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.inventory.InventoryCraftingWrapper;

public class CraftingContainer extends UpgradeContainer {

    private final InventoryCraftingWrapper inventoryCrafting;
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
        this.inventoryCrafting = new InventoryCraftingWrapper(
            containerSupplier,
            width,
            height,
            craftingInventory,
            startIndex);
    }

    public InventoryCraftingWrapper getInventoryCrafting() {
        return inventoryCrafting;
    }

    public ModularCraftingSlot getCraftingSlot() {
        return craftingSlot;
    }

    @Override
    public void detectAndSendChanges() {
        this.inventoryCrafting.detectChanges();
    }

    @Override
    public void onAdded(UpgradeSlotUpdate update) {
        ModularCraftingSlot slot = update.get("crafting_result_slot");
        if (this.craftingSlot != null && this.craftingSlot != slot) {
            throw new IllegalStateException(
                "Only one crafting output slot is supported with CraftingModularContainer!");
        }
        this.craftingSlot = slot;
        slot.setCraftMatrix(this.inventoryCrafting);
    }

    @Override
    public void onCraftMatrixChanged(@NotNull IInventory inventoryIn) {
        if (!this.getPlayer().worldObj.isRemote) {
            this.craftingSlot.updateResult(
                CraftingManager.getInstance()
                    .findMatchingRecipe(this.inventoryCrafting, this.getPlayer().worldObj));
        }
    }
}
