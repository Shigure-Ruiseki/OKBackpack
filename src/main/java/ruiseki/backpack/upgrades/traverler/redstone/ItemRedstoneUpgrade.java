package ruiseki.backpack.upgrades.traverler.redstone;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemRedstoneUpgrade extends ItemTravelerUpgrade<RedstoneUpgradeWrapper> {

    public static final UpgradeType<RedstoneUpgradeWrapper> TYPE = new UpgradeType<>(RedstoneUpgradeWrapper::new);

    public ItemRedstoneUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<RedstoneUpgradeWrapper> getType() {
        return TYPE;
    }
}
