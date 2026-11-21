package ruiseki.backpack.upgrades.traverler.redstone;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.IRedstoneUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class RedstoneUpgradeWrapper extends UpgradeWrapperBase<RedstoneUpgradeWrapper, ItemRedstoneUpgrade>
    implements ITravelersUpgrade, IRedstoneUpgrade {

    public RedstoneUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public int getRedstonePower() {
        return 15;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }
}
