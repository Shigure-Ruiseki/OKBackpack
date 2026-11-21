package ruiseki.backpack.upgrades.traverler.quartz;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemQuartzUpgrade extends ItemTravelerUpgrade<QuartzUpgradeWrapper> {

    public static final UpgradeType<QuartzUpgradeWrapper> TYPE = new UpgradeType<>(QuartzUpgradeWrapper::new);

    public ItemQuartzUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<QuartzUpgradeWrapper> getType() {
        return TYPE;
    }
}
