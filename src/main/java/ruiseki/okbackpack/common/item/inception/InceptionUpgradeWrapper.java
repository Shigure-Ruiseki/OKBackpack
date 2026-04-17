package ruiseki.okbackpack.common.item.inception;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.api.wrapper.IInceptionUpgrade;
import ruiseki.okbackpack.api.wrapper.ISlotModifiable;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class InceptionUpgradeWrapper extends UpgradeWrapperBase implements ISlotModifiable, IInceptionUpgrade {

    public InceptionUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
        save();
    }

    @Override
    public boolean canRemoveUpgrade(int slotIndex) {
        return getRemoveUpgradeResult(slotIndex).isSuccessful();
    }

    @Override
    public UpgradeSlotChangeResult getRemoveUpgradeResult(int slotIndex) {
        int[] nestedBackpackSlots = findNestedBackpackSlots();
        if (nestedBackpackSlots.length == 0) {
            return UpgradeSlotChangeResult.success();
        }

        return countInstalledInceptionUpgrades() > 1 ? UpgradeSlotChangeResult.success()
            : UpgradeSlotChangeResult.failInceptionSubBackpack(nestedBackpackSlots);
    }

    @Override
    public UpgradeSlotChangeResult getReplaceUpgradeResult(int slotIndex, ItemStack replacement) {
        int[] nestedBackpackSlots = findNestedBackpackSlots();
        if (nestedBackpackSlots.length == 0 || replacement == null) {
            return UpgradeSlotChangeResult.success();
        }

        if (replacement.getItem() == upgrade.getItem()) {
            return UpgradeSlotChangeResult.success();
        }

        return countInstalledInceptionUpgrades() > 1 ? UpgradeSlotChangeResult.success()
            : UpgradeSlotChangeResult.failInceptionSubBackpack(nestedBackpackSlots);
    }

    @Override
    public boolean canAddStack(int slot, ItemStack stack) {
        if (!BackpackEntityHelper.isBackpackStack(stack, false)) {
            return true;
        }

        if (!isEnabled()) {
            return false;
        }

        if (storage instanceof BackpackWrapper wrapper) {
            if (stack == wrapper.getBackpack()) {
                return false;
            }
            return !BackpackEntityHelper.isSameBackpack(stack, wrapper.uuid);
        }

        return true;
    }

    private int[] findNestedBackpackSlots() {
        List<Integer> nestedBackpackSlots = new ArrayList<>();
        for (int slot = 0; slot < storage.getSlots(); slot++) {
            ItemStack stack = storage.getStackInSlot(slot);
            if (BackpackEntityHelper.isBackpackStack(stack, false)) {
                nestedBackpackSlots.add(slot);
            }
        }

        return nestedBackpackSlots.stream()
            .mapToInt(Integer::intValue)
            .toArray();
    }

    private int countInstalledInceptionUpgrades() {
        int count = 0;
        for (ItemStack stack : storage.getUpgradeHandler()
            .getStacks()) {
            if (stack != null && stack.getItem() == upgrade.getItem()) {
                count++;
            }
        }
        return count;
    }
}
