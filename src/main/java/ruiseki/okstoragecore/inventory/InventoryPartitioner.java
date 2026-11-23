package ruiseki.okstoragecore.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizon.gtnhlib.util.data.ItemMeta;

import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;

public class InventoryPartitioner {

    public static final String BASE_INDEXES_TAG = "baseIndexes";
    private IInventoryPartHandler[] inventoryPartHandlers;

    private int[] baseIndexes;
    private InventoryHandler parent;

    public InventoryPartitioner(NBTTagCompound tag, InventoryHandler parent,
        Supplier<MemorySettingsCategory> getMemorySettings) {
        this.parent = parent;
        this.deserializeNBT(tag, getMemorySettings);
    }

    private int getIndexForSlot(int slot) {
        if (slot < 0) {
            return -1;
        }

        if (this.baseIndexes.length == 1) {
            return 0;
        }

        int i = 0;
        for (; i < this.baseIndexes.length; i++) {
            if (slot - this.baseIndexes[i] < 0) {
                return i - 1;
            }
        }
        return i - 1;
    }

    public IInventoryPartHandler getPartBySlot(int slot) {
        if (slot < 0 || slot >= this.parent.getSlots()) {
            return IInventoryPartHandler.EMPTY;
        }
        int index = getIndexForSlot(slot);
        if (index < 0 || index >= this.inventoryPartHandlers.length) {
            return IInventoryPartHandler.EMPTY;
        }
        return this.inventoryPartHandlers[index];
    }

    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon(int slot) {
        return this.getPartBySlot(slot)
            .getNoItemIcon(slot);
    }

    public void onSlotLimitChange() {
        for (IInventoryPartHandler inventoryPartHandler : this.inventoryPartHandlers) {
            inventoryPartHandler.onSlotLimitChange();
        }
    }

    public Set<Integer> getNoSortSlots() {
        Set<Integer> noSortSlots = new HashSet<>();
        for (IInventoryPartHandler inventoryPartHandler : this.inventoryPartHandlers) {
            noSortSlots.addAll(inventoryPartHandler.getNoSortSlots());
        }
        return noSortSlots;
    }

    public boolean isFilterItem(ItemMeta item) {
        for (IInventoryPartHandler inventoryPartHandler : this.inventoryPartHandlers) {
            if (inventoryPartHandler.isFilterItem(item)) {
                return true;
            }
        }

        return false;
    }

    public Map<ItemMeta, Set<Integer>> getFilterItems() {
        Map<ItemMeta, Set<Integer>> filterItems = new HashMap<>();
        for (IInventoryPartHandler inventoryPartHandler : this.inventoryPartHandlers) {
            for (Map.Entry<ItemMeta, Set<Integer>> entry : inventoryPartHandler.getFilterItems()
                .entrySet()) {
                filterItems.computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                    .addAll(entry.getValue());
            }
        }
        return filterItems;
    }

    public void onInit() {
        for (IInventoryPartHandler inventoryPartHandler : inventoryPartHandlers) {
            inventoryPartHandler.onInit();
        }
    }

    public record SlotRange(int firstSlot, int numberOfSlots) {

        public boolean isInRange(int slotIndex) {
            return slotIndex >= firstSlot && slotIndex < firstSlot + numberOfSlots;
        }
    }

    public Optional<SlotRange> getFirstSpace(int maxNumberOfSlots) {
        for (int partIndex = 0; partIndex < this.inventoryPartHandlers.length; partIndex++) {
            if (this.inventoryPartHandlers[partIndex].canBeReplaced()) {
                int firstSlot = this.baseIndexes[partIndex];
                int numberOfSlots = this.baseIndexes.length > partIndex + 1
                    ? this.baseIndexes[partIndex + 1] - firstSlot
                    : parent.getSlots() - firstSlot;
                numberOfSlots = Math.min(numberOfSlots, maxNumberOfSlots);
                return numberOfSlots > 0 ? Optional.of(new SlotRange(this.baseIndexes[partIndex], numberOfSlots))
                    : Optional.empty();
            }
        }
        return Optional.empty();
    }

    public void addInventoryPart(int inventorySlot, int numberOfSlots, IInventoryPartHandler inventoryPartHandler) {
        int index = getIndexForSlot(inventorySlot);
        if (index < 0 || index >= this.inventoryPartHandlers.length || this.baseIndexes[index] != inventorySlot) {
            return;
        }

        List<IInventoryPartHandler> newParts = new ArrayList<>();
        List<Integer> newBaseIndexes = new ArrayList<>();

        for (int i = 0; i < index; i++) {
            newParts.add(this.inventoryPartHandlers[i]);
            newBaseIndexes.add(this.baseIndexes[i]);
        }

        newParts.add(inventoryPartHandler);
        newBaseIndexes.add(inventorySlot);

        int newNextSlot = inventorySlot + numberOfSlots;
        int remainingSlots = this.inventoryPartHandlers[index].getSlots() - numberOfSlots;
        if (remainingSlots > 0) {
            newParts.add(new IInventoryPartHandler.Default(this.parent, remainingSlots));
            newBaseIndexes.add(newNextSlot);
        }

        for (int i = index + 1; i < this.inventoryPartHandlers.length; i++) {
            newParts.add(this.inventoryPartHandlers[i]);
            newBaseIndexes.add(this.baseIndexes[i]);
        }

        this.updatePartsAndIndexesFromLists(newParts, newBaseIndexes);

        inventoryPartHandler.onInit();
        this.parent.onFilterItemsChanged();
    }

