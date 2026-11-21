package ruiseki.backpack.upgrades.traverler.cake;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemCakeUpgrade extends ItemTravelerUpgrade<CakeUpgradeWrapper> {

    public static final UpgradeType<CakeUpgradeWrapper> TYPE = new UpgradeType<>(CakeUpgradeWrapper::new);

    public ItemCakeUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<CakeUpgradeWrapper> getType() {
        return TYPE;
    }
}
