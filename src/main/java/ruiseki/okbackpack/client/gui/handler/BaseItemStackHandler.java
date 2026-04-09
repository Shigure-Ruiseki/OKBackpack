package ruiseki.okbackpack.client.gui.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.ItemStackHandler;

public class BaseItemStackHandler extends ItemStackHandler {

    public BaseItemStackHandler(int size) {
        super(size);
    }

    public void resize(int newSize) {
        List<ItemStack> newStacks = new ArrayList<>(newSize);

        for (int i = 0; i < newSize; i++) {
            if (i < stacks.size()) {
                newStacks.add(stacks.get(i));
            } else {
                newStacks.add(null);
            }
        }

        this.stacks = newStacks;
    }

    public boolean isSizeInconsistent(int newSize) {
        return newSize != stacks.size();
    }

    public static <T> void syncListSize(List<T> list, int newSize, T defaultValue) {
        int currentSize = list.size();
        if (newSize < currentSize) {
            list.subList(newSize, currentSize)
                .clear();
        } else {
            for (int i = currentSize; i < newSize; i++) {
                list.add(defaultValue);
            }
        }
    }

    /**
     * Returns the indices of non-null slots in the given range [from, to).
     */
    public int[] getFilledSlotsInRange(int from, int to) {
        List<Integer> indices = new ArrayList<>();
        for (int i = from; i < to && i < stacks.size(); i++) {
            if (stacks.get(i) != null) indices.add(i);
        }
        return indices.stream()
            .mapToInt(Integer::intValue)
            .toArray();
    }
}
