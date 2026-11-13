package ruiseki.backpack.upgrades.traverler.creeper;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemCreeperUpgrade extends ItemTravelerUpgrade<CreeperUpgradeWrapper> {

    public static final UpgradeType<CreeperUpgradeWrapper> TYPE = new UpgradeType<>(CreeperUpgradeWrapper::new);

    public ItemCreeperUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<CreeperUpgradeWrapper> getType() {
        return TYPE;
    }
}