    public void removeInventoryPart(int inventorySlot) {
        int index = getIndexForSlot(inventorySlot);

        if (index < 0 || index >= this.inventoryPartHandlers.length || this.baseIndexes[index] != inventorySlot) {
            return;
        }

        if (this.inventoryPartHandlers.length == 1) {
            updatePartsAndIndexesFromLists(
                List.of(new IInventoryPartHandler.Default(this.parent, this.parent.getSlots())),
                List.of(0));
            this.parent.onFilterItemsChanged();
            return;
        }

        int slotsAtPartIndex = (this.baseIndexes.length > index + 1 ? this.baseIndexes[index + 1]
            : this.parent.getSlots()) - this.baseIndexes[index];

        List<IInventoryPartHandler> newParts = new ArrayList<>();
        List<Integer> newBaseIndexes = new ArrayList<>();

        boolean replacedNext = false;
        for (int i = 0; i < index; i++) {
            if (i == index - 1 && this.inventoryPartHandlers[i] instanceof IInventoryPartHandler.Default
                && this.baseIndexes.length > index + 1
                && this.inventoryPartHandlers[index + 1] instanceof IInventoryPartHandler.Default) {
                newParts.add(
                    new IInventoryPartHandler.Default(
                        this.parent,
                        this.inventoryPartHandlers[i].getSlots() + this.inventoryPartHandlers[index + 1].getSlots()
                            + slotsAtPartIndex));
                newBaseIndexes.add(this.baseIndexes[i]);
                replacedNext = true;
                continue;
            }

            newParts.add(this.inventoryPartHandlers[i]);
            newBaseIndexes.add(this.baseIndexes[i]);
        }

        if (!replacedNext && this.baseIndexes.length > index + 1) {
            if (this.inventoryPartHandlers[index + 1] instanceof IInventoryPartHandler.Default) {
                newParts.add(
                    new IInventoryPartHandler.Default(
                        this.parent,
                        this.inventoryPartHandlers[index + 1].getSlots() + slotsAtPartIndex));
                newBaseIndexes.add(inventorySlot);
            } else {
                newParts.add(new IInventoryPartHandler.Default(this.parent, slotsAtPartIndex));
                newBaseIndexes.add(inventorySlot);
                newParts.add(this.inventoryPartHandlers[index + 1]);
                newBaseIndexes.add(this.baseIndexes[index + 1]);
            }
        }

        for (int i = index + 2; i < this.inventoryPartHandlers.length; i++) {
            newParts.add(this.inventoryPartHandlers[i]);
            newBaseIndexes.add(this.baseIndexes[i]);
        }

        this.updatePartsAndIndexesFromLists(newParts, newBaseIndexes);

        this.parent.onFilterItemsChanged();
    }

    private void updatePartsAndIndexesFromLists(List<IInventoryPartHandler> newParts, List<Integer> newBaseIndexes) {
        this.inventoryPartHandlers = newParts.toArray(new IInventoryPartHandler[0]);
        this.baseIndexes = new int[newBaseIndexes.size()];
        for (int i = 0; i < newBaseIndexes.size(); i++) {
            this.baseIndexes[i] = newBaseIndexes.get(i);
        }
        this.parent.saveInventory();
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        ret.setIntArray(BASE_INDEXES_TAG, this.baseIndexes);
        NBTTagList partNames = new NBTTagList();
        for (IInventoryPartHandler inventoryPartHandler : this.inventoryPartHandlers) {
            partNames.appendTag(new NBTTagString(inventoryPartHandler.getName()));
        }
        ret.setTag("inventoryPartNames", partNames);
        return ret;
    }

    private void deserializeNBT(NBTTagCompound tag, Supplier<MemorySettingsCategory> getMemorySettings) {
        if (!tag.hasKey(BASE_INDEXES_TAG)) {
            this.inventoryPartHandlers = new IInventoryPartHandler[] {
                new IInventoryPartHandler.Default(this.parent, this.parent.getSlots()) };
            this.baseIndexes = new int[] { 0 };
            return;
        }

        this.baseIndexes = tag.getIntArray(BASE_INDEXES_TAG);
        this.inventoryPartHandlers = new IInventoryPartHandler[this.baseIndexes.length];
        NBTTagList partNamesTag = tag.getTagList("inventoryPartNames", Constants.NBT.TAG_STRING);
        for (int i = 0; i < partNamesTag.tagCount(); i++) {
            SlotRange slotRange = new SlotRange(
                this.baseIndexes[i],
                (i + 1 < this.baseIndexes.length ? this.baseIndexes[i + 1] : this.parent.getSlots())
                    - this.baseIndexes[i]);

            this.inventoryPartHandlers[i] = InventoryPartRegistry
                .instantiatePart(partNamesTag.getStringTagAt(i), this.parent, slotRange, getMemorySettings);
        }
    }

    public boolean isInfinite(int slot) {
        return this.getPartBySlot(slot)
            .isInfinite(slot);
    }
}
