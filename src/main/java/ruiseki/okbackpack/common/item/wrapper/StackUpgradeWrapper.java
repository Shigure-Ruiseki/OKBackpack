package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ISlotModifiable;
import ruiseki.okbackpack.config.ModConfig;

public class StackUpgradeWrapper extends UpgradeWrapperBase implements ISlotModifiable {

    public StackUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public int modifySlotLimit(int original, int slot) {
        return original;
    }

    @Override
    public int modifyStackLimit(int original, int slot, ItemStack stack) {
        return original;
    }

    public static int multiplier(ItemStack stack) {
        return switch (stack.getItemDamage()) {
            case 1 -> ModConfig.stackUpgradeTier2Mul;
            case 2 -> ModConfig.stackUpgradeTier3Mul;
            case 3 -> ModConfig.stackUpgradeTier4Mul;
            case 4 -> ModConfig.stackUpgradeTierOmegaMul;
            default -> ModConfig.stackUpgradeTier1Mul;
        };
    }
}
