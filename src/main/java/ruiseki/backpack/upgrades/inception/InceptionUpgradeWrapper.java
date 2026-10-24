package ruiseki.backpack.upgrades.inception;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

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
