package ruiseki.backpack.upgrades.traverler.dragon;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemDragonUpgrade extends ItemTravelerUpgrade<DragonUpgradeWrapper> {

    public static final UpgradeType<DragonUpgradeWrapper> TYPE = new UpgradeType<>(DragonUpgradeWrapper::new);

    public ItemDragonUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<DragonUpgradeWrapper> getType() {
        return TYPE;
    }
}
