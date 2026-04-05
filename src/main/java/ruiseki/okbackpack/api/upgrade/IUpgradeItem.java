package ruiseki.okbackpack.api.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;

public interface IUpgradeItem<W extends IUpgradeWrapper> {

    W createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer);

    void updateWidgetDelegates(W wrapper, UpgradeSlotUpdateGroup group);

    ExpandedTabWidget getExpandedTabWidget(int slotIndex, W wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey);

    /**
     * Checks if this upgrade can be added to the given storage wrapper.
     *
     * @param storageWrapper the storage to add the upgrade to
     * @param upgradeStack   the upgrade item stack being added
     * @param targetSlot     the slot index being placed into (used to exclude from counting)
     * @return result indicating success or failure with an error lang key
     */
    default UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack,
        int targetSlot) {
        return UpgradeSlotChangeResult.success();
    }

    /**
     * Counts how many upgrades of the given item class are already installed, excluding the target slot.
     */
    static int countUpgrades(IStorageWrapper wrapper, int excludeSlot, Class<? extends Item> itemClass) {
        UpgradeItemStackHandler handler = wrapper.getUpgradeHandler();
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null && itemClass.isInstance(stack.getItem())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts how many upgrades matching any of the given item classes are already installed, excluding the target slot.
     */
    @SafeVarargs
    static int countUpgrades(IStorageWrapper wrapper, int excludeSlot, Class<? extends Item>... itemClasses) {
        UpgradeItemStackHandler handler = wrapper.getUpgradeHandler();
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null) {
                for (Class<? extends Item> cls : itemClasses) {
                    if (cls.isInstance(stack.getItem())) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Finds slot indices of upgrades matching the given item class, excluding the target slot.
     */
    static int[] findConflictSlots(IStorageWrapper wrapper, int excludeSlot, Class<? extends Item> itemClass) {
        UpgradeItemStackHandler handler = wrapper.getUpgradeHandler();
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null && itemClass.isInstance(stack.getItem())) {
                slots.add(i);
            }
        }
        return slots.stream()
            .mapToInt(Integer::intValue)
            .toArray();
    }

    /**
     * Finds slot indices of upgrades matching any of the given item classes, excluding the target slot.
     */
    @SafeVarargs
    static int[] findConflictSlots(IStorageWrapper wrapper, int excludeSlot, Class<? extends Item>... itemClasses) {
        UpgradeItemStackHandler handler = wrapper.getUpgradeHandler();
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null) {
                for (Class<? extends Item> cls : itemClasses) {
                    if (cls.isInstance(stack.getItem())) {
                        slots.add(i);
                        break;
                    }
                }
            }
        }
        return slots.stream()
            .mapToInt(Integer::intValue)
            .toArray();
    }
}
