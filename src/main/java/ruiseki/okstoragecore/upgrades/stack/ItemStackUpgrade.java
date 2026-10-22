package ruiseki.okstoragecore.upgrades.stack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

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
        return this.stackSizeMultiplier;
    }

    @Override
    public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper) {
        double currentInventoryMultiplier = getInventorySlotLimit(storageWrapper) / 64D;
        double multiplierWhenRemoved = currentInventoryMultiplier / stackSizeMultiplier;
        return isMultiplierHighEnough(storageWrapper, multiplierWhenRemoved);
    }

    @Override
    public UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, IStorageWrapper storageWrapper) {
        if (!(upgradeStackToPut.getItem() instanceof ItemStackUpgrade otherStackUpgradeItem)) {
            return canRemoveUpgradeFrom(storageWrapper);
        }

        if (otherStackUpgradeItem.stackSizeMultiplier >= stackSizeMultiplier) {
            return new UpgradeSlotChangeResult.Success();
        }

        int currentInventoryMultiplier = getInventorySlotLimit(storageWrapper) / 64;
        double multiplierWhenRemoved = currentInventoryMultiplier / stackSizeMultiplier;

        return isMultiplierHighEnough(
            storageWrapper,
            multiplierWhenRemoved * otherStackUpgradeItem.stackSizeMultiplier);
    }

    private UpgradeSlotChangeResult isMultiplierHighEnough(IStorageWrapper storageWrapper, double multiplier) {
        Set<Integer> slotsOverMultiplier = new HashSet<>();

        for (int slot = 0; slot < storageWrapper.getInventoryHandler()
            .getSlots(); slot++) {
            ItemStack stack = storageWrapper.getInventoryHandler()
                .getSlotStack(slot);
            if (ItemHelpers.isEmpty(stack)) continue;
            double stackMultiplierNeeded = (double) ItemHelpers.getCount(stack) / stack.getMaxStackSize();
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
