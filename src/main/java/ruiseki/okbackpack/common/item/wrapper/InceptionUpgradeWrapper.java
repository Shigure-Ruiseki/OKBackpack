package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ISlotModifiable;
import ruiseki.okbackpack.common.block.BlockBackpack;

public class InceptionUpgradeWrapper extends UpgradeWrapperBase implements ISlotModifiable {

    public InceptionUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public boolean canRemoveUpgrade(int slotIndex) {

        boolean containsBackpack = false;
        for (ItemStack stack : storage.getStacks()) {
            if (stack != null && stack.getItem() instanceof BlockBackpack.ItemBackpack) {
                containsBackpack = true;
                break;
            }
        }

        if (!containsBackpack) return true;

        int count = 0;
        for (ItemStack stack : storage.getUpgradeHandler()
            .getStacks()) {
            if (stack != null && stack.getItem() == upgrade.getItem()) {
                count++;
            }
        }

        return count > 1;
    }

    @Override
    public boolean canAddStack(int slot, ItemStack stack) {
        return stack != null && stack.getItem() instanceof BlockBackpack.ItemBackpack;
    }
}
