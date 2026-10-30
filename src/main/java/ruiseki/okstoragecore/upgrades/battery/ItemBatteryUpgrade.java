package ruiseki.okstoragecore.upgrades.battery;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSHRegisters;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedValueSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.ExpandedTab;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.helper.TranslateHelpers;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemBatteryUpgrade extends ItemUpgradeBase<BatteryUpgradeWrapper> {

    public static final UpgradeType<BatteryUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<BatteryUpgradeWrapper>() {

            @Override
            public BatteryUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
                Consumer<ItemStack> upgradeSaveHandler) {
                return new BatteryUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
            }

            @Override
            public void updateTabDelegates(BatteryUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
                DelegatedItemHandlerSH batteryHandler = group.get("battery_handler");
                if (batteryHandler == null) return;
                batteryHandler.setItemHandler(wrapper::getStorage);
                batteryHandler.syncToServer(DelegatedItemHandlerSHRegisters.STORAGE);

                DelegatedIntSH energyStoredHandler = group.get("battery_energy_stored");
                if (energyStoredHandler == null) return;
                energyStoredHandler.setDelegatedSupplier(wrapper::getEnergyStored);
                energyStoredHandler.syncToServer(DelegatedValueSHRegisters.BATTERY_ENERGY);

                DelegatedIntSH maxEnergyHandler = group.get("battery_max_energy");
                if (maxEnergyHandler == null) return;
                maxEnergyHandler.setDelegatedSupplier(wrapper::getMaxEnergyStored);
                maxEnergyHandler.syncToServer(DelegatedValueSHRegisters.BATTERY_MAX_ENERGY);
            }

            @Override
            public ExpandedTab createExpandedTab(int slotIndex, BatteryUpgradeWrapper wrapper, ItemStack stack,
                StoragePanel storagePanel) {
                return new BatteryUpgradeTab(slotIndex, wrapper, stack, storagePanel);
            }

            @Override
            public Widget<?> createSlotWidget(int slotIndex, BatteryUpgradeWrapper wrapper, ItemStack stack,
                StoragePanel storagePanel) {
                return new BatterySlotWidget(slotIndex, wrapper, storagePanel);
            }
        });

    private final BatteryUpgradeConfig batteryUpgradeConfig;

    public ItemBatteryUpgrade(BatteryUpgradeConfig batteryUpgradeConfig,
        IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.batteryUpgradeConfig = batteryUpgradeConfig;
    }

    public BatteryUpgradeConfig getBatteryUpgradeConfig() {
        return batteryUpgradeConfig;
    }

    @Override
    public UpgradeType<BatteryUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public int getInventoryColumnsTaken() {
        return 2;
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack) {
        UpgradeSlotChangeResult result = super.canAddUpgradeTo(storageWrapper, upgradeStack);
        if (!result.isSuccessful()) {
            return result;
        }

        Set<Integer> errorUpgradeSlots = new HashSet<>();
        storageWrapper.getUpgradeHandler()
            .getSlotWrappers()
            .forEach((slot, wrapper) -> {
                if (wrapper instanceof BatteryUpgradeWrapper) {
                    errorUpgradeSlots.add(slot);
                }
            });

        if (!errorUpgradeSlots.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.battery_exists"),
                errorUpgradeSlots,
                Collections.emptySet(),
                Collections.emptySet());
        }

        int multiplierRequired = (int) Math
            .ceil((float) BatteryUpgradeWrapper.getEnergyStored(upgradeStack) / getMaxEnergyStored(storageWrapper));
        if (multiplierRequired > 1) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.battery_energy_high", multiplierRequired),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet());
        }

        return new UpgradeSlotChangeResult.Success();
    }

    public int getMaxEnergyStored(IStorageWrapper storageWrapper) {
        int stackMultiplier = getAdjustedStackMultiplier(storageWrapper);
        int maxEnergyBase = getMaxEnergyBase(storageWrapper);
        return Integer.MAX_VALUE / stackMultiplier < maxEnergyBase ? Integer.MAX_VALUE
            : maxEnergyBase * stackMultiplier;
    }

    public int getAdjustedStackMultiplier(IStorageWrapper storageWrapper) {
        return 1 + (int) (batteryUpgradeConfig.getStackMultiplierRatio() * (storageWrapper.getInventoryHandler()
            .getStackSizeMultiplier() - 1));
    }

    public int getMaxEnergyBase(IStorageWrapper storageWrapper) {
        return batteryUpgradeConfig.getEnergyPerSlotRow() * storageWrapper.getNumberOfSlotRows();
    }
}
