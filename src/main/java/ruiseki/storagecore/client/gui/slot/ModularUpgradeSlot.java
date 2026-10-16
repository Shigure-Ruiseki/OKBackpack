package ruiseki.storagecore.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.upgrades.IUpgradeItem;
import ruiseki.storagecore.upgrades.UpgradeSlotChangeResult;

public class ModularUpgradeSlot extends ModularItemSlot {

    protected final IStorageWrapper wrapper;
    protected final StoragePanel panel;

    public ModularUpgradeSlot(IItemHandler itemHandler, int index, IStorageWrapper wrapper, StoragePanel panel) {
        super(itemHandler, index);
        this.wrapper = wrapper;
        this.panel = panel;
    }

    public IStorageWrapper getWrapper() {
        return this.wrapper;
    }

    public StoragePanel getPanel() {
        return this.panel;
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        if (ItemHelpers.isEmpty(stack) || !this.getItemHandler()
            .isItemValid(this.getSlotIndex(), stack)) {
            return false;
        }
        UpgradeSlotChangeResult result;
        if (ItemHelpers.isEmpty(this.getStack())) {
            result = ((IUpgradeItem<?>) stack.getItem()).canAddUpgradeTo(this.getWrapper(), stack);
        } else if (ItemHelpers.getCount(stack) > 1) {
            return false;
        } else {
            result = ((IUpgradeItem<?>) this.getStack()
                .getItem()).canSwapUpgradeFor(stack, this.getWrapper());
        }

        this.getPanel()
            .updateSlotChangeError(result);
        return result.isSuccessful();
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        boolean ret = super.canTakeStack(player);
        if (!ret) {
            return false;
        }

        UpgradeSlotChangeResult result = ((IUpgradeItem<?>) this.getStack()
            .getItem()).canRemoveUpgradeFrom(this.getWrapper());
        this.getPanel()
            .updateSlotChangeError(result);
        return result.isSuccessful();
    }

    public boolean canSwapStack(EntityPlayer player, ItemStack stackToPut) {
        boolean ret = super.canTakeStack(player);
        if (!ret || ItemHelpers.getCount(stackToPut) > 1) {
            return false;
        }
        UpgradeSlotChangeResult result = ((IUpgradeItem<?>) this.getStack()
            .getItem()).canSwapUpgradeFor(stackToPut, this.getWrapper());
        this.getPanel()
            .updateSlotChangeError(result);
        return result.isSuccessful();
    }

}
