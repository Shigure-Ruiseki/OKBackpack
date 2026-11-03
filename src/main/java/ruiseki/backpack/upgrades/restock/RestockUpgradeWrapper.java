package ruiseki.backpack.upgrades.restock;

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
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.upgrades.ContentsFilterLogic;
import ruiseki.okstoragecore.upgrades.IContentsFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class RestockUpgradeWrapper extends UpgradeWrapperBase<RestockUpgradeWrapper, ItemRestockUpgrade>
    implements IContentsFilteredUpgrade, IItemHandlerInteractionUpgrade {

    private final ContentsFilterLogic filterLogic;
    private final int slotCount;

    public RestockUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(backpackWrapper, upgrade, upgradeSaveHandler);
        this.slotCount = this.upgradeItem.getFilterSlotCount();
        filterLogic = new ContentsFilterLogic(
            upgrade,
            upgradeSaveHandler,
            upgradeItem.getFilterSlotCount(),
            backpackWrapper::getInventoryHandler,
            backpackWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class));
    }

    public int getSlotCount() {
        return this.slotCount;
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player) {
        AtomicInteger stacksAdded = new AtomicInteger(0);

        InventoryHelpers.transfer(
            itemHandler,
            new FilteredItemHandler<IItemHandler>(
                storageWrapper.getInventoryForUpgradeProcessing(),
                Collections.singletonList(filterLogic),
                Collections.emptyList()),
            s -> stacksAdded.incrementAndGet());

        int stacksRestocked = stacksAdded.get();
        String translKey = stacksRestocked > 0 ? "gui.okbackpack.status.stacks_restocked"
            : "gui.okbackpack.status.nothing_to_restock";
        player.addChatComponentMessage(new ChatComponentTranslation(translKey, stacksRestocked));
    }
}
