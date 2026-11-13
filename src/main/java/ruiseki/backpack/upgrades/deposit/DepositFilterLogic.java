package ruiseki.backpack.upgrades.deposit;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okstoragecore.upgrades.FilterLogic;

public class DepositFilterLogic extends FilterLogic {

    private Set<ItemStackKey> inventoryFilterStacks = new HashSet<>();

    public DepositFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount) {
        super(upgrade, saveHandler, filterSlotCount);
    }

    public DepositFilterType getDepositFilterType() {
        if (shouldFilterByInventory()) {
            return DepositFilterType.INVENTORY;
        }
        return isAllowList() ? DepositFilterType.ALLOW : DepositFilterType.BLOCK;
    }

    public void setDepositFilterType(DepositFilterType depositFilterType) {
        switch (depositFilterType) {
            case ALLOW:
                setFilterByInventory(false);
                setAllowList(true);
                break;
            case BLOCK:
                setFilterByInventory(false);
                setAllowList(false);
                break;
            case INVENTORY:
            default:
                setFilterByInventory(true);
                save();
        }
    }

    public void setInventory(IItemHandler inventory) {
        inventoryFilterStacks = InventoryHelpers.getUniqueStacks(inventory);
    }

    @Override
    public boolean matchesFilter(ItemStack stack) {
        if (!shouldFilterByInventory()) {
            return super.matchesFilter(stack);
        }

        for (ItemStackKey filterStack : inventoryFilterStacks) {
            if (stackMatchesFilter(stack, filterStack.getStack())) {
                return true;
            }
        }
        return false;
    }

    private void setFilterByInventory(boolean filterByInventory) {
        NBTHelpers.setBoolean(upgrade, "filterByInventory", filterByInventory);
        save();
    }

    private boolean shouldFilterByInventory() {
        return NBTHelpers.getBoolean(upgrade, "filterByInventory")
            .orElse(false);
    }
}
