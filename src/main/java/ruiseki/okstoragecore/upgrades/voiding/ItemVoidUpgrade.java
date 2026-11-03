package ruiseki.okstoragecore.upgrades.voiding;

import java.util.function.BooleanSupplier;
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

public class ItemVoidUpgrade extends ItemUpgradeBase<VoidUpgradeWrapper> {

    public static final UpgradeType<VoidUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public VoidUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new VoidUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(VoidUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 9 ? group.get("basic_filter_logic")
                : group.get("adv_filter_logic");
            if (filterLogic == null) return;
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
            filterLogic.syncToServer(DelegatedFilterLogicSHRegisters.FILTER_LOGIC);
        }

        @Override
        public ExpandedTab createExpandedTab(int slotIndex, VoidUpgradeWrapper wrapper, ItemStack stack,
            StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9 ? new VoidUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new VoidUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });
    public final BooleanSupplier voidAnythingEnabled;
    public final IntSupplier filterSlots;

    public ItemVoidUpgrade(BooleanSupplier voidAnythingEnabled, IntSupplier filterSlots,
        IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.voidAnythingEnabled = voidAnythingEnabled;
        this.filterSlots = filterSlots;
    }

    @Override
    public UpgradeType<VoidUpgradeWrapper> getType() {
        return TYPE;
    }

    public int getFilterSlotCount() {
        return filterSlots.getAsInt();
    }

    public boolean isVoidAnythingEnabled() {
        return voidAnythingEnabled.getAsBoolean();
    }
}
