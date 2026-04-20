package ruiseki.okbackpack.common.item.deposit;

import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IDepositUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.helpers.InventoryInteractionHelpers;
import ruiseki.okbackpack.common.helpers.InventoryInteractionHelpers.StackKey;
import ruiseki.okbackpack.common.item.BasicUpgradeWrapper;
import ruiseki.okbackpack.common.network.PacketStatusMessage;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class DepositUpgradeWrapper extends BasicUpgradeWrapper implements IDepositUpgrade {

    private static final String DEPOSIT_FILTER_TYPE_TAG = "DepositFilterType";

    public DepositUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.deposit_settings";
    }

    public DepositFilterType getDepositFilterType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, DEPOSIT_FILTER_TYPE_TAG, DepositFilterType.ALLOW.ordinal());
        DepositFilterType[] types = DepositFilterType.values();
        if (ordinal < 0 || ordinal >= types.length) return DepositFilterType.ALLOW;
        return types[ordinal];
    }

    public void setDepositFilterType(DepositFilterType type) {
        if (type == null) type = DepositFilterType.ALLOW;
        ItemNBTHelpers.setInt(upgrade, DEPOSIT_FILTER_TYPE_TAG, type.ordinal());
        save();
    }

    @Override
    public boolean onInteract(IInventory inventory, EntityPlayer player, ForgeDirection side) {
        if (!isEnabled()) return false;

        DepositFilterType filterType = getDepositFilterType();

        // For INVENTORY filter, collect unique stacks from target container
        final Set<StackKey> inventoryStacks;
        if (filterType == DepositFilterType.INVENTORY) {
            inventoryStacks = InventoryInteractionHelpers.getUniqueStacks(inventory);
            if (inventoryStacks.isEmpty()) return false;
        } else {
            inventoryStacks = null;
        }

        int transferred = InventoryInteractionHelpers.transferToInventory(storage, inventory, stack -> {
            switch (filterType) {
                case ALLOW:
                    return checkFilter(stack);
                case BLOCK:
                    return checkFilter(stack);
                case INVENTORY:
                    boolean matchesInventory = inventoryStacks.stream()
                        .anyMatch(key -> key.matches(stack));
                    if (hasAnyFilterItem()) {
                        return matchesInventory && checkFilter(stack);
                    }
                    return matchesInventory;
                default:
                    return false;
            }
        }, side);

        if (transferred > 0) {
            OKBackpack.instance.getPacketHandler()
                .sendToPlayer(
                    new PacketStatusMessage("gui.okbackpack.status.stacks_deposited", transferred),
                    (EntityPlayerMP) player);
        } else {
            OKBackpack.instance.getPacketHandler()
                .sendToPlayer(
                    new PacketStatusMessage("gui.okbackpack.status.nothing_to_deposit"),
                    (EntityPlayerMP) player);
        }

        return transferred > 0;
    }

    private boolean hasAnyFilterItem() {
        BaseItemStackHandler filterItems = getFilterItems();
        for (int i = 0; i < filterItems.getSlots(); i++) {
            if (filterItems.getStackInSlot(i) != null) return true;
        }
        return false;
    }

    @Override
    public boolean checkFilter(ItemStack check) {
        if (!isEnabled()) return false;
        if (!hasAnyFilterItem()) return true;

        DepositFilterType filterType = getDepositFilterType();
        boolean matchesFilter = matchesAnyFilterItem(check);

        switch (filterType) {
            case ALLOW:
            case INVENTORY:
                return matchesFilter;
            case BLOCK:
                return !matchesFilter;
            default:
                return false;
        }
    }

    private boolean matchesAnyFilterItem(ItemStack check) {
        BaseItemStackHandler filterItems = getFilterItems();
        for (int i = 0; i < filterItems.getSlots(); i++) {
            ItemStack filterStack = filterItems.getStackInSlot(i);
            if (filterStack != null && filterStack.isItemEqual(check)) return true;
        }
        return false;
    }
}
