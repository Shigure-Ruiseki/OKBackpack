package ruiseki.okstoragecore.upgrades.cooking;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFloatSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeGroup;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemAutoSmeltingUpgrade extends ItemUpgradeBase<AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper>
    implements IAutoCookingUpgradeItem {

    public static final UpgradeType<AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper>() {

            @Override
            public AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper create(IStorageWrapper storageWrapper,
                ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
                return new AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper(
                    storageWrapper,
                    upgrade,
                    upgradeSaveHandler);
            }

            @Override
            public void updateTabDelegates(AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper wrapper,
                UpgradeSlotUpdate slotUpdate) {
                DelegatedItemHandlerSH cookingHandler = slotUpdate.get("cooking_handler");
                cookingHandler.setItemHandler(
                    () -> wrapper.getCookingLogic()
                        .getCookingInventory());
                cookingHandler.syncToServer(CommonSHRegistries.COOKING_STORAGE);

                DelegatedFilterLogicSH filterLogic = slotUpdate.get("adv_filter_logic");
                filterLogic.setFilterLogic(wrapper::getInputFilterLogic);
                filterLogic.syncToServer(CommonSHRegistries.INPUT_FILTER);

                DelegatedFilterLogicSH fuelLogic = slotUpdate.get("fuel_filter_logic");
                fuelLogic.setFilterLogic(wrapper::getFuelFilterLogic);
                fuelLogic.syncToServer(CommonSHRegistries.FUEL_FILTER);

                DelegatedFloatSH cookingProgressHandler = slotUpdate.get("cooking_cook_progress");
                cookingProgressHandler.setDelegated(
                    () -> wrapper.getCookingLogic()
                        .getCookProgress(slotUpdate.getPlayer().worldObj));
                cookingProgressHandler.syncToServer(CommonSHRegistries.COOKING_COOK);

                DelegatedFloatSH burnProgressHandler = slotUpdate.get("cooking_burn_progress");
                burnProgressHandler.setDelegated(
                    () -> wrapper.getCookingLogic()
                        .getBurnProgress(slotUpdate.getPlayer().worldObj));
                burnProgressHandler.syncToServer(CommonSHRegistries.COOKING_BURN);
            }

            @Override
            public @NotNull ExpandedUpgradeTab<AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper> createExpandedTab(
                int slotIndex, AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper wrapper, ItemStack stack,
                StoragePanel storagePanel) {
                return new AutoCookingUpgradeTab<>(
                    slotIndex,
                    wrapper,
                    stack,
                    storagePanel,
                    "gui.okstoragecore.upgrades.auto_smelting");
            }
        });
    private final AutoCookingUpgradeConfig autoSmeltingUpgradeConfig;

    public ItemAutoSmeltingUpgrade(AutoCookingUpgradeConfig autoSmeltingUpgradeConfig,
        IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.autoSmeltingUpgradeConfig = autoSmeltingUpgradeConfig;
    }

    @Override
    public UpgradeType<AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }

    @Override
    public AutoCookingUpgradeConfig getAutoCookingUpgradeConfig() {
        return autoSmeltingUpgradeConfig;
    }

    @Override
    public UpgradeGroup getUpgradeGroup() {
        return ICookingUpgrade.UPGRADE_GROUP;
    }
}
