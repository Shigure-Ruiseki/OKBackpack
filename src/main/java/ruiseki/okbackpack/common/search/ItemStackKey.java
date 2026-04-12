package ruiseki.okbackpack.common.search;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackKey {

    public final ItemStack stack;

    private static final Map<ItemStack, ItemStackKey> CACHE = new ConcurrentHashMap<>();

    public static ItemStackKey of(ItemStack stack) {
        return CACHE.computeIfAbsent(stack, ItemStackKey::new);
    }

    private boolean hashInitialized = false;
    private int hash;

    private ItemStackKey(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.stackSize = 1;
        this.stack = copy;
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemStackKey that = (ItemStackKey) o;
        return ItemStack.areItemStacksEqual(this.stack, that.stack);
    }

    public boolean hashCodeNotEquals(ItemStack otherStack) {
        return hashCode() != computeHash(otherStack);
    }

    @Override
    public int hashCode() {
        if (!hashInitialized) {
            hashInitialized = true;
            hash = computeHash(stack);
        }
        return hash;
    }

    public boolean matches(ItemStack stack) {
        return hashCode() == computeHash(stack);
    }

    @Override
    public String toString() {
        return "ItemStackKey[stack=" + stack + ']';
    }

    private static int computeHash(ItemStack stack) {
        int hash = 1;

        hash = 31 * hash + Item.getIdFromItem(stack.getItem());
        hash = 31 * hash + stack.getItemDamage();

        NBTTagCompound tag = stack.getTagCompound();
        hash = 31 * hash + (tag == null ? 0 : tag.hashCode());

        return hash;
    }
}
