package ruiseki.okbackpack.common.item.inception;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ISlotModifiable;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class InceptionUpgradeWrapper extends UpgradeWrapperBase implements ISlotModifiable {

    public InceptionUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean canRemoveUpgrade(int slotIndex) {

        boolean containsBackpack = false;
        for (ItemStack stack : storage.getStacks()) {
            if (BackpackEntityHelper.isBackpackStack(stack, false)) {
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
        if (!BackpackEntityHelper.isBackpackStack(stack, false)) {
            return true;
        }

        if (storage instanceof BackpackWrapper wrapper) {
            if (stack == wrapper.getBackpack()) {
                return false;
            }
            return !BackpackEntityHelper.isSameBackpack(stack, wrapper.uuid);
        }

        return true;
    }
}
