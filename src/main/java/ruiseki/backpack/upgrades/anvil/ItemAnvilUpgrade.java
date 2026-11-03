package ruiseki.backpack.upgrades.anvil;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.UpgradeContainer;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.ExpandedTab;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemAnvilUpgrade extends ItemUpgradeBase<AnvilUpgradeWrapper> {

    public static final UpgradeType<AnvilUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public AnvilUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new AnvilUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(AnvilUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedItemHandlerSH handler = group.get("anvil_handler");
            if (handler == null) return;
            handler.setItemHandler(wrapper::getStorage);
            handler.syncToServer(DelegatedItemHandlerSHRegisters.STORAGE);
        }

        @Override
        public ExpandedTab createExpandedTab(int slotIndex, AnvilUpgradeWrapper wrapper, ItemStack stack,
            StoragePanel storagePanel) {
            return new AnvilUpgradeTab(slotIndex, wrapper, stack, storagePanel);
        }
    });

    public ItemAnvilUpgrade() {
        super(GeneralConfig.maxUpgradesPerStorageConfig);
    }

    @Override
    public UpgradeType<AnvilUpgradeWrapper> getType() {
        return TYPE;
    }
}
