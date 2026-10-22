package ruiseki.okstoragecore.upgrades.inception;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import ruiseki.backpack.backpack.ItemBackpack;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.TranslateHelpers;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemInceptionUpgrade extends ItemUpgradeBase<InceptionUpgradeWrapper> {

    public static final UpgradeType<InceptionUpgradeWrapper> TYPE = new UpgradeType<>(InceptionUpgradeWrapper::new);

    public ItemInceptionUpgrade() {
        super(null);
    }

    @Override
    public UpgradeType<InceptionUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack) {
        UpgradeSlotChangeResult result = super.canAddUpgradeTo(storageWrapper, upgradeStack);
        if (!result.isSuccessful()) {
            return result;
        }

        Set<Integer> errorUpgradeSlots = new HashSet<>();
        storageWrapper.getUpgradeHandler()
            .getSlotWrappers()
            .forEach((slot, wrapper) -> {
                if (wrapper instanceof InceptionUpgradeWrapper) {
                    errorUpgradeSlots.add(slot);
                }
            });

        if (!errorUpgradeSlots.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("add.inception_exists"),
                errorUpgradeSlots,
                Collections.emptySet(),
                Collections.emptySet());
        }

        return new UpgradeSlotChangeResult.Success();
    }

    @Override
    public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper) {
        Set<Integer> slots = InventoryHelpers
            .getItemSlots(storageWrapper.getInventoryHandler(), stack -> stack.getItem() instanceof ItemBackpack);
        if (!slots.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                TranslateHelpers.error("remove.inception_sub_backpack"),
                Collections.emptySet(),
                slots,
                Collections.emptySet());
        }
        return new UpgradeSlotChangeResult.Success();
    }
}
