package ruiseki.okstoragecore.upgrades.tank;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFluidStackSH;
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
            tankHandler.syncToServer(CommonSHRegistries.STORAGE);

            DelegatedFluidStackSH fluidStackHandler = group.get("tank_fluid");
            fluidStackHandler.setDelegated(wrapper::getContents);
            fluidStackHandler.syncToServer(CommonSHRegistries.TANK_FLUID);

            DelegatedIntSH tankCapacityHandler = group.get("tank_capacity");
            tankCapacityHandler.setDelegated(wrapper::getTankCapacity);
            tankCapacityHandler.syncToServer(CommonSHRegistries.TANK_CAPACITY);
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
    public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(
        new UpgradeConflictDefinition(
            ItemTankUpgrade.class::isInstance,
            1,
            TranslateHelpers.error("add.two_tank_upgrades_present")));

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
    public UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, int upgradeSlot,
        IStorageWrapper storageWrapper, EntityPlayer player) {
        if (upgradeStackToPut.getItem() == this) {
            return checkExtraInsertConditions(upgradeStackToPut, storageWrapper, upgradeSlot, this);
        }

        return super.canSwapUpgradeFor(upgradeStackToPut, upgradeSlot, storageWrapper, player);
    }

    @Override
    public UpgradeSlotChangeResult checkExtraInsertConditions(ItemStack upgradeStack, IStorageWrapper storageWrapper,
        @Nullable IUpgradeItem<?> upgradeInSlot) {
        int capacityAfter = (int) (getTankCapacity(storageWrapper)
            / (upgradeInSlot instanceof ItemStackUpgrade stackUpgrade ? stackUpgrade.getStackSizeMultiplier() : 1));
        double multiplierRequired = (double) FluidHelpers.getAmount(TankUpgradeWrapper.getContents(upgradeStack))
            / capacityAfter;
        if (multiplierRequired > 1) {
            DecimalFormat multiplierFormat = new DecimalFormat("0.#");
            String formattedMultiplierRequired = multiplierFormat.format(Math.ceil(10 * multiplierRequired) / 10);
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.tank_capacity_high", formattedMultiplierRequired),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet());
        }

        return new UpgradeSlotChangeResult.Success();
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return UPGRADE_CONFLICT_DEFINITIONS;
    }

    @Override
    public int getInventoryColumnsTaken() {
        return 2;
    }
}
