package ruiseki.storagecore.settings.nosort;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.storagecore.settings.ISettingsCategory;
import ruiseki.storagecore.settings.ISlotColorCategory;

public class NoSortSettingsCategory implements ISettingsCategory<NoSortSettingsCategory>, ISlotColorCategory {

    public static final String NAME = "no_sort";
    private static final String COLOR_TAG = "color";
    public static final int DEFAULT_COLOR = 0xFF7ED957;
    private static final String SELECTED_SLOTS_TAG = "selectedSlots";
    private NBTTagCompound categoryNbt;
    private final Consumer<NBTTagCompound> saveNbt;
    private final Set<Integer> selectedSlots = new HashSet<>();
    private int color = DEFAULT_COLOR;

    public NoSortSettingsCategory(NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
        this.categoryNbt = categoryNbt;
        this.saveNbt = saveNbt;
        deserialize();
    }

    private void deserialize() {
        for (int slotNumber : categoryNbt.getIntArray(SELECTED_SLOTS_TAG)) {
            selectedSlots.add(slotNumber);
        }
        NBTHelpers.getInteger(categoryNbt, COLOR_TAG)
            .ifPresent(c -> color = c);
    }

    public boolean isSlotSelected(int slotNumber) {
        return selectedSlots.contains(slotNumber);
    }

    public void unselectAllSlots() {
        selectedSlots.clear();
        serializeSelectedSlots();
    }

    /**
     * Selects slots that shouldn't be sorted
     *
     * @param minSlot inclusive
     * @param maxSlot exclusive
     */

    public void selectSlots(int minSlot, int maxSlot) {
        for (int slot = minSlot; slot < maxSlot; slot++) {
            selectedSlots.add(slot);
        }
        serializeSelectedSlots();
    }

    public void selectSlot(int slotNumber) {
        selectSlots(slotNumber, slotNumber + 1);
    }

    public void unselectSlot(int slotNumber) {
        selectedSlots.remove(slotNumber);
        serializeSelectedSlots();
    }

    private void serializeSelectedSlots() {
        int[] slots = new int[selectedSlots.size()];
        int i = 0;
        for (int slotNumber : selectedSlots) {
            slots[i++] = slotNumber;
        }
        categoryNbt.setIntArray(SELECTED_SLOTS_TAG, slots);
        saveNbt.accept(categoryNbt);
    }

    public void setColor(int argb) {
        this.color = argb;
        categoryNbt.setInteger(COLOR_TAG, color);
        saveNbt.accept(categoryNbt);
    }

    public int getColor() {
        return color;
    }

    @Override
    public Optional<Integer> getSlotColor(int slotNumber) {
        return selectedSlots.contains(slotNumber) ? Optional.of(color) : Optional.empty();
    }

    public Set<Integer> getNoSortSlots() {
        return selectedSlots;
    }

    @Override
    public void reloadFrom(NBTTagCompound categoryNbt) {
        this.categoryNbt = categoryNbt;
        selectedSlots.clear();
        color = DEFAULT_COLOR;
        deserialize();
    }

    @Override
    public void overwriteWith(NoSortSettingsCategory otherCategory) {
        selectedSlots.clear();
        selectedSlots.addAll(otherCategory.getNoSortSlots());
        serializeSelectedSlots();
        setColor(otherCategory.getColor());
    }

    @Override
    public boolean isLargerThanNumberOfSlots(int slots) {
        return selectedSlots.stream()
            .anyMatch(slotIndex -> slotIndex >= slots);
    }
}
