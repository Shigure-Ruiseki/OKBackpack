
package ruiseki.okstoragecore.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.client.gui.container.UpgradeContainer;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class ModularResultSlot extends IndexedModularSlot {

    private UpgradeContainer container;

    public ModularResultSlot(StoragePanel storagePanel, int upgradeIndex) {
        this(new ItemStackHandler(1), storagePanel, upgradeIndex);
    }

    private ModularResultSlot(ItemStackHandler resultHandler, StoragePanel storagePanel, int upgradeIndex) {
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

    public void setContainer(UpgradeContainer container) {
        this.container = container;
    }

    public void updateResult(ItemStack stack) {
        this.putStack(ItemHelpers.isEmpty(stack) ? ItemHelpers.EMPTY : stack);
        this.getSyncHandler()
            .forceSyncItem();
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        if (this.container == null || ItemHelpers.isEmpty(stack, false) || player == null) return;
        this.container.onTakeResult(player, stack);
    }

    @Override
    public void onCrafting(ItemStack stack) {
        super.onCrafting(stack);
    }

    @Override
    public void onCrafting(ItemStack stack, int amount) {
        super.onCrafting(stack, amount);
    }
}
