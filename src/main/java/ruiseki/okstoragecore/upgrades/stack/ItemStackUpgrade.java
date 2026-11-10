
package ruiseki.okstoragecore.upgrades.stack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.TranslateHelpers;
import ruiseki.okstoragecore.upgrades.IStackableContentsUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeGroup;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;
import ruiseki.okstoragecore.upgrades.UpgradeType;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class ItemStackUpgrade extends ItemUpgradeBase<ItemStackUpgrade.Wrapper> {

    public static final UpgradeType<Wrapper> TYPE = new UpgradeType<>(Wrapper::new);

    public static final UpgradeGroup UPGRADE_GROUP = new UpgradeGroup("stack", LangHelpers.localize("stack_upgrades"));

    private final DoubleSupplier stackSizeMultiplier;

    public ItemStackUpgrade(DoubleSupplier stackSizeMultiplier, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.stackSizeMultiplier = stackSizeMultiplier;
    }

    public static double getInventoryStackMultiplier(IStorageWrapper storageWrapper) {
        double multiplier = storageWrapper.getBaseStackSizeMultiplier();
        for (Wrapper wrapper : storageWrapper.getUpgradeHandler()
            .getTypeWrappers(TYPE)) {
            multiplier *= wrapper.getStackSizeMultiplier();
        }
        return multiplier;
    }

    public static int getInventorySlotLimit(IStorageWrapper storageWrapper) {
        double multiplier = getInventoryStackMultiplier(storageWrapper);
        if (!Double.isFinite(multiplier) || multiplier <= 0) {
            return 1;
        }
        if (multiplier >= Integer.MAX_VALUE / 64D) {
            return Integer.MAX_VALUE;
        }
        return Math.max(1, (int) (multiplier * 64));
    }

    @Override
    public UpgradeType<Wrapper> getType() {
        return TYPE;
    }

    public double getStackSizeMultiplier() {
        return this.stackSizeMultiplier.getAsDouble();
    }

    private boolean isValidMultiplier() {
        double multiplier = this.getStackSizeMultiplier();
        return Double.isFinite(multiplier) && multiplier > 0;
    }

    @Override
    public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper) {
        if (!isValidMultiplier()) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("remove.stack_invalid_multiplier"),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet());
        }
        double multiplierWhenRemoved = getInventoryStackMultiplier(storageWrapper) / getStackSizeMultiplier();
        return isMultiplierHighEnough(storageWrapper, multiplierWhenRemoved);
    }

    @Override
    public UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, IStorageWrapper storageWrapper) {
        if (!(ItemHelpers.getItem(upgradeStackToPut) instanceof ItemStackUpgrade otherUpgrade)) {
            return canRemoveUpgradeFrom(storageWrapper);
        }

        double oldMultiplier = getStackSizeMultiplier();
        double newMultiplier = otherUpgrade.getStackSizeMultiplier();

        if (!Double.isFinite(oldMultiplier) || oldMultiplier <= 0
            || !Double.isFinite(newMultiplier)
            || newMultiplier <= 0) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("remove.stack_invalid_multiplier"),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet());
        }

        if (newMultiplier >= oldMultiplier) {
            return new UpgradeSlotChangeResult.Success();
        }

        double multiplierWhenRemoved = getInventoryStackMultiplier(storageWrapper) / oldMultiplier * newMultiplier;

        return isMultiplierHighEnough(storageWrapper, multiplierWhenRemoved);
    }

    private UpgradeSlotChangeResult isMultiplierHighEnough(IStorageWrapper storageWrapper, double multiplier) {
        Set<Integer> slotsOverMultiplier = new HashSet<>();

        for (int slot = 0; slot < storageWrapper.getInventoryHandler()
            .getSlots(); slot++) {
            ItemStack stack = storageWrapper.getInventoryHandler()
                .getSlotStack(slot);
            if (ItemHelpers.isEmpty(stack)) continue;
            double stackMultiplierNeeded = (double) ItemHelpers.getCount(stack) / ItemHelpers.getMaxStackSize(stack);
            if (stackMultiplierNeeded > multiplier) {
                slotsOverMultiplier.add(slot);
            }
        }

        Set<Integer> errorInventoryParts = new HashSet<>();

        storageWrapper.getUpgradeHandler()
            .getSlotWrappers()
            .forEach((slot, wrapper) -> {
                if (wrapper instanceof IStackableContentsUpgrade stackableContentsUpgrade
                    && stackableContentsUpgrade.getMinimumMultiplierRequired() > multiplier) {
                    errorInventoryParts.add(slot);
                }
            });

        if (!slotsOverMultiplier.isEmpty() || !errorInventoryParts.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("remove.stack_low_multiplier", multiplier),
                Collections.emptySet(),
                slotsOverMultiplier,
                errorInventoryParts);
        }

        return new UpgradeSlotChangeResult.Success();
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
            return this.upgradeItem.getStackSizeMultiplier();
        }

        @Override
        public boolean hideSettingsTab() {
            return true;
        }

        @Override
        public boolean canBeDisabled() {
            return false;
        }
    }
}
