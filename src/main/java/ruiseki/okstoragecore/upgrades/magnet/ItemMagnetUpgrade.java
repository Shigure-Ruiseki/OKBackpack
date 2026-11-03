package ruiseki.okstoragecore.upgrades.magnet;

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

public class ItemMagnetUpgrade extends ItemUpgradeBase<MagnetUpgradeWrapper> {

    public static final UpgradeType<MagnetUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public MagnetUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new MagnetUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(MagnetUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 9 ? group.get("basic_filter_logic")
                : group.get("adv_filter_logic");
            if (filterLogic == null) return;
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
            filterLogic.syncToServer(DelegatedFilterLogicSHRegisters.FILTER_LOGIC);
        }

        @Override
        public ExpandedTab createExpandedTab(int slotIndex, MagnetUpgradeWrapper wrapper, ItemStack stack,
            StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9 ? new MagnetUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new MagnetUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });

    private final IntSupplier radius;
    private final IntSupplier filterSlotCount;

    public ItemMagnetUpgrade(IntSupplier radius, IntSupplier filterSlotCount,
        IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.radius = radius;
        this.filterSlotCount = filterSlotCount;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }

    public int getRadius() {
        return radius.getAsInt();
    }

    @Override
    public UpgradeType<MagnetUpgradeWrapper> getType() {
        return TYPE;
    }
}
