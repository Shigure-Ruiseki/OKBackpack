package ruiseki.backpack.upgrades.traverler.spider;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemSpiderUpgrade extends ItemTravelerUpgrade<SpiderUpgradeWrapper> {

    public static final UpgradeType<SpiderUpgradeWrapper> TYPE = new UpgradeType<>(SpiderUpgradeWrapper::new);

    public ItemSpiderUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<SpiderUpgradeWrapper> getType() {
        return TYPE;
    }
}
