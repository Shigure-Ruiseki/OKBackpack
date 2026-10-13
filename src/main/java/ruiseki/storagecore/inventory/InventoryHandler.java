package ruiseki.storagecore.inventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.storagecore.api.IStorageWrapper;

public class InventoryHandler extends ItemStackHandler {

    public static final String INVENTORY_TAG = "inventory";
    private static final String REAL_COUNT_TAG = "realCount";
    private final IStorageWrapper wrapper;
    private final NBTTagCompound contentsNbt;
    private final Runnable saveHandler;
    private final List<IntConsumer> onContentsChangedListeners = new ArrayList<>();
    private boolean persistent = true;
    private final Map<Integer, NBTTagCompound> stackNbts = new LinkedHashMap<>();

    public InventoryHandler(int numberOfInventorySlots, IStorageWrapper wrapper, NBTTagCompound contentsNbt,
        Runnable saveHandler) {
        super(numberOfInventorySlots);
        this.wrapper = wrapper;
        this.contentsNbt = contentsNbt;
        this.saveHandler = saveHandler;
        deserializeNBT(contentsNbt.getCompoundTag(INVENTORY_TAG));
        initStackNbts();
    }

    @Override
    public void setSize(int size) {
        super.setSize(this.stacks.size());
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    private void initStackNbts() {
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack slotStack = stacks.get(slot);
            if (!ItemHelpers.isEmpty(slotStack)) {
                stackNbts.put(slot, getSlotsStackNbt(slot, slotStack));
            }
        }
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (persistent && updateSlotNbt(slot)) {
            saveInventory();
            triggerOnChangeListeners(slot);
        }
    }

    public void triggerOnChangeListeners(int slot) {
        for (IntConsumer onContentsChangedListener : onContentsChangedListeners) {
            onContentsChangedListener.accept(slot);
        }
    }

    private boolean updateSlotNbt(int slot) {
        ItemStack slotStack = getStackInSlot(slot);
        if (ItemHelpers.isEmpty(slotStack)) {
            if (stackNbts.containsKey(slot)) {
                stackNbts.remove(slot);
                return true;
            }
        } else {
            NBTTagCompound itemTag = getSlotsStackNbt(slot, slotStack);
            if (!stackNbts.containsKey(slot) || !stackNbts.get(slot)
                .equals(itemTag)) {
                stackNbts.put(slot, itemTag);
                return true;
            }
        }
        return false;
    }

    private NBTTagCompound getSlotsStackNbt(int slot, ItemStack slotStack) {
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setInteger("Slot", slot);
        itemTag.setInteger(REAL_COUNT_TAG, slotStack.stackSize);
        slotStack.writeToNBT(itemTag);
        return itemTag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        setSize(nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.size());
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                ItemStack slotStack = ItemStack.loadItemStackFromNBT(itemTags);
                if (itemTags.hasKey(REAL_COUNT_TAG)) {
                    ItemHelpers.setCount(slotStack, itemTags.getInteger(REAL_COUNT_TAG));
                }
                stacks.set(slot, slotStack);
            }
        }
        onLoad();
    }

    public void saveInventory() {
        contentsNbt.setTag(INVENTORY_TAG, serializeNBT());
        saveHandler.run();
    }

    public void addListener(IntConsumer onContentsChanged) {
        onContentsChangedListeners.add(onContentsChanged);
    }

    public void clearListeners() {
        onContentsChangedListeners.clear();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList nbtTagList = new NBTTagList();
        for (NBTTagCompound tag : stackNbts.values()) {
            nbtTagList.appendTag(tag);
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", getSlots());
        return nbt;
    }

    public void changeSlots(int diff) {
        List<ItemStack> previousStacks = stacks;
        stacks = NonNullList.withSize(previousStacks.size() + diff, ItemHelpers.EMPTY);
        for (int slot = 0; slot < previousStacks.size() && slot < stacks.size(); slot++) {
            stacks.set(slot, previousStacks.get(slot));
        }
        initStackNbts();
        saveInventory();
    }

    public void onInit() {}
}
