package ruiseki.okbackpack.upgrades.toolswapper;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemToolSwapperUpgrade extends ItemUpgradeBase<ToolSwapperUpgradeWrapper> {

    private static final UpgradeType<ToolSwapperUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public ToolSwapperUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new ToolSwapperUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(ToolSwapperUpgradeWrapper wrapper, UpgradeSlotUpdate slotUpdate) {
            DelegatedFilterLogicSH filterLogic = wrapper.getSlotCount() == 9 ? slotUpdate.get("basic_filter_logic")
                : slotUpdate.get("adv_filter_logic");
            filterLogic.setFilterLogic(wrapper::getFilterLogic);
            filterLogic.syncToServer(CommonSHRegistries.FILTER);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<ToolSwapperUpgradeWrapper> createExpandedTab(int slotIndex,
            ToolSwapperUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 9
                ? new ToolSwapperUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new ToolSwapperUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });
    public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(
        new UpgradeConflictDefinition(
            ItemToolSwapperUpgrade.class::isInstance,
            0,
            LangHelpers.localize("gui.okbackpack.error.add.tool_swapper_exists")));
    private final BooleanSupplier hasSettingsTab;
    private final BooleanSupplier swapToolOnKeyPress;
    private final IntSupplier slotCount;

    public ItemToolSwapperUpgrade(IntSupplier slotCount, BooleanSupplier hasSettingsTab,
        BooleanSupplier swapToolOnKeyPress) {
        super(GeneralConfig.maxUpgradesPerStorageConfig);
        this.slotCount = slotCount;
        this.hasSettingsTab = hasSettingsTab;
        this.swapToolOnKeyPress = swapToolOnKeyPress;
    }

    @Override
    public UpgradeType<ToolSwapperUpgradeWrapper> getType() {
        return TYPE;
    }

    public int getSlotCount() {
        return slotCount.getAsInt();
    }

    public boolean hasSettingsTab() {
        return hasSettingsTab.getAsBoolean();
    }

    public boolean shouldSwapToolOnKeyPress() {
        return swapToolOnKeyPress.getAsBoolean();
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return UPGRADE_CONFLICT_DEFINITIONS;
    }
}
