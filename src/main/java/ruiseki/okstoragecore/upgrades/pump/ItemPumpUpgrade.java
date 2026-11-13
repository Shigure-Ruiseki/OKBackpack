package ruiseki.okstoragecore.upgrades.pump;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFluidFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemPumpUpgrade extends ItemUpgradeBase<PumpUpgradeWrapper> {

    public static final UpgradeType<PumpUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public PumpUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new PumpUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(PumpUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFluidFilterLogicSH filterLogic = wrapper.getSlotCount() == 9
                ? group.get("basic_fluid_filter_logic")
                : group.get("adv_fluid_filter_logic");
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
            filterLogic.syncToServer(CommonSHRegistries.FLUID_FILTER);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<PumpUpgradeWrapper> createExpandedTab(int slotIndex,
            PumpUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9 ? new PumpUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new PumpUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });

    private final PumpUpgradeConfig pumpUpgradeConfig;

    private final BooleanSupplier interactWithHandDefault;
    private final BooleanSupplier interactWithWorldDefault;

    public ItemPumpUpgrade(PumpUpgradeConfig pumpUpgradeConfig, BooleanSupplier interactWithHandDefault,
        BooleanSupplier interactWithWorldDefault, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.pumpUpgradeConfig = pumpUpgradeConfig;
        this.interactWithHandDefault = interactWithHandDefault;
        this.interactWithWorldDefault = interactWithWorldDefault;
    }

    public PumpUpgradeConfig getPumpUpgradeConfig() {
        return pumpUpgradeConfig;
    }

    public boolean getInteractWithHandDefault() {
        return interactWithHandDefault.getAsBoolean();
    }

    public boolean getInteractWithWorldDefault() {
        return interactWithWorldDefault.getAsBoolean();
    }

    @Override
    public UpgradeType<PumpUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }
}
