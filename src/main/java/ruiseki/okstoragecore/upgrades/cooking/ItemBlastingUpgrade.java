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

public class ItemBlastingUpgrade extends ItemUpgradeBase<CookingUpgradeWrapper.BlastingUpgradeWrapper>
    implements ICookingUpgradeItem {

    public static final UpgradeType<CookingUpgradeWrapper.BlastingUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<CookingUpgradeWrapper.BlastingUpgradeWrapper>() {

            @Override
            public CookingUpgradeWrapper.BlastingUpgradeWrapper create(IStorageWrapper storageWrapper,
                ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
                return new CookingUpgradeWrapper.BlastingUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
            }

            @Override
            public void updateTabDelegates(CookingUpgradeWrapper.BlastingUpgradeWrapper wrapper,
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
            public @NotNull ExpandedUpgradeTab<CookingUpgradeWrapper.BlastingUpgradeWrapper> createExpandedTab(
                int slotIndex, CookingUpgradeWrapper.BlastingUpgradeWrapper wrapper, ItemStack stack,
                StoragePanel storagePanel) {
                return new CookingUpgradeTab<>(
                    slotIndex,
                    wrapper,
                    stack,
                    storagePanel,
                    "gui.okstoragecore.upgrades.blasting");
            }
        });

    private final CookingUpgradeConfig smeltingUpgradeConfig;

    public ItemBlastingUpgrade(CookingUpgradeConfig smeltingUpgradeConfig,
        IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.smeltingUpgradeConfig = smeltingUpgradeConfig;
    }

    @Override
    public UpgradeType<CookingUpgradeWrapper.BlastingUpgradeWrapper> getType() {
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
