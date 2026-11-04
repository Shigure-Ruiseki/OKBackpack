package ruiseki.okstoragecore.upgrades.tank;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFluidStackSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.helper.TranslateHelpers;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemTankUpgrade extends ItemUpgradeBase<TankUpgradeWrapper> {

    public static final UpgradeType<TankUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public TankUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new TankUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(TankUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedItemHandlerSH tankHandler = group.get("tank_handler");
            tankHandler.setItemHandler(wrapper::getStorage);

            DelegatedFluidStackSH fluidStackHandler = group.get("tank_fluid");
            fluidStackHandler.setDelegatedSupplier(wrapper::getContents);

            DelegatedIntSH tankCapacityHandler = group.get("tank_capacity");
            tankCapacityHandler.setDelegatedSupplier(wrapper::getTankCapacity);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<TankUpgradeWrapper> createExpandedTab(int slotIndex,
            TankUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            return new TankUpgradeTab(slotIndex, wrapper, stack, storagePanel);
        }

        @Override
        public @NotNull Widget<?> createSlotWidget(int slotIndex, TankUpgradeWrapper wrapper, ItemStack stack,
            StoragePanel storagePanel) {
            return new TankSlotWidget(slotIndex, wrapper, storagePanel);
        }
    });

    private final TankUpgradeConfig tankUpgradeConfig;

    public ItemTankUpgrade(TankUpgradeConfig tankUpgradeConfig, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.tankUpgradeConfig = tankUpgradeConfig;
    }

    public int getBaseCapacity(IStorageWrapper storageWrapper) {
        return tankUpgradeConfig.getCapacityPerSlotRow() * storageWrapper.getNumberOfSlotRows();
    }

    public int getAdjustedStackMultiplier(IStorageWrapper storageWrapper) {
        return 1 + (int) (tankUpgradeConfig.getStackMultiplierRatio() * (storageWrapper.getInventoryHandler()
            .getStackSizeMultiplier() - 1));
    }

    public int getTankCapacity(IStorageWrapper storageWrapper) {
        int stackMultiplier = getAdjustedStackMultiplier(storageWrapper);
        int baseCapacity = getBaseCapacity(storageWrapper);
        return Integer.MAX_VALUE / stackMultiplier < baseCapacity ? Integer.MAX_VALUE : baseCapacity * stackMultiplier;
    }

    public TankUpgradeConfig getTankUpgradeConfig() {
        return tankUpgradeConfig;
    }

    @Override
    public UpgradeType<TankUpgradeWrapper> getType() {
        return TYPE;
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
                if (wrapper instanceof TankUpgradeWrapper) {
                    errorUpgradeSlots.add(slot);
                }
            });

        if (errorUpgradeSlots.size() >= 2) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.two_tank_upgrades_present"),
                errorUpgradeSlots,
                Collections.emptySet(),
                Collections.emptySet());
        }

        int multiplierRequired = (int) Math.ceil(
            (float) FluidHelpers.getAmount(TankUpgradeWrapper.getContents(upgradeStack))
                / getTankCapacity(storageWrapper));
        if (multiplierRequired > 1) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.tank_capacity_high", multiplierRequired),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet());
        }

        return new UpgradeSlotChangeResult.Success();
    }

    @Override
    public int getInventoryColumnsTaken() {
        return 2;
    }
}
