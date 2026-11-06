package ruiseki.okstoragecore.upgrades.compacting;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemCompactingUpgrade extends ItemUpgradeBase<CompactingUpgradeWrapper> {

    public static final UpgradeType<CompactingUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public CompactingUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new CompactingUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(CompactingUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 9 ? group.get("basic_filter_logic")
                : group.get("adv_filter_logic");
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<CompactingUpgradeWrapper> createExpandedTab(int slotIndex,
            CompactingUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9 ? new CompactingUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new CompactingUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });

    private final BooleanSupplier shouldCompactThreeByThree;
    private final IntSupplier filterSlotCount;

    public ItemCompactingUpgrade(BooleanSupplier shouldCompactThreeByThree, IntSupplier filterSlotCount,
        IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.shouldCompactThreeByThree = shouldCompactThreeByThree;
        this.filterSlotCount = filterSlotCount;
    }

    public boolean shouldCompactThreeByThree() {
        return shouldCompactThreeByThree.getAsBoolean();
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    @Override
    public UpgradeType<CompactingUpgradeWrapper> getType() {
        return TYPE;
    }
}
