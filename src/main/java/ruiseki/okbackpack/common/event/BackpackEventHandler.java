package ruiseki.okbackpack.common.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.gtnewhorizon.gtnhlib.concurrent.ThreadsafeCache;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.init.ModBlocks;

public class BackpackEventHandler {

    public BackpackEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SubscribeEvent
    public void registerBackpackProperty(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer && BackpackProperty.get((EntityPlayer) event.entity) == null) {
            BackpackProperty.register((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void playerWokeUp(PlayerWakeUpEvent event) {
        if (event.entity.worldObj.isRemote) return;

        EntityPlayer player = event.entityPlayer;
        ChunkCoordinates bedLocation = player.getBedLocation(player.dimension);
        if (bedLocation != null && player.worldObj.getBlock(bedLocation.posX, bedLocation.posY, bedLocation.posZ)
            == ModBlocks.SLEEPING_BAG.getBlock()) {
            if (BlockSleepingBag.isSleepingInPortableBag(player)) {
                BlockSleepingBag.packPortableSleepingBag(player);
                BackpackProperty.get(player)
                    .setWakingUpInPortableBag(true);
            } else {
                BackpackProperty props = BackpackProperty.get(player);
                if (props != null) {
                    BackpackProperty.get(player)
                        .setWakingUpInDeployedBag(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void tickPlayer(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player == null || player.isDead) return;
        if (event.phase == TickEvent.Phase.END) {
            if (!player.worldObj.isRemote) {
                if (BackpackProperty.get(player)
                    .isWakingUpInPortableBag()) {
                    BlockSleepingBag.restoreOriginalSpawn(player);
                    BackpackProperty.get(player)
                        .setWakingUpInPortableBag(false);
                }
                if (BackpackProperty.get(player)
                    .isWakingUpInDeployedBag()) {
                    BlockSleepingBag.restoreOriginalSpawn(player);
                    BackpackProperty.get(player)
                        .setWakingUpInDeployedBag(false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        if (player.worldObj.isRemote || player.isDead) return;

        tickInventory(player);
        tickBaubles(player);
    }

    private void tickInventory(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof BlockBackpack.ItemBackpack item)) continue;

            if (!stack.getTagCompound()
                .hasKey(BackpackWrapper.BACKPACK_NBT)) {
                BackpackWrapper wrapper = getWrapper(stack);
                wrapper.writeToItem();
                continue;
            }

            if (!(player.openContainer instanceof BackPackContainer)) {
                BackpackWrapper wrapper = getWrapper(stack);
                if (wrapper.tick(player)) {
                    wrapper.writeToItem();
                }
            }
        }
    }

    private void tickBaubles(EntityPlayer player) {
        IInventory baubles = BaublesApi.getBaubles(player);
        if (baubles == null) return;

        for (int i = 0; i < baubles.getSizeInventory(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof BlockBackpack.ItemBackpack item)) continue;
            if (!(player.openContainer instanceof BackPackContainer)) {
                BackpackWrapper wrapper = getWrapper(stack);
                if (wrapper.tick(player)) {
                    wrapper.writeToItem();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.item.getEntityItem()
            .copy();

        IInventory baubles = BaublesApi.getBaubles(player);
        stack = attemptPickup(player, baubles, stack, InventoryTypes.BAUBLES);

        if (stack != null) {
            IInventory inventory = player.inventory;
            stack = attemptPickup(player, inventory, stack, InventoryTypes.PLAYER);
        }

        if (stack == null || stack.stackSize <= 0) {
            event.item.setDead();
            event.setCanceled(true);

            World world = event.item.worldObj;

            world.playSoundEffect(
                event.item.posX,
                event.item.posY,
                event.item.posZ,
                "random.pop",
                0.2F,
                ((player.getRNG()
                    .nextFloat()
                    - player.getRNG()
                        .nextFloat())
                    * 0.7F + 1.0F) * 2.0F);
            return;
        } else if (stack.stackSize != event.item.getEntityItem().stackSize) {
            event.item.setDead();
            event.setCanceled(true);

            World world = event.item.worldObj;

            EntityItem newItem = new EntityItem(world, event.item.posX, event.item.posY, event.item.posZ, stack);

            newItem.delayBeforeCanPickup = 0;
            world.spawnEntityInWorld(newItem);
        }

    }

    private static ItemStack attemptPickup(EntityPlayer player, IInventory targetInventory, ItemStack pickupStack,
        InventoryType type) {

        for (int i = 0; i < targetInventory.getSizeInventory(); i++) {
            ItemStack stack = targetInventory.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack backpack)) continue;

            BackpackWrapper wrapper;
            if (player.openContainer instanceof BackPackContainer container && type == container.wrapper.getType()
                && i == container.wrapper.getSlotIndex()) {
                wrapper = (BackpackWrapper) container.wrapper;
            } else {
                wrapper = getWrapper(stack);
            }

            if (!wrapper.canPickupItem(pickupStack)) continue;

            ItemStack before = pickupStack.copy();

            ItemStack result = wrapper.insertItem(pickupStack, false);

            boolean changed = result == null || result.stackSize != before.stackSize;

            if (changed) {
                wrapper.writeToItem();
                if (player.openContainer instanceof BackPackContainer container && wrapper == container.wrapper
                    && type == container.wrapper.getType()
                    && i == container.wrapper.getSlotIndex()) {
                    container.detectAndSendChanges();
                }
            }

            pickupStack = result;

            if (pickupStack == null || pickupStack.stackSize <= 0) {
                return null;
            }
        }

        return pickupStack;
    }

    private static final int MAX_CACHE_SIZE = 256;

    private static final ThreadsafeCache<BackpackKey, BackpackWrapper> WRAPPER_CACHE = new ThreadsafeCache<>(
        MAX_CACHE_SIZE,
        key -> {
            BackpackKey k = (BackpackKey) key;
            ItemStack stack = k.getStack();
            if (stack == null) return null;
            if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack item)) return null;

            return new BackpackWrapper(stack, item);
        },
        false);

    private static BackpackWrapper getWrapper(ItemStack stack) {
        if (stack == null) return null;
        return WRAPPER_CACHE.get(new BackpackKey(stack));
    }

    private static final class BackpackKey {

        private final ItemStack stack;
        private final int nbtHash;

        public BackpackKey(ItemStack stack) {
            this.stack = stack;
            this.nbtHash = computeNBTSignature(stack);
        }

        public ItemStack getStack() {
            return stack;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BackpackKey other)) return false;
            return stack == other.stack && nbtHash == other.nbtHash;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(stack) * 31 + nbtHash;
        }
    }

    private static int computeNBTSignature(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return 0;

        NBTTagCompound tag = stack.getTagCompound();

        if (!tag.hasKey(BackpackWrapper.BACKPACK_NBT)) return 0;

        NBTTagCompound bp = tag.getCompoundTag(BackpackWrapper.BACKPACK_NBT);

        return deepHash(bp);
    }

    private static int deepHash(NBTTagCompound tag) {
        int hash = 1;

        List<String> keys = new ArrayList<>(tag.func_150296_c());
        Collections.sort(keys);

        for (String key : keys) {
            hash = 31 * hash + key.hashCode();

            switch (tag.func_150299_b(key)) {
                case 3:
                    hash = 31 * hash + tag.getInteger(key);
                    break;
                case 8:
                    hash = 31 * hash + tag.getString(key)
                        .hashCode();
                    break;
                case 10:
                    hash = 31 * hash + deepHash(tag.getCompoundTag(key));
                    break;
                case 9:
                    hash = 31 * hash + tag.getTagList(key, 10)
                        .tagCount();
                    break;
                default:
                    hash = 31 * hash + tag.getTag(key)
                        .toString()
                        .hashCode();
            }
        }

        return hash;
    }
}
