package ruiseki.okstoragecore.upgrades;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.TranslateHelpers;

public interface IUpgradeItem<W extends IUpgradeWrapper> {

    UpgradeType<W> getType();

    default UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack,
        EntityPlayer player) {
        UpgradeSlotChangeResult result = checkUpgradePerStorageTypeLimit(storageWrapper);
        if (!result.isSuccessful()) return result;

        result = checkForConflictingUpgrades(storageWrapper, getUpgradeConflicts(), -1);
        if (!result.isSuccessful()) {
            return result;
        }

        result = checkThisForConflictsWithExistingUpgrades(upgradeStack, storageWrapper, -1);
        if (!result.isSuccessful()) {
            return result;
        }

        result = checkInventoryColumns(storageWrapper, 0, getInventoryColumnsTaken());
        if (!result.isSuccessful()) {
            return result;
        }

        return checkExtraInsertConditions(upgradeStack, storageWrapper, -1, null);
    }

    default UpgradeSlotChangeResult checkThisForConflictsWithExistingUpgrades(ItemStack upgradeStack,
        IStorageWrapper storageWrapper, int excludeUpgradeSlot) {
        AtomicReference<UpgradeSlotChangeResult> result = new AtomicReference<>(new UpgradeSlotChangeResult.Success());
        InventoryHelpers.iterate(storageWrapper.getUpgradeHandler(), (slot, stack) -> {
            if (slot != excludeUpgradeSlot && !ItemHelpers.isEmpty(stack)
                && ItemHelpers.getItem(stack) instanceof IUpgradeItem<?>upgradeItem) {
                for (UpgradeConflictDefinition conflictDefinition : upgradeItem.getUpgradeConflicts()) {
                    // only checking for single item conflicts here would need to be expanded to support multiple,
                    // but there isn't a case like that at the moment because the other conflict check (the one where
                    // item inserted checks items that exist)
                    // covers all multiple item cases
                    if (conflictDefinition.maxConflictingAllowed() == 0
                        && conflictDefinition.isConflictingItem.test(ItemHelpers.getItem(upgradeStack))) {
                        result.set(
                            new UpgradeSlotChangeResult.Fail(
                                conflictDefinition.otherBeingAddedErrorMessage,
                                Set.of(slot),
                                Set.of(),
                                Set.of()));
                        return;
                    }
                }
            }
        },
            () -> !result.get()
                .isSuccessful());
        return result.get();
    }

    private UpgradeSlotChangeResult checkForConflictingUpgrades(IStorageWrapper storageWrapper,
        List<UpgradeConflictDefinition> upgradeConflicts, int excludeUpgradeSlot) {
        for (UpgradeConflictDefinition conflictDefinition : upgradeConflicts) {
            AtomicInteger conflictingCount = new AtomicInteger(0);
            Set<Integer> conflictingSlots = new HashSet<>();
            InventoryHelpers.iterate(storageWrapper.getUpgradeHandler(), (slot, stack) -> {
                if (slot != excludeUpgradeSlot && !ItemHelpers.isEmpty(stack)
                    && conflictDefinition.isConflictingItem.test(ItemHelpers.getItem(stack))) {
                    conflictingCount.incrementAndGet();
                    conflictingSlots.add(slot);
                }
            });

            if (conflictingCount.get() > conflictDefinition.maxConflictingAllowed) {
                return new UpgradeSlotChangeResult.Fail(
                    conflictDefinition.errorMessage,
                    conflictingSlots,
                    Set.of(),
                    Set.of());
            }
        }
        return new UpgradeSlotChangeResult.Success();
    }

    List<UpgradeConflictDefinition> getUpgradeConflicts();

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
            if (!ItemHelpers.isEmpty(stack) && stack.getItem() == this) {
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
            if (ItemHelpers.getItem(stack) instanceof IUpgradeItem<?>upgradeItem
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

    default UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper, EntityPlayer player) {
        return new UpgradeSlotChangeResult.Success();
    }

    default UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, int upgradeSlot,
        IStorageWrapper storageWrapper, EntityPlayer player) {
        if (upgradeStackToPut.getItem() == this) {
            return new UpgradeSlotChangeResult.Success();
        }

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

            UpgradeSlotChangeResult result = checkForConflictingUpgrades(
                storageWrapper,
                upgradeToPut.getUpgradeConflicts(),
                upgradeSlot);
            if (!result.isSuccessful()) {
                return result;
            }

            result = checkInventoryColumns(storageWrapper, 0, getInventoryColumnsTaken());
            if (!result.isSuccessful()) {
                return result;
            }

            return upgradeToPut.checkExtraInsertConditions(upgradeStackToPut, storageWrapper, upgradeSlot, this);
        }

        return new UpgradeSlotChangeResult.Success();
    }

    default UpgradeSlotChangeResult checkExtraInsertConditions(ItemStack upgradeStack, IStorageWrapper storageWrapper,
        int upgradeSlot, @Nullable IUpgradeItem<?> upgradeInSlot) {
        return checkExtraInsertConditions(upgradeStack, storageWrapper, upgradeInSlot);
    }

    default UpgradeSlotChangeResult checkExtraInsertConditions(ItemStack upgradeStack, IStorageWrapper storageWrapper,
        @Nullable IUpgradeItem<?> upgradeInSlot) {
        return new UpgradeSlotChangeResult.Success();
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

    record UpgradeConflictDefinition(Predicate<Item> isConflictingItem, int maxConflictingAllowed, String errorMessage,
        String otherBeingAddedErrorMessage) {

        public UpgradeConflictDefinition(Predicate<Item> isConflictingItem, int maxConflictingAllowed,
            String errorMessage) {
            this(isConflictingItem, maxConflictingAllowed, errorMessage, errorMessage);
        }
    }

    default void findUpgrades(Set<Integer> slots, IStorageWrapper storageWrapper,
        Predicate<IUpgradeWrapper> condition) {
        storageWrapper.getUpgradeHandler()
            .getSlotWrappers()
            .forEach((slot, wrapper) -> {
                if (condition.test(wrapper)) {
                    slots.add(slot);
                }
            });

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
            if (!ItemHelpers.isEmpty(stack)) occupiedSlots.add(slot);
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
}
