package ruiseki.backpack.upgrades.traverler.quiver;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemQuiverUpgrade extends ItemTravelerUpgrade<QuiverUpgradeWrapper> {

    public static final UpgradeType<QuiverUpgradeWrapper> TYPE = new UpgradeType<>(QuiverUpgradeWrapper::new);

    public ItemQuiverUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<QuiverUpgradeWrapper> getType() {
        return TYPE;
    }
}
