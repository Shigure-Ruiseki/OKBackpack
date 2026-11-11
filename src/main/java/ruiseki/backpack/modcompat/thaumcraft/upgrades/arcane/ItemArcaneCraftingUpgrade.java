package ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.backpack.modcompat.thaumcraft.client.gui.sync.ThaumcraftSHRegistries;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.slot.CraftingSlotInfo;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularCraftingSlot;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemArcaneCraftingUpgrade extends ItemUpgradeBase<ArcaneCraftingUpgradeWrapper> {

    public static final UpgradeType<ArcaneCraftingUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<>() {

            @Override
            public ArcaneCraftingUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
                Consumer<ItemStack> upgradeSaveHandler) {
                return new ArcaneCraftingUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
            }

            @Override
            public void updateTabDelegates(ArcaneCraftingUpgradeWrapper wrapper, UpgradeSlotUpdate slotUpdate) {
                DelegatedItemHandlerSH handler = slotUpdate.get("arcane_handler");
                handler.setItemHandler(wrapper::getStorage);
                handler.syncToServer(CommonSHRegistries.STORAGE);

                DelegatedBooleanSH hasWand = slotUpdate.get("arcane_has_wand");
                hasWand.setDelegated(wrapper::hasWand);
                hasWand.syncToServer(ThaumcraftSHRegistries.HAS_WAND);

                DelegatedStringSH missingResearch = slotUpdate.get("arcane_missing_research");
                missingResearch.setDelegated(wrapper::getMissingResearch);
                missingResearch.syncToServer(ThaumcraftSHRegistries.MISSING_RESEARCH);

                DelegatedStringSH missingResearchName = slotUpdate.get("arcane_missing_research_name");
                missingResearchName.setDelegated(wrapper::getMissingResearchName);
                missingResearchName.syncToServer(ThaumcraftSHRegistries.MISSING_RESEARCH_NAME);

                UpgradeSlotSH slotSH = slotUpdate.get("upgrade_handler");
                ArcaneContainer container = new ArcaneContainer(
                    slotUpdate.getWrapper(),
                    slotUpdate.getPanel(),
                    handler.getContainerSupplier(),
                    slotUpdate.getPlayer(),
                    slotUpdate.getUpgradeIndex(),
                    3,
                    3,
                    handler.getItemHandler());
                slotUpdate.setUpgradeContainer(container);
                slotSH.syncToServer(ThaumcraftSHRegistries.ARCANE_CONTAINER);

                IndexedModularSlot[] matrix = slotUpdate.get("arcane_matrix_slots");
                ModularCraftingSlot result = slotUpdate.get("arcane_result_slot");
                CraftingSlotInfo slotInfo = new CraftingSlotInfo(
                    slotUpdate.getUpgradeIndex(),
                    matrix,
                    result,
                    3,
                    3,
                    47,
                    38,
                    28,
                    27);
                slotUpdate.setCraftingInfo(slotInfo);
                slotSH.syncToServer(ThaumcraftSHRegistries.ARCANE_INFO);
            }

            @Override
            public @NotNull ExpandedUpgradeTab<ArcaneCraftingUpgradeWrapper> createExpandedTab(int slotIndex,
                ArcaneCraftingUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
                return new ArcaneCraftingUpgradeTab(slotIndex, wrapper, stack, storagePanel);
            }
        });

    public ItemArcaneCraftingUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<ArcaneCraftingUpgradeWrapper> getType() {
        return TYPE;
    }
}
