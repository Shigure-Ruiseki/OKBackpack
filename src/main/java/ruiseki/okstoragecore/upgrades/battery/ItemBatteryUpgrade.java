package ruiseki.okstoragecore.upgrades.battery;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.helper.TranslateHelpers;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.IUpgradeItem;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;
import ruiseki.okstoragecore.upgrades.UpgradeType;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

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
                batteryHandler.setItemHandler(wrapper::getStorage);
                batteryHandler.syncToServer(CommonSHRegistries.STORAGE);

                DelegatedIntSH energyStoredHandler = group.get("battery_energy_stored");
                energyStoredHandler.setDelegated(wrapper::getEnergyStored);
                energyStoredHandler.syncToServer(CommonSHRegistries.BATTERY_ENERGY);

                DelegatedIntSH maxEnergyHandler = group.get("battery_max_energy");
                maxEnergyHandler.setDelegated(wrapper::getMaxEnergyStored);
                maxEnergyHandler.syncToServer(CommonSHRegistries.BATTERY_CAPACITY);
            }

            @Override
            public @NotNull ExpandedUpgradeTab<BatteryUpgradeWrapper> createExpandedTab(int slotIndex,
                BatteryUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
                return new BatteryUpgradeTab(slotIndex, wrapper, stack, storagePanel);
            }

            @Override
            public @NotNull Widget<?> createSlotWidget(int slotIndex, BatteryUpgradeWrapper wrapper, ItemStack stack,
                StoragePanel storagePanel) {
                return new BatterySlotWidget(slotIndex, wrapper, storagePanel);
            }
        });
    public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(
        new UpgradeConflictDefinition(
            ItemBatteryUpgrade.class::isInstance,
            0,
            TranslateHelpers.error("add.battery_exists")));

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
    public UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, int upgradeSlot,
        IStorageWrapper storageWrapper, EntityPlayer player) {
        if (upgradeStackToPut.getItem() == this) {
            return checkExtraInsertConditions(upgradeStackToPut, storageWrapper, upgradeSlot, this);
        }

        return super.canSwapUpgradeFor(upgradeStackToPut, upgradeSlot, storageWrapper, player);
    }

    @Override
    public int getInventoryColumnsTaken() {
        return 2;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return UPGRADE_CONFLICT_DEFINITIONS;
    }

    @Override
    public UpgradeSlotChangeResult checkExtraInsertConditions(ItemStack upgradeStack, IStorageWrapper storageWrapper,
        @Nullable IUpgradeItem<?> upgradeInSlot) {
        int maxEnergyAfter = (int) (getMaxEnergyStored(storageWrapper)
            / (upgradeInSlot instanceof ItemStackUpgrade stackUpgrade ? stackUpgrade.getStackSizeMultiplier() : 1));
        double multiplierRequired = (double) BatteryUpgradeWrapper.getEnergyStored(upgradeStack) / maxEnergyAfter;
        if (multiplierRequired > 1) {
            DecimalFormat multiplierFormat = new DecimalFormat("0.#");
            String formattedMultiplierRequired = multiplierFormat.format(Math.ceil(10 * multiplierRequired) / 10);
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.battery_energy_high", formattedMultiplierRequired),
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
