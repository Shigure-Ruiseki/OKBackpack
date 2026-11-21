package ruiseki.okstoragecore.upgrades.filter;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okstoragecore.api.IIOFilterUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.upgrades.ContentsFilterLogic;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IContentsFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class FilterUpgradeWrapper extends UpgradeWrapperBase<FilterUpgradeWrapper, ItemFilterUpgrade>
    implements IContentsFilteredUpgrade, IIOFilterUpgrade {

    private final ContentsFilterLogic filterLogic;
    private final int slotCount;

    protected FilterUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        this.slotCount = upgradeItem.getFilterSlotCount();
        filterLogic = new ContentsFilterLogic(
            upgrade,
            upgradeSaveHandler,
            upgradeItem.getFilterSlotCount(),
            storageWrapper::getInventoryHandler,
            storageWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class));
    }

    public int getSlotCount() {
        return slotCount;
    }

    public void setDirection(Direction direction) {
        NBTHelpers.setEnumConstant(upgrade, "direction", direction);
        save();
        storageWrapper.refreshInventoryForInputOutput();
    }

    public Direction getDirection() {
        return NBTHelpers.getEnumConstant(upgrade, "direction", Direction::fromName)
            .orElse(Direction.BOTH);
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public Optional<FilterLogic> getInputFilter() {
        Direction direction = getDirection();
        return direction == Direction.INPUT || direction == Direction.BOTH ? Optional.of(getFilterLogic())
            : Optional.empty();
    }

    @Override
    public Optional<FilterLogic> getOutputFilter() {
        Direction direction = getDirection();
        return direction == Direction.OUTPUT || direction == Direction.BOTH ? Optional.of(getFilterLogic())
            : Optional.empty();
    }
}
