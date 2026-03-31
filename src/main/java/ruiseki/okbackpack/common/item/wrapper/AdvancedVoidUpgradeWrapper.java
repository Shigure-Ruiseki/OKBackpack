package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedVoidUpgradeWrapper extends AdvancedUpgradeWrapper implements IVoidUpgrade {

    public AdvancedVoidUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public boolean canVoid(ItemStack stack) {
        return checkFilter(stack);
    }

    @Override
    public VoidType getVoidType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, VOID_TYPE_TAG, VoidType.OVERFLOW.ordinal());
        VoidType[] types = VoidType.values();
        if (ordinal < 0 || ordinal >= types.length) {
            return VoidType.OVERFLOW;
        }
        return types[ordinal];
    }

    @Override
    public void setVoidType(VoidType type) {
        if (type == null) {
            type = VoidType.OVERFLOW;
        }
        ItemNBTHelpers.setInt(upgrade, VOID_TYPE_TAG, type.ordinal());
    }

    @Override
    public VoidInput getVoidInput() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, VOID_INPUT_TAG, VoidInput.ALL.ordinal());
        VoidInput[] types = VoidInput.values();
        if (ordinal < 0 || ordinal >= types.length) {
            return VoidInput.ALL;
        }
        return types[ordinal];
    }

    @Override
    public void setVoidInput(VoidInput type) {
        if (type == null) {
            type = VoidInput.ALL;
        }
        ItemNBTHelpers.setInt(upgrade, VOID_INPUT_TAG, type.ordinal());
    }
}
