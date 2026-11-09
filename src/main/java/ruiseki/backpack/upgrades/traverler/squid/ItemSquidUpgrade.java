package ruiseki.backpack.upgrades.traverler.squid;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemSquidUpgrade extends ItemTravelerUpgrade<SquidUpgradeWrapper> {

    public static final UpgradeType<SquidUpgradeWrapper> TYPE = new UpgradeType<>(SquidUpgradeWrapper::new);

    public ItemSquidUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<SquidUpgradeWrapper> getType() {
        return TYPE;
    }
}
