package ruiseki.backpack.upgrades.traverler.lapis;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemLapisUpgrade extends ItemTravelerUpgrade<LapisUpgradeWrapper> {

    public static final UpgradeType<LapisUpgradeWrapper> TYPE = new UpgradeType<>(LapisUpgradeWrapper::new);

    public ItemLapisUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<LapisUpgradeWrapper> getType() {
        return TYPE;
    }
}
