package ruiseki.backpack.upgrades.deposit;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import ruiseki.backpack.api.IItemHandlerInteractionUpgrade;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.inventory.FilteredItemHandler;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class DepositUpgradeWrapper extends UpgradeWrapperBase<DepositUpgradeWrapper, ItemDepositUpgrade>
    implements IFilteredUpgrade, IItemHandlerInteractionUpgrade {

    private final DepositFilterLogic filterLogic;
    private final int slotCount;

    public DepositUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(backpackWrapper, upgrade, upgradeSaveHandler);
        this.slotCount = upgradeItem.getFilterSlotCount();
        filterLogic = new DepositFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount());
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public DepositFilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player) {
        if (filterLogic.getDepositFilterType() == DepositFilterType.INVENTORY) {
            filterLogic.setInventory(itemHandler);
        }
        AtomicInteger stacksAdded = new AtomicInteger(0);

        InventoryHelpers.transfer(
            storageWrapper.getInventoryForUpgradeProcessing(),
            new FilteredItemHandler<>(itemHandler, Collections.singletonList(filterLogic), Collections.emptyList()),
            s -> stacksAdded.incrementAndGet());

        int stacksDeposited = stacksAdded.get();
        String translKey = stacksDeposited > 0 ? "gui.sophisticatedbackpacks.status.stacks_deposited"
            : "gui.sophisticatedbackpacks.status.nothing_to_deposit";
        player.addChatComponentMessage(new ChatComponentTranslation(translKey, stacksDeposited));
    }
}
