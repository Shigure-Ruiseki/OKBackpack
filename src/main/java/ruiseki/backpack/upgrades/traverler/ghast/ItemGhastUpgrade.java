package ruiseki.backpack.upgrades.traverler.ghast;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemGhastUpgrade extends ItemTravelerUpgrade<GhastUpgradeWrapper> {

    public static final UpgradeType<GhastUpgradeWrapper> TYPE = new UpgradeType<>(GhastUpgradeWrapper::new);

    public ItemGhastUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<GhastUpgradeWrapper> getType() {
        return TYPE;
    }
}
