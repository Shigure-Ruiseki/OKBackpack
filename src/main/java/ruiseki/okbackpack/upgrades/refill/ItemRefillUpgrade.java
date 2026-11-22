package ruiseki.okbackpack.upgrades.refill;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemRefillUpgrade extends ItemUpgradeBase<RefillUpgradeWrapper> {

    private static final UpgradeType<RefillUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<RefillUpgradeWrapper>() {

            @Override
            public RefillUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
                Consumer<ItemStack> upgradeSaveHandler) {
                return new RefillUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
            }

            @Override
            public void updateTabDelegates(RefillUpgradeWrapper wrapper, UpgradeSlotUpdate slotUpdate) {
                DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 6 ? slotUpdate.get("basic_refill_logic")
                    : slotUpdate.get("adv_refill_logic");
                filterLogic.setFilterLogic(wrapper::getFilterLogic);
                filterLogic.syncToServer(CommonSHRegistries.FILTER);
            }

            @Override
            public @NotNull ExpandedUpgradeTab<RefillUpgradeWrapper> createExpandedTab(int slotIndex,
                RefillUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
                return wrapper.getSlotCount() == 6 ? new RefillUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                    : new RefillUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
            }
        });

    private final IntSupplier filterSlotCount;
    private final BooleanSupplier targetSlotSelection;
    private final BooleanSupplier supportsBlockPick;

    public ItemRefillUpgrade(IntSupplier filterSlotCount, BooleanSupplier targetSlotSelection,
        BooleanSupplier supportsBlockPick) {
        super(GeneralConfig.maxUpgradesPerStorageConfig);
        this.filterSlotCount = filterSlotCount;
        this.targetSlotSelection = targetSlotSelection;
        this.supportsBlockPick = supportsBlockPick;
    }

    public boolean supportsBlockPick() {
        return supportsBlockPick.getAsBoolean();
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    public boolean allowsTargetSlotSelection() {
        return targetSlotSelection.getAsBoolean();
    }

    @Override
    public UpgradeType<RefillUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }
}
