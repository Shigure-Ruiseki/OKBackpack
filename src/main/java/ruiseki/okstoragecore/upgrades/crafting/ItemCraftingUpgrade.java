package ruiseki.okstoragecore.upgrades.crafting;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemCraftingUpgrade extends ItemUpgradeBase<CraftingUpgradeWrapper> {

    public static final UpgradeType<CraftingUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<CraftingUpgradeWrapper>() {

            @Override
            public CraftingUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
                Consumer<ItemStack> upgradeSaveHandler) {
                return new CraftingUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
            }

            @Override
            public void updateTabDelegates(CraftingUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
                DelegatedItemHandlerSH handler = group.get("crafting_handler");
                handler.setItemHandler(wrapper::getStorage);
            }

            @Override
            public void updateContainer(CraftingUpgradeWrapper wrapper, UpgradeSlotUpdate group) {
                DelegatedItemHandlerSH handler = group.get("crafting_handler");
                CraftingContainer container = new CraftingContainer(
                    group.getWrapper(),
                    group.getPanel(),
                    handler.getContainerSupplier(),
                    group.getPlayer(),
                    group.getUpgradeIndex(),
                    3,
                    3,
                    handler.getItemHandler());
                group.setUpgradeContainer(container);
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
}
