package ruiseki.okstoragecore.upgrades.pickup;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.upgrades.ContentsFilterLogic;
import ruiseki.okstoragecore.upgrades.IContentsFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.IPickupResponseUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class PickupUpgradeWrapper extends UpgradeWrapperBase<PickupUpgradeWrapper, ItemPickupUpgrade>
    implements IPickupResponseUpgrade, IContentsFilteredUpgrade {

    private final ContentsFilterLogic filterLogic;
    private final int slotCount;

    protected PickupUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        this.slotCount = upgradeItem.getFilterSlotCount();
        filterLogic = new ContentsFilterLogic(
            upgrade,
            stack -> save(),
            upgradeItem.getFilterSlotCount(),
            storageWrapper::getInventoryHandler,
            storageWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class));
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public ItemStack pickup(World world, ItemStack stack, boolean simulate) {
        if (!filterLogic.matchesFilter(stack)) return stack;
        return storageWrapper.getInventoryForUpgradeProcessing()
            .insertItem(stack, simulate);
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }
}
