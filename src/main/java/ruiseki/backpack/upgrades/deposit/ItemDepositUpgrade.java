package ruiseki.backpack.upgrades.deposit;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemDepositUpgrade extends ItemUpgradeBase<DepositUpgradeWrapper> {

    public static final UpgradeType<DepositUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public DepositUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new DepositUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(DepositUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 9 ? group.get("basic_filter_logic")
                : group.get("adv_filter_logic");
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
            filterLogic.syncToServer(CommonSHRegistries.FILTER);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<DepositUpgradeWrapper> createExpandedTab(int slotIndex,
            DepositUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9 ? new DepositUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new DepositUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });

    public final IntSupplier filterSlots;

    public ItemDepositUpgrade(IntSupplier filterSlots, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.filterSlots = filterSlots;
    }

    public int getFilterSlotCount() {
        return filterSlots.getAsInt();
    }

    @Override
    public UpgradeType<DepositUpgradeWrapper> getType() {
        return TYPE;
    }
}
