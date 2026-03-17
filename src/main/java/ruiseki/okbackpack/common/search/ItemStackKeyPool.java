package ruiseki.okbackpack.common.search;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class ItemStackKeyPool {

    private static final int MAX_POOL = 32768;

    private static final Object2ObjectOpenHashMap<ItemStackKey, ItemStackKey> POOL = new Object2ObjectOpenHashMap<>();

    public static ItemStackKey get(ItemStack stack) {
        if (POOL.size() > MAX_POOL) {
            POOL.clear();
        }

        ItemStackKey tmp = ItemStackKey.of(stack);
        ItemStackKey existing = POOL.get(tmp);

        if (existing != null) {
            return existing;
        }

        POOL.put(tmp, tmp);
        return tmp;
    }

    public static void clear() {
        POOL.clear();
    }
}
