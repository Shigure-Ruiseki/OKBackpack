package ruiseki.backpack.upgrades.traverler.hay;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemHayUpgrade extends ItemTravelerUpgrade<HayUpgradeWrapper> {

    public static final UpgradeType<HayUpgradeWrapper> TYPE = new UpgradeType<>(HayUpgradeWrapper::new);

    public ItemHayUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<HayUpgradeWrapper> getType() {
        return TYPE;
    }
}
