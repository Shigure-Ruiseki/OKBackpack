package ruiseki.okbackpack.common.item.restock;

import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IRestockUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.helpers.InventoryInteractionHelpers;
import ruiseki.okbackpack.common.helpers.InventoryInteractionHelpers.StackKey;
import ruiseki.okbackpack.common.item.BasicUpgradeWrapper;
import ruiseki.okbackpack.common.network.PacketStatusMessage;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class RestockUpgradeWrapper extends BasicUpgradeWrapper implements IRestockUpgrade {

    private static final String RESTOCK_FILTER_TYPE_TAG = "RestockFilterType";

    public RestockUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.restock_settings";
    }

    public RestockFilterType getRestockFilterType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, RESTOCK_FILTER_TYPE_TAG, RestockFilterType.ALLOW.ordinal());
        RestockFilterType[] types = RestockFilterType.values();
        if (ordinal < 0 || ordinal >= types.length) return RestockFilterType.ALLOW;
        return types[ordinal];
    }

    public void setRestockFilterType(RestockFilterType type) {
        if (type == null) type = RestockFilterType.ALLOW;
        ItemNBTHelpers.setInt(upgrade, RESTOCK_FILTER_TYPE_TAG, type.ordinal());
        save();
    }

    @Override
    public boolean onInteract(IInventory inventory, EntityPlayer player, ForgeDirection side) {
        if (!isEnabled()) return false;

        RestockFilterType filterType = getRestockFilterType();

        // For STORAGE filter, collect unique stacks from backpack
        final Set<StackKey> storageStacks;
        if (filterType == RestockFilterType.STORAGE) {
            storageStacks = new java.util.HashSet<>();
            for (int i = 0; i < storage.getSlots(); i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0) {
                    storageStacks.add(new StackKey(stack));
                }
            }
            if (storageStacks.isEmpty() && !hasAnyFilterItem()) return false;
        } else {
            storageStacks = null;
        }

        int transferred = InventoryInteractionHelpers.transferFromInventory(inventory, storage, stack -> {
            switch (filterType) {
                case ALLOW:
                case BLOCK:
                    return checkFilter(stack);
                case STORAGE:
                    boolean matchesStorage = storageStacks.stream()
                        .anyMatch(key -> key.matches(stack));
                    if (hasAnyFilterItem()) {
                        return matchesStorage && checkFilter(stack);
                    }
                    return matchesStorage;
                default:
                    return false;
            }
        }, side);

        if (transferred > 0) {
            OKBackpack.instance.getPacketHandler()
                .sendToPlayer(
                    new PacketStatusMessage("gui.okbackpack.status.stacks_restocked", transferred),
                    (EntityPlayerMP) player);
        } else {
            OKBackpack.instance.getPacketHandler()
                .sendToPlayer(
                    new PacketStatusMessage("gui.okbackpack.status.nothing_to_restock"),
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

        RestockFilterType filterType = getRestockFilterType();
        boolean matchesFilter = matchesAnyFilterItem(check);

        return switch (filterType) {
            case ALLOW, STORAGE -> matchesFilter;
            case BLOCK -> !matchesFilter;
        };
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
