package ruiseki.okstoragecore.upgrades;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.TranslateHelpers;

public interface IUpgradeItem<W extends IUpgradeWrapper> {

    UpgradeType<W> getType();

    default UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack) {
        UpgradeSlotChangeResult result = checkUpgradePerStorageTypeLimit(storageWrapper);
        if (!result.isSuccessful()) return result;
        return checkInventoryColumns(storageWrapper, 0, getInventoryColumnsTaken());
    }

    private UpgradeSlotChangeResult checkUpgradePerStorageTypeLimit(IStorageWrapper storageWrapper) {
        int upgradesPerStorage = getUpgradesPerStorage(storageWrapper.getStorageType());
        int upgradesInGroupPerStorage = getUpgradesInGroupPerStorage(storageWrapper.getStorageType());

        if (upgradesPerStorage == Integer.MAX_VALUE && upgradesInGroupPerStorage == Integer.MAX_VALUE) {
            return new UpgradeSlotChangeResult.Success();
        }

        if (upgradesPerStorage == 0) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.upgrade_not_allowed", getName(), storageWrapper.getDisplayName()),
                Set.of(),
                Set.of(),
                Set.of());
        } else if (upgradesInGroupPerStorage == 0) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error(
                    "add.upgrade_not_allowed",
                    LangHelpers.localize(getUpgradeGroup().translName()),
                    storageWrapper.getDisplayName()),
                Set.of(),
                Set.of(),
                Set.of());
        }

        Set<Integer> slotsWithUpgrade = new HashSet<>();
        InventoryHelpers.iterate(storageWrapper.getUpgradeHandler(), (slot, stack) -> {
            if (stack.getItem() == this) {
                slotsWithUpgrade.add(slot);
            }
        });

        if (slotsWithUpgrade.size() >= upgradesPerStorage) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error(
                    "add.only_x_upgrades_allowed",
                    upgradesPerStorage,
                    getName(),
                    storageWrapper.getDisplayName(),
                    upgradesPerStorage),
                slotsWithUpgrade,
                Set.of(),
                Set.of());
        }

        Set<Integer> slotsWithUgradeGroup = new HashSet<>();
        InventoryHelpers.iterate(storageWrapper.getUpgradeHandler(), (slot, stack) -> {
            if (stack.getItem() instanceof IUpgradeItem<?>upgradeItem
                && upgradeItem.getUpgradeGroup() == getUpgradeGroup()) {
                slotsWithUgradeGroup.add(slot);
            }
        });

        if (slotsWithUgradeGroup.size() >= upgradesInGroupPerStorage) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error(
                    "add.only_x_upgrades_allowed",
                    upgradesInGroupPerStorage,
                    LangHelpers.localize(getUpgradeGroup().translName()),
                    storageWrapper.getDisplayName()),
                slotsWithUgradeGroup,
                Set.of(),
                Set.of());
        }

        return new UpgradeSlotChangeResult.Success();
    }

    private static UpgradeSlotChangeResult checkInventoryColumns(IStorageWrapper storageWrapper, int oldColumnsTaken,
        int newColumnsTaken) {
        int columnsDelta = newColumnsTaken - oldColumnsTaken;

        if (columnsDelta <= 0) {
            return new UpgradeSlotChangeResult.Success();
        }

        int totalColumns = storageWrapper.getNumberOfSlotColumns();
        int totalRows = storageWrapper.getNumberOfSlotRows();
        int currentTaken = storageWrapper.getColumnsTaken();

        int resultingTaken = currentTaken + columnsDelta;

        if (resultingTaken >= totalColumns) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.not_enough_columns"),
                Set.of(),
                Set.of(),
                Set.of());
        }

        int currentAvailable = Math.max(0, storageWrapper.getNumberOfInventorySlots() - totalRows * currentTaken);

        int newAvailable = Math.max(0, storageWrapper.getNumberOfInventorySlots() - totalRows * resultingTaken);

        var inventory = storageWrapper.getInventoryHandler();
        Set<Integer> occupiedSlots = new HashSet<>();

        int end = Math.min(currentAvailable, inventory.getSlots());

        for (int slot = newAvailable; slot < end; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);

            if (stack != null && stack.stackSize > 0) {
                occupiedSlots.add(slot);
            }
        }

        if (!occupiedSlots.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.inventory_slots_occupied"),
                Set.of(),
                occupiedSlots,
                Set.of());
        }

        return new UpgradeSlotChangeResult.Success();
    }

    default UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper) {
        return new UpgradeSlotChangeResult.Success();
    }

    default UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, IStorageWrapper storageWrapper) {
        if (upgradeStackToPut.getItem() instanceof IUpgradeItem<?>upgradeToPut) {
            int upgradesPerStorage = upgradeToPut.getUpgradesPerStorage(storageWrapper.getStorageType());
            int upgradesInGroupPerStorage = upgradeToPut.getUpgradesInGroupPerStorage(storageWrapper.getStorageType());
            if (upgradesPerStorage < upgradesInGroupPerStorage) {
                if (upgradeStackToPut.getItem() != this) {
                    UpgradeSlotChangeResult result = upgradeToPut.checkUpgradePerStorageTypeLimit(storageWrapper);
                    if (!result.isSuccessful()) {
                        return result;
                    }
                }
            } else {
                if (upgradeToPut.getUpgradeGroup() != getUpgradeGroup()) {
                    UpgradeSlotChangeResult result = upgradeToPut.checkUpgradePerStorageTypeLimit(storageWrapper);
                    if (!result.isSuccessful()) {
                        return result;
                    }
                }
            }
        }

        UpgradeSlotChangeResult result = canRemoveUpgradeFrom(storageWrapper);
        if (!result.isSuccessful()) return result;

        if (!(upgradeStackToPut.getItem() instanceof IUpgradeItem<?>upgradeToPut)) {
            return new UpgradeSlotChangeResult.Success();
        }

        return checkInventoryColumns(
            storageWrapper,
            getInventoryColumnsTaken(),
            upgradeToPut.getInventoryColumnsTaken());
    }

    default int getInventoryColumnsTaken() {
        return 0;
    }

    default ItemStack getCleanedUpgradeStack(ItemStack upgradeStack) {
        return upgradeStack;
    }

    int getUpgradesPerStorage(String storageType);

    int getUpgradesInGroupPerStorage(String storageType);

    default UpgradeGroup getUpgradeGroup() {
        return UpgradeGroup.NONE;
    }

    String getName();
}
