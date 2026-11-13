package ruiseki.backpack.upgrades.traverler.wither;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemWitherUpgrade extends ItemTravelerUpgrade<WitherUpgradeWrapper> {

    public static final UpgradeType<WitherUpgradeWrapper> TYPE = new UpgradeType<>(WitherUpgradeWrapper::new);

    public ItemWitherUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<WitherUpgradeWrapper> getType() {
        return TYPE;
    }
}
