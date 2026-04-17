package ruiseki.okbackpack.common.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.github.bsideup.jabel.Desugar;
import com.gtnewhorizon.gtnhlib.concurrent.ThreadsafeCache;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.api.entity.IBackpackCarrierEntity;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;

public final class BackpackEntityHelper {

    private static final int MAX_CACHE_SIZE = 256;

    private static final ThreadsafeCache<BackpackKey, BackpackWrapper> WRAPPER_CACHE = new ThreadsafeCache<>(
        MAX_CACHE_SIZE,
        key -> {
            BackpackKey backpackKey = (BackpackKey) key;
            ItemStack stack = backpackKey.getStack();
            if (!isBackpackStack(stack)) return null;
            return new BackpackWrapper(stack, (BlockBackpack.ItemBackpack) stack.getItem());
        },
        false);

    private BackpackEntityHelper() {}

    public static boolean visitBackpacks(Entity entity, BackpackVisitor visitor) {
        return visitBackpacks(entity, SearchOrder.PLAYER_THEN_BAUBLES, visitor);
    }

    public static boolean visitBackpacks(Entity entity, SearchOrder order, BackpackVisitor visitor) {
        if (entity == null || visitor == null) return false;

        if (entity instanceof EntityPlayer player) {
            return visitPlayerBackpacks(player, order, visitor);
        }

        if (entity instanceof IBackpackCarrierEntity carrier) {
            return visitCarrierBackpacks(entity, carrier.getCarriedBackpacks(), visitor);
        }

        return false;
    }

    public static boolean visitPlayerBackpacks(EntityPlayer player, SearchOrder order, BackpackVisitor visitor) {
        if (player == null || visitor == null) return false;

        if (order == SearchOrder.BAUBLES_THEN_PLAYER) {
            return visitInventory(player, BaublesApi.getBaubles(player), InventoryTypes.BAUBLES, visitor)
                || visitInventory(player, player.inventory, InventoryTypes.PLAYER, visitor);
        }

        return visitInventory(player, player.inventory, InventoryTypes.PLAYER, visitor)
            || visitInventory(player, BaublesApi.getBaubles(player), InventoryTypes.BAUBLES, visitor);
    }

    public static BackpackContext getBackpack(EntityPlayer player, InventoryType type, int slotIndex) {
        if (player == null || type == null || slotIndex < 0) return null;

        IInventory inventory = getInventory(player, type);
        if (inventory == null || slotIndex >= inventory.getSizeInventory()) return null;

        ItemStack stack = inventory.getStackInSlot(slotIndex);
        if (!isBackpackStack(stack)) return null;

        return new BackpackContext(player, stack, resolveWrapper(player, stack, type, slotIndex), type, slotIndex);
    }

    public static ItemStack findBackpackByUuid(Entity entity, String uuid, InventoryType preferredType) {
        if (entity == null || uuid == null || uuid.isEmpty()) return null;

        if (entity instanceof EntityPlayer player) {
            SearchOrder order = preferredType == InventoryTypes.BAUBLES ? SearchOrder.BAUBLES_THEN_PLAYER
                : SearchOrder.PLAYER_THEN_BAUBLES;
            final ItemStack[] result = new ItemStack[1];
            visitPlayerBackpacks(player, order, context -> {
                if (isSameBackpack(context.stack(), uuid)) {
                    result[0] = context.stack();
                    return true;
                }
                return false;
            });
            return result[0];
        }

        if (entity instanceof IBackpackCarrierEntity carrier) {
            for (ItemStack stack : carrier.getCarriedBackpacks()) {
                if (isSameBackpack(stack, uuid)) {
                    return stack;
                }
            }
        }

        return null;
    }

    public static BackpackWrapper getWrapper(ItemStack stack) {
        if (!isBackpackStack(stack)) return null;
        return WRAPPER_CACHE.get(new BackpackKey(stack));
    }

    public static BackpackWrapper getInteractionWrapper(EntityPlayer player, ItemStack stack) {
        if (!isBackpackStack(stack)) return null;

        BackpackWrapper openWrapper = getOpenBackpackWrapper(player, stack);
        if (openWrapper != null) {
            return openWrapper;
        }

        return getWrapper(stack);
    }

    public static boolean isBackpackStack(ItemStack stack) {
        return isBackpackStack(stack, true);
    }

    public static boolean isBackpackStack(ItemStack stack, boolean checkCount) {
        return stack != null && (!checkCount || stack.stackSize > 0)
            && stack.getItem() instanceof BlockBackpack.ItemBackpack;
    }

    public static boolean isSameBackpack(ItemStack stack, String uuid) {
        if (!isBackpackStack(stack) || uuid == null || uuid.isEmpty()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(BackpackWrapper.BACKPACK_NBT)) return false;

        NBTTagCompound backpackTag = tag.getCompoundTag(BackpackWrapper.BACKPACK_NBT);
        return uuid.equals(backpackTag.getString(BackpackWrapper.UUID_TAG));
    }

