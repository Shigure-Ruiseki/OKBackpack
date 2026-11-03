package ruiseki.backpack.upgrades.restock;

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

public class ItemRestockUpgrade extends ItemUpgradeBase<RestockUpgradeWrapper> {

    public static final UpgradeType<RestockUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public RestockUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new RestockUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(RestockUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 9 ? group.get("basic_filter_logic")
                : group.get("adv_filter_logic");
            if (filterLogic == null) return;
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
            filterLogic.syncToServer(DelegatedFilterLogicSHRegisters.FILTER_LOGIC);
        }

        @Override
        public ExpandedTab createExpandedTab(int slotIndex, RestockUpgradeWrapper wrapper, ItemStack stack,
            StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9 ? new RestockUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new RestockUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });

    private final IntSupplier filterSlotCount;

    public ItemRestockUpgrade(IntSupplier filterSlotCount, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.filterSlotCount = filterSlotCount;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    @Override
    public UpgradeType<RestockUpgradeWrapper> getType() {
        return TYPE;
    }
}
