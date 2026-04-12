package ruiseki.okbackpack.common.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.common.block.BackpackWrapper;

public class BackpackSettingsTemplate {

    public static final String KEEP_TAB_TAG = "KeepTab";
    public static final String SHIFT_CLICK_INTO_OPEN_TAB_TAG = "ShiftClickIntoOpenTab";
    public static final String KEEP_SEARCH_PHRASE_TAG = "KeepSearchPhrase";
    public static final String LOCKED_BACKPACK_TAG = "LockedBackpack";
    public static final String USE_PLAYER_SETTINGS_TAG = "UsePlayerSettings";
    public static final String NO_SORT_COLOR_INDEX_TAG = "NoSortColorIndex";

    private boolean keepTab;
    private boolean shiftClickIntoOpenTab;
    private boolean keepSearchPhrase;
    private boolean lockBackpack;
    private boolean usePlayerSettings;
    private int noSortColorIndex;

    private final List<ItemStack> memorizedStacks;
    private final List<Boolean> respectNbt;
    private final List<Boolean> lockedSlots;

    public BackpackSettingsTemplate(int slotCount) {
        memorizedStacks = new ArrayList<>(slotCount);
        respectNbt = new ArrayList<>(slotCount);
        lockedSlots = new ArrayList<>(slotCount);
        for (int i = 0; i < slotCount; i++) {
            memorizedStacks.add(null);
            respectNbt.add(false);
            lockedSlots.add(false);
        }
    }

    public static BackpackSettingsTemplate fromWrapper(BackpackWrapper wrapper) {
        BackpackSettingsTemplate template = new BackpackSettingsTemplate(wrapper.getSlots());
        template.keepTab = wrapper.isKeepTab();
        template.shiftClickIntoOpenTab = wrapper.isShiftClickIntoOpenTab();
        template.keepSearchPhrase = wrapper.isKeepSearchPhrase();
        template.lockBackpack = wrapper.isLockStorage();
        template.usePlayerSettings = wrapper.isUsePlayerSettings();
        template.noSortColorIndex = wrapper.getNoSortColorIndex();

        for (int i = 0; i < wrapper.getSlots(); i++) {
            ItemStack memoryStack = wrapper.getMemoryStack(i);
            template.memorizedStacks.set(i, memoryStack == null ? null : memoryStack.copy());
            template.respectNbt.set(i, wrapper.isMemoryStackRespectNBT(i));
            template.lockedSlots.set(i, wrapper.isSlotLocked(i));
        }

        return template;
    }

    public void applyTo(BackpackWrapper wrapper) {
        wrapper.setKeepTab(keepTab);
        wrapper.setShiftClickIntoOpenTab(shiftClickIntoOpenTab);
        wrapper.setKeepSearchPhrase(keepSearchPhrase);
        wrapper.setLockStorage(lockBackpack);
        wrapper.setUsePlayerSettings(usePlayerSettings);
        wrapper.setNoSortColorIndex(noSortColorIndex);

        for (int i = 0; i < wrapper.getSlots(); i++) {
            ItemStack memoryStack = i < memorizedStacks.size() ? memorizedStacks.get(i) : null;
            wrapper.backpackHandler.setMemoryStack(i, memoryStack == null ? null : memoryStack.copy());
            wrapper.backpackHandler.setRespectNBT(i, i < respectNbt.size() && respectNbt.get(i));
            wrapper.backpackHandler.setSlotLocked(i, i < lockedSlots.size() && lockedSlots.get(i));
        }

        wrapper.markDirty();
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(KEEP_TAB_TAG, keepTab);
        tag.setBoolean(SHIFT_CLICK_INTO_OPEN_TAB_TAG, shiftClickIntoOpenTab);
        tag.setBoolean(KEEP_SEARCH_PHRASE_TAG, keepSearchPhrase);
        tag.setBoolean(LOCKED_BACKPACK_TAG, lockBackpack);
        tag.setBoolean(USE_PLAYER_SETTINGS_TAG, usePlayerSettings);
        tag.setInteger(NO_SORT_COLOR_INDEX_TAG, noSortColorIndex);

        NBTTagCompound memoryTag = new NBTTagCompound();
        BackpackItemStackHelpers.saveAllSlotsExtended(memoryTag, memorizedStacks);
        tag.setTag(BackpackWrapper.MEMORY_STACK_ITEMS_TAG, memoryTag);

        byte[] respectBytes = new byte[respectNbt.size()];
        for (int i = 0; i < respectNbt.size(); i++) {
            respectBytes[i] = (byte) (respectNbt.get(i) ? 1 : 0);
        }
        tag.setByteArray(BackpackWrapper.MEMORY_STACK_RESPECT_NBT_TAG, respectBytes);

        byte[] lockedBytes = new byte[lockedSlots.size()];
        for (int i = 0; i < lockedSlots.size(); i++) {
            lockedBytes[i] = (byte) (lockedSlots.get(i) ? 1 : 0);
        }
        tag.setByteArray(BackpackWrapper.LOCKED_SLOTS_TAG, lockedBytes);
        return tag;
    }

