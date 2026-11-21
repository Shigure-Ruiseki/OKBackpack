package ruiseki.backpack.upgrades.traverler.glowstone;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemGlowstoneUpgrade extends ItemTravelerUpgrade<GlowstoneUpgradeWrapper> {

    public static final UpgradeType<GlowstoneUpgradeWrapper> TYPE = new UpgradeType<>(GlowstoneUpgradeWrapper::new);

    public ItemGlowstoneUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<GlowstoneUpgradeWrapper> getType() {
        return TYPE;
    }
}
