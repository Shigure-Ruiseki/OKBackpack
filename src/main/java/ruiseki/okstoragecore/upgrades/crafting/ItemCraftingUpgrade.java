package ruiseki.okstoragecore.upgrades.crafting;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.slot.CraftingSlotInfo;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularCraftingSlot;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemCraftingUpgrade extends ItemUpgradeBase<CraftingUpgradeWrapper> {

    public static final UpgradeType<CraftingUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public CraftingUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new CraftingUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(CraftingUpgradeWrapper wrapper, UpgradeSlotUpdate slotUpdate) {
            DelegatedItemHandlerSH handler = slotUpdate.get("crafting_handler");
            handler.setItemHandler(wrapper::getStorage);
            handler.syncToServer(CommonSHRegistries.STORAGE);

            UpgradeSlotSH slotSH = slotUpdate.get("upgrade_handler");
            CraftingContainer container = new CraftingContainer(
                slotUpdate.getWrapper(),
                slotUpdate.getPanel(),
                handler.getContainerSupplier(),
                slotUpdate.getPlayer(),
                slotUpdate.getUpgradeIndex(),
                3,
                3,
                handler.getItemHandler());
            slotUpdate.setUpgradeContainer(container);
            slotSH.syncToServer(CommonSHRegistries.CRAFTING_CONTAINER);

            IndexedModularSlot[] matrix = slotUpdate.get("crafting_matrix_slots");
            ModularCraftingSlot result = slotUpdate.get("crafting_result_slot");
            CraftingSlotInfo slotInfo = new CraftingSlotInfo(
                slotUpdate.getUpgradeIndex(),
                matrix,
                result,
                3,
                3,
                25,
                6,
                18,
                18);
            slotUpdate.setCraftingInfo(slotInfo);
            slotSH.syncToServer(CommonSHRegistries.CRAFTING_INFO);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<CraftingUpgradeWrapper> createExpandedTab(int slotIndex,
            CraftingUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            return new CraftingUpgradeTab(slotIndex, wrapper, stack, storagePanel);
        }
    });

    public ItemCraftingUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<CraftingUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }
}
