package ruiseki.okstoragecore.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.IUpgradeItem;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;

public class ModularUpgradeSlot extends ModularItemSlot {

    protected final StoragePanel panel;

    public ModularUpgradeSlot(int index, StoragePanel panel) {
        super(
            panel.getWrapper()
                .getUpgradeHandler(),
            index);
        this.panel = panel;
    }

    public StoragePanel getPanel() {
        return panel;
    }

    public IStorageWrapper getWrapper() {
        return panel.getWrapper();
    }

    @Override
    public com.cleanroommc.modularui.utils.item.IItemHandler getItemHandler() {
        return this.getWrapper()
            .getUpgradeHandler();
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        if (ItemHelpers.isEmpty(stack) || !this.getItemHandler()
            .isItemValid(this.getSlotIndex(), stack)) {
            return false;
        }
        UpgradeSlotChangeResult result;
        if (ItemHelpers.isEmpty(this.getStack())) {
            result = ((IUpgradeItem<?>) ItemHelpers.getItem(stack))
                .canAddUpgradeTo(this.getWrapper(), stack, this.getPlayer());
        } else if (ItemHelpers.getCount(stack) > 1) {
            return false;
        } else {
            result = ((IUpgradeItem<?>) ItemHelpers.getItem(this.getStack()))
                .canSwapUpgradeFor(stack, this.getSlotIndex(), this.getWrapper(), this.getPlayer());
        }

        this.getPanel()
            .updateSlotChangeError(result);
        return result.isSuccessful();
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        if (!super.canTakeStack(player)) {
            return false;
        }

        ItemStack stack = this.getStack();
        if (ItemHelpers.isEmpty(stack) || !(ItemHelpers.getItem(stack) instanceof IUpgradeItem<?>upgradeItem)) {
            return false;
        }

        UpgradeSlotChangeResult result = upgradeItem.canRemoveUpgradeFrom(this.getWrapper(), player);

        this.getPanel()
            .updateSlotChangeError(result);
        return result.isSuccessful();
    }

    public boolean canSwapStack(EntityPlayer player, ItemStack stackToPut) {
        boolean ret = super.canTakeStack(player);
        if (!ret || ItemHelpers.getCount(stackToPut) > 1) {
            return false;
        }
        UpgradeSlotChangeResult result = ((IUpgradeItem<?>) ItemHelpers.getItem(this.getStack()))
            .canSwapUpgradeFor(stackToPut, this.getSlotIndex(), this.getWrapper(), player);
        this.getPanel()
            .updateSlotChangeError(result);
        return result.isSuccessful();
    }

    @Override
    public void putStack(@Nullable ItemStack stack) {
        int oldColumnsTaken = getColumnsTaken(this.getStack());
        super.putStack(stack);
        this.updateColumnsTaken(oldColumnsTaken);
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        int oldColumnsTaken = getColumnsTaken(this.getStack());
        ItemStack extracted = super.decrStackSize(amount);
        this.updateColumnsTaken(oldColumnsTaken);
        return extracted;
    }

    private int getColumnsTaken(@Nullable ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return 0;

        if (ItemHelpers.getItem(stack) instanceof IUpgradeItem<?>upgrade) {
            return Math.max(0, upgrade.getInventoryColumnsTaken());
        }

        return 0;
    }

    private void updateColumnsTaken(int oldColumnsTaken) {
        int newColumnsTaken = getColumnsTaken(this.getStack());
        int delta = newColumnsTaken - oldColumnsTaken;
        if (delta == 0) return;
        IStorageWrapper wrapper = this.getWrapper();
        int totalColumnsTaken = Math.max(0, wrapper.getColumnsTaken() + delta);
        wrapper.setColumnsTaken(totalColumnsTaken, true);
    }
}
