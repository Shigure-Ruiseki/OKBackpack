package ruiseki.storagecore.upgrades.stack;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.storagecore.upgrades.UpgradeGroup;
import ruiseki.storagecore.upgrades.ItemUpgradeBase;
import ruiseki.storagecore.upgrades.UpgradeType;
import ruiseki.storagecore.upgrades.UpgradeWrapperBase;

public class ItemStackUpgrade extends ItemUpgradeBase<ItemStackUpgrade.Wrapper> {

    public static final UpgradeType<Wrapper> TYPE = new UpgradeType<>(Wrapper::new);
    public static final UpgradeGroup UPGRADE_GROUP = new UpgradeGroup(
        "stack_upgrades",
        LangHelpers.localize("stack_upgrades"));
    private final double stackSizeMultiplier;

    public ItemStackUpgrade(double stackSizeMultiplier, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.stackSizeMultiplier = stackSizeMultiplier;
    }

    public static int getInventorySlotLimit(IStorageWrapper storageWrapper) {
        double multiplier = storageWrapper.getBaseStackSizeMultiplier();

        for (Wrapper stackWrapper : storageWrapper.getUpgradeHandler()
            .getTypeWrappers(TYPE)) {
            if (Integer.MAX_VALUE / stackWrapper.getStackSizeMultiplier() < multiplier) {
                return Integer.MAX_VALUE;
            }
            multiplier *= stackWrapper.getStackSizeMultiplier();
        }

        return Integer.MAX_VALUE / 64D < multiplier ? Integer.MAX_VALUE : (int) (multiplier * 64);
    }

    @Override
    public UpgradeType<Wrapper> getType() {
        return TYPE;
    }

    public double getStackSizeMultiplier() {
        return stackSizeMultiplier;
    }

    @Override
    public UpgradeGroup getUpgradeGroup() {
        return UPGRADE_GROUP;
    }

    public static class Wrapper extends UpgradeWrapperBase<Wrapper, ItemStackUpgrade> {

        protected Wrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
            super(storageWrapper, upgrade, upgradeSaveHandler);
        }

        public double getStackSizeMultiplier() {
            return upgradeItem.getStackSizeMultiplier();
        }

        @Override
        public boolean hideSettingsTab() {
            return true;
        }

        @Override
        public boolean canBeDisabled() {
            return true;
        }
    }
}
