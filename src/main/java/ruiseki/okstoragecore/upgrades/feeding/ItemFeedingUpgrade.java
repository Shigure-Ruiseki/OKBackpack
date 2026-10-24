package ruiseki.okstoragecore.upgrades.feeding;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

import net.minecraft.item.ItemStack;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.ExpandedTab;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemFeedingUpgrade extends ItemUpgradeBase<FeedingUpgradeWrapper> {

    public static final UpgradeType<FeedingUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public FeedingUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new FeedingUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(FeedingUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFilterLogicSH filterLogicSH = group.get("filter_logic");
            if (filterLogicSH == null) return;
            filterLogicSH.setFilterLogic(wrapper::getFilterLogic);
        }

        @Override
        public ExpandedTab createExpandedTab(int slotIndex, FeedingUpgradeWrapper wrapper, ItemStack stack,
            IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey) {
            return new FeedingUpgradeTab.Advanced(slotIndex, wrapper, stack, storageWrapper, storagePanel, titleKey);
        }
    });

    private final IntSupplier filterSlotCount;

    protected ItemFeedingUpgrade(IntSupplier filterSlotCount, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.filterSlotCount = filterSlotCount;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    @Override
    public UpgradeType<FeedingUpgradeWrapper> getType() {
        return TYPE;
    }
}