    public static void persistBackpack(BackpackContext context) {
        if (context == null) return;

        if (context.carrier() instanceof EntityPlayer player) {
            context.wrapper()
                .writeToItem(player);
            if (player.openContainer instanceof BackPackContainer container && context.matches(container)) {
                container.detectAndSendChanges();
            }
            return;
        }

        context.wrapper()
            .writeToItem();
    }

    private static boolean visitInventory(EntityPlayer player, IInventory inventory, InventoryType type,
        BackpackVisitor visitor) {
        if (inventory == null) return false;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!isBackpackStack(stack)) continue;

            BackpackContext context = new BackpackContext(
                player,
                stack,
                resolveWrapper(player, stack, type, i),
                type,
                i);
            if (visitor.visit(context)) {
                return true;
            }
        }

        return false;
    }

    private static boolean visitCarrierBackpacks(Entity carrier, Iterable<ItemStack> stacks, BackpackVisitor visitor) {
        if (stacks == null) return false;

        int slotIndex = 0;
        for (ItemStack stack : stacks) {
            if (!isBackpackStack(stack)) {
                slotIndex++;
                continue;
            }

            BackpackContext context = new BackpackContext(carrier, stack, getWrapper(stack), null, slotIndex);
            if (visitor.visit(context)) {
                return true;
            }

            slotIndex++;
        }

        return false;
    }

    private static BackpackWrapper resolveWrapper(EntityPlayer player, ItemStack stack, InventoryType type,
        int slotIndex) {
        if (player.openContainer instanceof BackPackContainer container && type == container.wrapper.getType()
            && slotIndex == container.wrapper.getSlotIndex()
            && container.wrapper instanceof BackpackWrapper wrapper) {
            return wrapper;
        }

        return getWrapper(stack);
    }

    public static BackpackWrapper getOpenBackpackWrapper(EntityPlayer player, ItemStack stack) {
        if (player == null || stack == null
            || !(player.openContainer instanceof BackPackContainer container)
            || !(container.wrapper instanceof BackpackWrapper wrapper)) {
            return null;
        }

        if (wrapper.getBackpack() == stack || isSameBackpack(stack, wrapper.uuid)) {
            return wrapper;
        }

        return null;
    }

    private static IInventory getInventory(EntityPlayer player, InventoryType type) {
        if (player == null || type == null) return null;
        if (type == InventoryTypes.PLAYER) return player.inventory;
        if (type == InventoryTypes.BAUBLES) return BaublesApi.getBaubles(player);
        return null;
    }

    @FunctionalInterface
    public interface BackpackVisitor {

        boolean visit(BackpackContext context);
    }

    public enum SearchOrder {
        PLAYER_THEN_BAUBLES,
        BAUBLES_THEN_PLAYER
    }

    @Desugar
    public record BackpackContext(Entity carrier, ItemStack stack, BackpackWrapper wrapper, InventoryType inventoryType,
        int slotIndex) {

        public Entity getCarrier() {
            return carrier;
        }

        public ItemStack getStack() {
            return stack;
        }

        public BackpackWrapper getWrapper() {
            return wrapper;
        }

        public InventoryType getInventoryType() {
            return inventoryType;
        }

        public int getSlotIndex() {
            return slotIndex;
        }

        public boolean matches(BackPackContainer container) {
            return container != null && wrapper == container.wrapper
                && inventoryType == container.wrapper.getType()
                && slotIndex == container.wrapper.getSlotIndex();
        }
    }

    @Desugar
    public record BackpackKey(ItemStack stack, int nbtHash) {

        public BackpackKey(ItemStack stack) {
            this(stack, computeNbtSignature(stack));
        }

        public ItemStack getStack() {
            return stack;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof BackpackKey other)) return false;
            return stack == other.stack && nbtHash == other.nbtHash;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(stack) * 31 + nbtHash;
        }
    }

    private static int computeNbtSignature(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return 0;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(BackpackWrapper.BACKPACK_NBT)) return 0;

        return deepHash(tag.getCompoundTag(BackpackWrapper.BACKPACK_NBT));
    }

    private static int deepHash(NBTTagCompound tag) {
        int hash = 1;
        List<String> keys = new ArrayList<>(tag.func_150296_c());
        Collections.sort(keys);

        for (String key : keys) {
            hash = 31 * hash + key.hashCode();

            hash = switch (tag.func_150299_b(key)) {
                case 3 -> 31 * hash + tag.getInteger(key);
                case 8 -> 31 * hash + tag.getString(key)
                    .hashCode();
                case 10 -> 31 * hash + deepHash(tag.getCompoundTag(key));
                case 9 -> 31 * hash + tag.getTagList(key, 10)
                    .tagCount();
                default -> 31 * hash + tag.getTag(key)
                    .toString()
                    .hashCode();
            };
        }

        return hash;
    }
}
