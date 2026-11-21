package ruiseki.backpack.upgrades.traverler.cactus;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemCactusUpgrade extends ItemTravelerUpgrade<CactusUpgradeWrapper> {

    public static final UpgradeType<CactusUpgradeWrapper> TYPE = new UpgradeType<>(CactusUpgradeWrapper::new);

    public ItemCactusUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<CactusUpgradeWrapper> getType() {
        return TYPE;
    }
}
