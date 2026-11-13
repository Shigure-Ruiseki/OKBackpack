package ruiseki.backpack.upgrades.traverler.chicken;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemChickenUpgrade extends ItemTravelerUpgrade<ChickenUpgradeWrapper> {

    public static final UpgradeType<ChickenUpgradeWrapper> TYPE = new UpgradeType<>(ChickenUpgradeWrapper::new);

    public ItemChickenUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<ChickenUpgradeWrapper> getType() {
        return TYPE;
    }
}
