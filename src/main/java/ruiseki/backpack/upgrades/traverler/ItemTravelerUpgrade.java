package ruiseki.backpack.upgrades.traverler;

import java.util.List;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;

public abstract class ItemTravelerUpgrade<T extends IUpgradeWrapper> extends ItemUpgradeBase<T> {

    public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(
        new UpgradeConflictDefinition(
            ItemTravelerUpgrade.class::isInstance,
            0,
            LangHelpers.localize("gui.okbackpack.error.add.traverler_exists")));

    public ItemTravelerUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return UPGRADE_CONFLICT_DEFINITIONS;
    }
}
