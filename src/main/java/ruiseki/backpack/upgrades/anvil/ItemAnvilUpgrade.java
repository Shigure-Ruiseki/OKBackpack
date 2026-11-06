package ruiseki.backpack.upgrades.anvil;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
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
        public void updateTabDelegates(AnvilUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
            DelegatedItemHandlerSH handler = group.get("anvil_handler");
            handler.setItemHandler(wrapper::getStorage);

            AnvilContainer container = new AnvilContainer(
                group.getWrapper(),
                group.getPanel(),
                handler.getContainerSupplier(),
                group.getPlayer(),
                group.getUpgradeIndex(),
                handler.getItemHandler());
            group.setUpgradeContainer(container);

            DelegatedStringSH nameField = group.get("anvil_name_field");
            nameField.setDelegated(wrapper::getItemName, s -> {
                wrapper.setItemName(s);
                container.updateRepairOutput();
            });
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
}
