package ruiseki.storagecore.upgrades.inception;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.upgrades.UpgradeWrapperBase;

public class InceptionUpgradeWrapper extends UpgradeWrapperBase<InceptionUpgradeWrapper, ItemInceptionUpgrade> {

    public InceptionUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(backpackWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }
}
