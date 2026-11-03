package ruiseki.backpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.upgrades.anvil.AnvilContainer;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class AnvilResultSlot extends IndexedModularSlot {

    private AnvilContainer anvilContainer;

    public AnvilResultSlot(StoragePanel storagePanel, int upgradeIndex) {
        this(new ItemStackHandler(1), 0, storagePanel, upgradeIndex);
    }

    public AnvilResultSlot(IItemHandlerModifiable handler, int index, StoragePanel storagePanel, int upgradeIndex) {
        super(handler, index, storagePanel, upgradeIndex);
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        return false;
    }

    @Override
    public boolean canDragIntoSlot() {
        return false;
    }

    public void setAnvilContainer(AnvilContainer container) {
        this.anvilContainer = container;
    }

    public void updateResult(ItemStack stack) {
        putStack(ItemHelpers.isEmpty(stack) ? ItemHelpers.EMPTY : stack);
        getSyncHandler().forceSyncItem();
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        if (anvilContainer == null || ItemHelpers.isEmpty(stack)) {
            return;
        }

        int cost = anvilContainer.getMaximumCost();

        if (!player.capabilities.isCreativeMode) {
            player.addExperienceLevel(-cost);
        }

        IItemHandlerModifiable handler = (IItemHandlerModifiable) getItemHandler();

        handler.setStackInSlot(0, ItemHelpers.EMPTY);
        ItemStack right = handler.getStackInSlot(1);
        if (!ItemHelpers.isEmpty(right)) {
            int consume = Math.max(1, anvilContainer.getStackSizeToBeUsedInRepair());

            ItemStack remaining = ItemHelpers.copy(right);
            ItemHelpers.shrink(remaining, consume);

            handler.setStackInSlot(1, ItemHelpers.isEmpty(remaining) ? ItemHelpers.EMPTY : remaining);
        }

        handler.setStackInSlot(2, ItemHelpers.EMPTY);

        anvilContainer.updateRepairOutput();
    }
}