    public static BackpackSettingsTemplate fromNBT(NBTTagCompound tag, int slotCount) {
        BackpackSettingsTemplate template = new BackpackSettingsTemplate(slotCount);
        if (tag == null) {
            return template;
        }

        template.keepTab = tag.getBoolean(KEEP_TAB_TAG);
        template.shiftClickIntoOpenTab = tag.getBoolean(SHIFT_CLICK_INTO_OPEN_TAB_TAG);
        template.keepSearchPhrase = tag.getBoolean(KEEP_SEARCH_PHRASE_TAG);
        template.lockBackpack = tag.getBoolean(LOCKED_BACKPACK_TAG);
        template.usePlayerSettings = tag.getBoolean(USE_PLAYER_SETTINGS_TAG);
        template.noSortColorIndex = tag.getInteger(NO_SORT_COLOR_INDEX_TAG);

        if (tag.hasKey(BackpackWrapper.MEMORY_STACK_ITEMS_TAG, 10)) {
            BackpackItemStackHelpers.loadAllItemsExtended(
                tag.getCompoundTag(BackpackWrapper.MEMORY_STACK_ITEMS_TAG),
                template.memorizedStacks);
        }

        byte[] respectBytes = tag.getByteArray(BackpackWrapper.MEMORY_STACK_RESPECT_NBT_TAG);
        for (int i = 0; i < respectBytes.length && i < template.respectNbt.size(); i++) {
            template.respectNbt.set(i, respectBytes[i] != 0);
        }

        byte[] lockedBytes = tag.getByteArray(BackpackWrapper.LOCKED_SLOTS_TAG);
        for (int i = 0; i < lockedBytes.length && i < template.lockedSlots.size(); i++) {
            template.lockedSlots.set(i, lockedBytes[i] != 0);
        }

        return template;
    }

    public boolean isKeepTab() {
        return keepTab;
    }

    public void setKeepTab(boolean keepTab) {
        this.keepTab = keepTab;
    }

    public boolean isShiftClickIntoOpenTab() {
        return shiftClickIntoOpenTab;
    }

    public void setShiftClickIntoOpenTab(boolean shiftClickIntoOpenTab) {
        this.shiftClickIntoOpenTab = shiftClickIntoOpenTab;
    }

    public boolean isKeepSearchPhrase() {
        return keepSearchPhrase;
    }

    public void setKeepSearchPhrase(boolean keepSearchPhrase) {
        this.keepSearchPhrase = keepSearchPhrase;
    }

    public boolean isLockBackpack() {
        return lockBackpack;
    }

    public void setLockBackpack(boolean lockBackpack) {
        this.lockBackpack = lockBackpack;
    }

    public boolean isUsePlayerSettings() {
        return usePlayerSettings;
    }

    public void setUsePlayerSettings(boolean usePlayerSettings) {
        this.usePlayerSettings = usePlayerSettings;
    }

    public int getNoSortColorIndex() {
        return noSortColorIndex;
    }

    public void setNoSortColorIndex(int noSortColorIndex) {
        this.noSortColorIndex = noSortColorIndex;
    }

    public List<ItemStack> getMemorizedStacks() {
        return memorizedStacks;
    }

    public List<Boolean> getRespectNbt() {
        return respectNbt;
    }

    public List<Boolean> getLockedSlots() {
        return lockedSlots;
    }

    public void setMemorySlot(int slot, ItemStack stack, boolean respectNbt) {
        if (slot < 0 || slot >= memorizedStacks.size()) {
            return;
        }
        memorizedStacks.set(slot, stack == null ? null : stack.copy());
        this.respectNbt.set(slot, respectNbt);
    }

    public void setLockedSlot(int slot, boolean locked) {
        if (slot < 0 || slot >= lockedSlots.size()) {
            return;
        }
        lockedSlots.set(slot, locked);
    }
}
