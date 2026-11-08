package ruiseki.backpack.upgrades.traverler.blaze;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemBlazeUpgrade extends ItemTravelerUpgrade<BlazeUpgradeWrapper> {

    public static final UpgradeType<BlazeUpgradeWrapper> TYPE = new UpgradeType<>(BlazeUpgradeWrapper::new);

    public ItemBlazeUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<BlazeUpgradeWrapper> getType() {
        return TYPE;
    }
}
