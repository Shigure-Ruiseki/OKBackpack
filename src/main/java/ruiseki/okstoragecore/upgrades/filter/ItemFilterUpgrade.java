package ruiseki.okstoragecore.upgrades.filter;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

import net.minecraft.item.ItemStack;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.ExpandedTab;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemFilterUpgrade extends ItemUpgradeBase<FilterUpgradeWrapper> {

    public static final UpgradeType<FilterUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public FilterUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new FilterUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(FilterUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 9 ? group.get("basic_filter_logic")
                : group.get("adv_filter_logic");
            if (filterLogic == null) return;
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
            filterLogic.syncToServer(DelegatedFilterLogicSHRegisters.FILTER_LOGIC);
        }

        @Override
        public ExpandedTab createExpandedTab(int slotIndex, FilterUpgradeWrapper wrapper, ItemStack stack,
            StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9 ? new FilterUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new FilterUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });

    private final IntSupplier filterSlotCount;

    public ItemFilterUpgrade(IntSupplier filterSlotCount, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.filterSlotCount = filterSlotCount;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    @Override
    public UpgradeType<FilterUpgradeWrapper> getType() {
        return TYPE;
    }
}
