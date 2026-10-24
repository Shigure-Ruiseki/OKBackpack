package ruiseki.okstoragecore.upgrades.crafting;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.ExpandedTab;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
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
                if (handler == null) return;
                handler.setItemHandler(wrapper::getStorage);
                handler.syncToServer(DelegatedItemHandlerSHRegisters.STORAGE);
            }

            @Override
            public ExpandedTab createExpandedTab(int slotIndex, CraftingUpgradeWrapper wrapper, ItemStack stack,
                IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey) {
                return new CraftingUpgradeTab(slotIndex, wrapper, stack, storageWrapper, storagePanel, titleKey);
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
