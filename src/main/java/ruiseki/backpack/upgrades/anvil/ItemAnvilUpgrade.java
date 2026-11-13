package ruiseki.backpack.upgrades.anvil;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.backpack.client.gui.sync.BackpackSHRegistries;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
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
        public void updateTabDelegates(AnvilUpgradeWrapper wrapper, UpgradeSlotUpdate slotUpdate) {
            DelegatedItemHandlerSH handler = slotUpdate.get("anvil_handler");
            handler.setItemHandler(wrapper::getStorage);
            handler.syncToServer(CommonSHRegistries.STORAGE);

            UpgradeSlotSH slotSH = slotUpdate.get("upgrade_handler");
            AnvilContainer container = new AnvilContainer(
                slotUpdate.getWrapper(),
                slotUpdate.getPanel(),
                handler.getContainerSupplier(),
                slotUpdate.getPlayer(),
                slotUpdate.getUpgradeIndex(),
                handler.getItemHandler());
            slotUpdate.setUpgradeContainer(container);
            slotSH.syncToServer(BackpackSHRegistries.ANVIL_CONTAINER);

            DelegatedStringSH nameField = slotUpdate.get("anvil_name_field");
            nameField.setDelegated(wrapper::getItemName, s -> {
                wrapper.setItemName(s);
                container.updateRepairOutput();
            });
            handler.syncToServer(BackpackSHRegistries.ANVIL_NAME);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<AnvilUpgradeWrapper> createExpandedTab(int slotIndex,
            AnvilUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
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

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }
}
