package ruiseki.backpack.upgrades.traverler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;

public abstract class ItemTravelerUpgrade<T extends IUpgradeWrapper> extends ItemUpgradeBase<T> {

    public ItemTravelerUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack) {
        UpgradeSlotChangeResult result = super.canAddUpgradeTo(storageWrapper, upgradeStack);
        if (!result.isSuccessful()) {
            return result;
        }

        Set<Integer> errorUpgradeSlots = new HashSet<>();
        this.findUpgrades(errorUpgradeSlots, storageWrapper, upgrade -> upgrade instanceof ITravelersUpgrade);

        if (!errorUpgradeSlots.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                LangHelpers.localize("gui.okbackpack.error.add.traverler_exists"),
                errorUpgradeSlots,
                Collections.emptySet(),
                Collections.emptySet());
        }

        return new UpgradeSlotChangeResult.Success();
    }
}
