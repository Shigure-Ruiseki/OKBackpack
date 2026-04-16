package ruiseki.okbackpack.client.gui.handler;

import net.minecraft.inventory.Container;

import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade.CraftingDestination;
import ruiseki.okcore.item.IItemHandlerModifiable;

public class IndexedInventoryCraftingWrapper extends InventoryCraftingWrapper {

    private final int upgradeSlotIndex;
    private CraftingDestination craftingDestination = CraftingDestination.INVENTORY;

    public IndexedInventoryCraftingWrapper(int upgradeSlotIndex, Container cont, int width, int height,
        IItemHandlerModifiable delegate, int startIndex) {
        super(cont, width, height, delegate, startIndex);
        this.upgradeSlotIndex = upgradeSlotIndex;
    }

    public int getUpgradeSlotIndex() {
        return upgradeSlotIndex;
    }

    public CraftingDestination getCraftingDestination() {
        return craftingDestination;
    }

    public void setCraftingDestination(CraftingDestination craftingDestination) {
        this.craftingDestination = craftingDestination;
    }
}
