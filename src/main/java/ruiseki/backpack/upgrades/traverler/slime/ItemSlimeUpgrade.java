package ruiseki.backpack.upgrades.traverler.slime;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemSlimeUpgrade extends ItemTravelerUpgrade<SlimeUpgradeWrapper> {

    public static final UpgradeType<SlimeUpgradeWrapper> TYPE = new UpgradeType<>(SlimeUpgradeWrapper::new);

    public ItemSlimeUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<SlimeUpgradeWrapper> getType() {
        return TYPE;
    }
}
