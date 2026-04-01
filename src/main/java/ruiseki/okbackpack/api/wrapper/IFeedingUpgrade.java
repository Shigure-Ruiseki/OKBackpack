package ruiseki.okbackpack.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.IItemHandler;

public interface IFeedingUpgrade extends ITickable {

    int getFoodSlot(IItemHandler handler, int foodLevel, float health, float maxHealth);

    default boolean feed(EntityPlayer entity, IItemHandler handler) {

        if (!entity.canEat(false)) return false;

        int slot = getFoodSlot(
            handler,
            entity.getFoodStats()
                .getFoodLevel(),
            entity.getHealth(),
            entity.getMaxHealth());

        if (slot < 0 || slot >= handler.getSlots()) {
            return false;
        }

        ItemStack food = handler.extractItem(slot, 1, false);
        if (food == null || !(food.getItem() instanceof ItemFood foodItem)) {
            return false;
        }

        // Create an intercepting inventory to capture container items added by onEaten()
        InterceptingInventory interceptor = new InterceptingInventory(entity);
        InventoryPlayer originalInventory = entity.inventory;

        try {
            // Temporarily replace the player's inventory with our interceptor
            entity.inventory = interceptor;

            // Call onEaten to let vanilla/mod handle food consumption
            foodItem.onEaten(food, entity.worldObj, entity);

        } finally {
            // Always restore the original inventory
            entity.inventory = originalInventory;
        }

        // Process captured container items
        for (ItemStack capturedItem : interceptor.getCapturedItems()) {
            // Try to insert into backpack
            ItemStack remaining = capturedItem;
            for (int i = 0; i < handler.getSlots(); i++) {
                remaining = handler.insertItem(i, remaining, false);
                if (remaining == null || remaining.stackSize == 0) {
                    break; // Successfully inserted
                }
            }

            // If couldn't insert into backpack, add to player inventory or drop
            if (remaining != null && remaining.stackSize > 0) {
                if (!entity.inventory.addItemStackToInventory(remaining)) {
                    entity.dropPlayerItemWithRandomChoice(remaining, false);
                }
            }
        }

        return true;
    }

    /**
     * Custom inventory that intercepts items being added and delegates everything else to the original inventory.
     */
    class InterceptingInventory extends InventoryPlayer {

        private final InventoryPlayer original;
        private final List<ItemStack> capturedItems = new ArrayList<>();

        public InterceptingInventory(EntityPlayer player) {
            super(player);
            this.original = player.inventory;
        }

        @Override
        public boolean addItemStackToInventory(ItemStack stack) {
            if (stack != null && stack.stackSize > 0) {
                // Capture the item instead of adding it to inventory
                capturedItems.add(stack.copy());
                return true; // Pretend we successfully added it
            }
            return false;
        }

        public List<ItemStack> getCapturedItems() {
            return capturedItems;
        }

        // Delegate all other methods to the original inventory
        @Override
        public ItemStack getStackInSlot(int index) {
            return original.getStackInSlot(index);
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {
            return original.decrStackSize(index, count);
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int index) {
            return original.getStackInSlotOnClosing(index);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            original.setInventorySlotContents(index, stack);
        }

        @Override
        public int getSizeInventory() {
            return original.getSizeInventory();
        }

        @Override
        public String getInventoryName() {
            return original.getInventoryName();
        }

        @Override
        public boolean hasCustomInventoryName() {
            return original.hasCustomInventoryName();
        }

        @Override
        public int getInventoryStackLimit() {
            return original.getInventoryStackLimit();
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return original.isUseableByPlayer(player);
        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return original.isItemValidForSlot(index, stack);
        }
    }

    class FeedingStrategy {

        public enum Hunger {
            FULL,
            HALF,
            ALWAYS;
        }

        public enum HEALTH {
            ALWAYS,
            IGNORE;
        }
    }
}
