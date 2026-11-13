package ruiseki.backpack.upgrades.anvil;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class AnvilUpgradeWrapper extends UpgradeWrapperBase<AnvilUpgradeWrapper, ItemAnvilUpgrade>
    implements IStorageUpgrade {

    private final ItemStackHandler inventory;

    protected AnvilUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);

        inventory = new ItemStackHandler(2) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                NBTHelpers.setCompoundTag(upgrade, "craftingInventory", this.serializeNBT());
                save();
            }
        };
        NBTHelpers.getCompoundTag(upgrade, "craftingInventory")
            .ifPresent(this.inventory::deserializeNBT);
    }

    @Override
    public IItemHandlerModifiable getStorage() {
        return this.inventory;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    public boolean shouldShiftClickIntoStorage() {
        return NBTHelpers.getBoolean(this.upgrade, "shiftClickIntoStorage")
            .orElse(true);
    }

    public void setShiftClickIntoStorage(boolean shiftClickIntoStorage) {
        NBTHelpers.setBoolean(this.upgrade, "shiftClickIntoStorage", shiftClickIntoStorage);
        this.save();
    }

    public String getItemName() {
        return NBTHelpers.getString(this.upgrade, "itemName")
            .orElse("");
    }

    public void setItemName(String itemName) {
        NBTHelpers.setString(NBTHelpers.getOrCreateTag(this.upgrade), "itemName", itemName);
        this.save();
    }
}
