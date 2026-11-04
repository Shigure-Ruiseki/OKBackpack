package ruiseki.okstoragecore.upgrades.pickup;

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

public class ItemPickupUpgrade extends ItemUpgradeBase<PickupUpgradeWrapper> {

    public static final UpgradeType<PickupUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public PickupUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new PickupUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(PickupUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 9 ? group.get("basic_filter_logic")
                : group.get("adv_filter_logic");
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<PickupUpgradeWrapper> createExpandedTab(int slotIndex,
            PickupUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9 ? new PickupUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new PickupUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });

    private final IntSupplier filterSlotCount;

    public ItemPickupUpgrade(IntSupplier filterSlotCount, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.filterSlotCount = filterSlotCount;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    @Override
    public UpgradeType<PickupUpgradeWrapper> getType() {
        return TYPE;
    }
}
