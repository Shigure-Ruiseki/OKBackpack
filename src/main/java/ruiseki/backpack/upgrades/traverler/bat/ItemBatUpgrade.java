package ruiseki.backpack.upgrades.traverler.bat;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemBatUpgrade extends ItemTravelerUpgrade<BatUpgradeWrapper> {

    public static final UpgradeType<BatUpgradeWrapper> TYPE = new UpgradeType<>(BatUpgradeWrapper::new);

    public ItemBatUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<BatUpgradeWrapper> getType() {
        return TYPE;
    }
}
