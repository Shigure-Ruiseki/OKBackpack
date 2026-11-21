package ruiseki.backpack.upgrades.traverler.magma;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemMagmaCubeUpgrade extends ItemTravelerUpgrade<MagmaCubeUpgradeWrapper> {

    public static final UpgradeType<MagmaCubeUpgradeWrapper> TYPE = new UpgradeType<>(MagmaCubeUpgradeWrapper::new);

    public ItemMagmaCubeUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<MagmaCubeUpgradeWrapper> getType() {
        return TYPE;
    }
}
