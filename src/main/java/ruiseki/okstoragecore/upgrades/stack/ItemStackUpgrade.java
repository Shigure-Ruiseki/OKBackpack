
package ruiseki.okstoragecore.upgrades.stack;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.TranslateHelpers;
import ruiseki.okstoragecore.upgrades.IStackableContentsUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;
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

    public static int getInventorySlotLimit(IStorageWrapper storageWrapper) {
        return getInventorySlotLimit(storageWrapper, 1);
    }

    private static int getInventorySlotLimit(IStorageWrapper storageWrapper, double skipMultiplier) {
        double multiplier = storageWrapper.getBaseStackSizeMultiplier();
        boolean multiplierSkipped = false;
        for (Wrapper stackWrapper : storageWrapper.getUpgradeHandler()
            .getTypeWrappers(TYPE)) {
            if (!multiplierSkipped && stackWrapper.getStackSizeMultiplier() == skipMultiplier) {
                multiplierSkipped = true;
                continue;
            }

            if (Integer.MAX_VALUE / stackWrapper.getStackSizeMultiplier() < multiplier) {
                return Integer.MAX_VALUE;
            }
            multiplier *= stackWrapper.getStackSizeMultiplier();
        }

        return getInventorySlotLimit(multiplier);
    }

    private static int getInventorySlotLimit(double multiplier) {
        return Integer.MAX_VALUE / 64D < multiplier ? Integer.MAX_VALUE : Math.max(1, (int) (multiplier * 64));
    }

    @Override
    public UpgradeType<Wrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }

    public double getStackSizeMultiplier() {
        return this.stackSizeMultiplier.getAsDouble();
    }

    private boolean isValidMultiplier() {
        double multiplier = this.getStackSizeMultiplier();
        return Double.isFinite(multiplier) && multiplier > 0;
    }

    @Override
    public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper, EntityPlayer player) {
        if (!isValidMultiplier()) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("remove.stack_invalid_multiplier"),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet());
        }
        double multiplierWhenRemoved = getInventorySlotLimit(storageWrapper, this.getStackSizeMultiplier()) / 64D;
        return isMultiplierHighEnough(storageWrapper, multiplierWhenRemoved, -1);
    }

    @Override
    public UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, int upgradeSlot,
        IStorageWrapper storageWrapper, EntityPlayer player) {
        UpgradeSlotChangeResult result = super.canSwapUpgradeFor(
            upgradeStackToPut,
            upgradeSlot,
            storageWrapper,
            player);
        if (!result.isSuccessful()) {
            return result;
        }
        if (!(ItemHelpers.getItem(upgradeStackToPut) instanceof ItemStackUpgrade otherUpgrade)) {
            return canRemoveUpgradeFrom(storageWrapper, player);
        }

        if (otherUpgrade.getStackSizeMultiplier() >= this.getStackSizeMultiplier()) {
            return new UpgradeSlotChangeResult.Success();
        }

        double multiplierWhenRemoved = getInventorySlotLimit(storageWrapper, this.getStackSizeMultiplier()) / 64D;

        return isMultiplierHighEnough(
            storageWrapper,
            multiplierWhenRemoved * otherUpgrade.getStackSizeMultiplier(),
            -1);
    }

    static UpgradeSlotChangeResult isMultiplierHighEnough(IStorageWrapper storageWrapper, double multiplier,
        int ignoreUpgradeSlot) {
        int inventorySlotLimit = getInventorySlotLimit(multiplier);
        double effectiveMultiplier = inventorySlotLimit / 64D;
        Set<Integer> slotsOverMultiplier = new HashSet<>();

        for (int slot = 0; slot < storageWrapper.getInventoryHandler()
            .getSlots(); slot++) {
            ItemStack stack = storageWrapper.getInventoryHandler()
                .getSlotStack(slot);
            if (ItemHelpers.getCount(stack) <= 1) {
                continue;
            }
            double stackMultiplierNeeded = (double) ItemHelpers.getCount(stack) / stack.getMaxStackSize();
            if (stackMultiplierNeeded > effectiveMultiplier) {
                slotsOverMultiplier.add(slot);
            }
        }

        Set<Integer> errorUpgradeSlots = new HashSet<>();
        Set<Integer> errorInventoryParts = new HashSet<>();
        for (Map.Entry<Integer, IUpgradeWrapper> entry : storageWrapper.getUpgradeHandler()
            .getSlotWrappers()
            .entrySet()) {
            Integer slot = entry.getKey();
            IUpgradeWrapper wrapper = entry.getValue();
            if (slot == ignoreUpgradeSlot) {
                continue;
            }
            if (wrapper instanceof IStackableContentsUpgrade stackableContentsUpgrade
                && stackableContentsUpgrade.getMinimumMultiplierRequired() > effectiveMultiplier) {
                errorUpgradeSlots.add(slot);
                errorInventoryParts.add(slot);
            }
        }

        if (!slotsOverMultiplier.isEmpty() || !errorInventoryParts.isEmpty()
            || storageWrapper.getMinimumStackMultiplierRequired() > effectiveMultiplier) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("remove.stack_low_multiplier", inventorySlotLimit),
                errorUpgradeSlots,
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
