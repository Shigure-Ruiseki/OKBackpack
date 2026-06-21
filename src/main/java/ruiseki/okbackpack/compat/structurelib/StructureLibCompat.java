package ruiseki.okbackpack.compat.structurelib;

import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.structurelib.util.InventoryUtility;

import cpw.mods.fml.common.Optional;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.init.IInitListener;

public class StructureLibCompat implements IInitListener {

    public static final String REGISTRY_KEY = "0999-okbackpack";

    public static boolean registered;

    @Override
    public void onInit(Step step) {
        if (step != Step.POSTINIT || registered || !Mods.StructureLib.isModLoaded()) return;
        register();
    }

    @Optional.Method(modid = "structurelib")
    public static void register() {
        if (registered) return;
        InventoryUtility.registerStackExtractor(REGISTRY_KEY, new StructureLibBackpackExtractor());
        registered = true;
        OKBackpack.okLog("Registered StructureLib backpack extractor");
    }

    public static final class StructureLibBackpackExtractor implements Function<ItemStack, IInventory> {

        @Override
        public IInventory apply(ItemStack stack) {
            if (!BackpackEntityHelpers.isBackpackStack(stack, false)) {
                return null;
            }
            BackpackWrapper wrapper = new BackpackWrapper(stack, (BlockBackpack.ItemBackpack) stack.getItem());
            return new StructureLibBackpackInventory(wrapper);
        }
    }

    public static final class StructureLibBackpackInventory implements IInventory {

        public final BackpackWrapper wrapper;

        public StructureLibBackpackInventory(BackpackWrapper wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public int getSizeInventory() {
            return wrapper.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return wrapper.getStackInSlot(slot);
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            return wrapper.extractItem(slot, amount, false);
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            ItemStack stack = wrapper.getStackInSlot(slot);
            if (stack != null) {
                wrapper.setStackInSlot(slot, null);
            }
            return stack;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            wrapper.setStackInSlot(slot, stack);
        }

        @Override
        public String getInventoryName() {
            return wrapper.getInventoryName();
        }

        @Override
        public boolean hasCustomInventoryName() {
            return wrapper.hasCustomInventoryName();
        }

        @Override
        public int getInventoryStackLimit() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void markDirty() {
            wrapper.markDirty();
            wrapper.writeToItem();
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {
            wrapper.writeToItem();
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return wrapper.getStackHandler()
                .isItemValid(slot, stack);
        }
    }
}
