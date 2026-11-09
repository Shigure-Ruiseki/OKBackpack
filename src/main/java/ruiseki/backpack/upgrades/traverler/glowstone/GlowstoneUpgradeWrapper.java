package ruiseki.backpack.upgrades.traverler.glowstone;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ILightUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class GlowstoneUpgradeWrapper extends UpgradeWrapperBase<GlowstoneUpgradeWrapper, ItemGlowstoneUpgrade>
    implements ITravelersUpgrade, ILightUpgrade {

    public GlowstoneUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public int getLightLevel() {
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
