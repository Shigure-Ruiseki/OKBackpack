package ruiseki.backpack.upgrades.traverler.wolf;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemWolfUpgrade extends ItemTravelerUpgrade<WolfUpgradeWrapper> {

    public static final UpgradeType<WolfUpgradeWrapper> TYPE = new UpgradeType<>(WolfUpgradeWrapper::new);

    public ItemWolfUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<WolfUpgradeWrapper> getType() {
        return TYPE;
    }
}
