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
        this.slotCount = this.upgradeItem.getFilterSlotCount();
        this.filterLogic = new ContentsFilterLogic(
            upgrade,
            upgradeSaveHandler,
            this.upgradeItem.getFilterSlotCount(),
            storageWrapper::getInventoryHandler,
            storageWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class));
    }

    public int getSlotCount() {
        return this.slotCount;
    }

    @Override
    public ItemStack pickup(World world, ItemStack stack, boolean simulate) {
        if (!this.filterLogic.matchesFilter(stack)) return stack;
        return this.storageWrapper.getInventoryForUpgradeProcessing()
            .insertItem(stack, simulate);
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }
}
