package ruiseki.okstoragecore.upgrades.cooking;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFloatSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeGroup;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemSmeltingUpgrade extends ItemUpgradeBase<CookingUpgradeWrapper.SmeltingUpgradeWrapper>
    implements ICookingUpgradeItem {

    public static final UpgradeType<CookingUpgradeWrapper.SmeltingUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<CookingUpgradeWrapper.SmeltingUpgradeWrapper>() {

            @Override
            public CookingUpgradeWrapper.SmeltingUpgradeWrapper create(IStorageWrapper storageWrapper,
                ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
                return new CookingUpgradeWrapper.SmeltingUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
            }

            @Override
            public void updateTabDelegates(CookingUpgradeWrapper.SmeltingUpgradeWrapper wrapper,
                UpgradeSlotUpdate slotUpdate) {
                DelegatedItemHandlerSH cookingHandler = slotUpdate.get("cooking_handler");
                cookingHandler.setItemHandler(
                    () -> wrapper.getCookingLogic()
                        .getCookingInventory());
                cookingHandler.syncToServer(CommonSHRegistries.COOKING_STORAGE);

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
            public @NotNull ExpandedUpgradeTab<CookingUpgradeWrapper.SmeltingUpgradeWrapper> createExpandedTab(
                int slotIndex, CookingUpgradeWrapper.SmeltingUpgradeWrapper wrapper, ItemStack stack,
                StoragePanel storagePanel) {
                return new CookingUpgradeTab<>(
                    slotIndex,
                    wrapper,
                    stack,
                    storagePanel,
                    "gui.okstoragecore.upgrades.smelting");
            }
        });

    private final CookingUpgradeConfig smeltingUpgradeConfig;

    public ItemSmeltingUpgrade(CookingUpgradeConfig smeltingUpgradeConfig,
        IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.smeltingUpgradeConfig = smeltingUpgradeConfig;
    }

    @Override
    public UpgradeType<CookingUpgradeWrapper.SmeltingUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }

    @Override
    public CookingUpgradeConfig getCookingUpgradeConfig() {
        return smeltingUpgradeConfig;
    }

    @Override
    public UpgradeGroup getUpgradeGroup() {
        return ICookingUpgrade.UPGRADE_GROUP;
    }
}
