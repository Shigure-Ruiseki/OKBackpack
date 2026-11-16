package ruiseki.okstoragecore.upgrades.cooking;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.UpgradeGroup;

public interface ICookingUpgrade {

    UpgradeGroup UPGRADE_GROUP = new UpgradeGroup("furnace_upgrades", LangHelpers.localize("cooking_upgrades"));

    CookingLogic getCookingLogic();
}
