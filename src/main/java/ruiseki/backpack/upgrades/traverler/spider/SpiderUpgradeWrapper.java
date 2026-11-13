package ruiseki.backpack.upgrades.traverler.spider;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.backpack.api.ILivingClimbingResponseUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.event.entity.LivingClimbEvent;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class SpiderUpgradeWrapper extends UpgradeWrapperBase<SpiderUpgradeWrapper, ItemSpiderUpgrade>
    implements ITravelersUpgrade, ILivingClimbingResponseUpgrade {

    public SpiderUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }

    @Override
    public boolean onLivingClimb(LivingClimbEvent event) {
        event.setCanClimb(this.isEnabled());
        return false;
    }
}
