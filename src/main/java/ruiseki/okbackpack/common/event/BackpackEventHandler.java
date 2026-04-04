package ruiseki.okbackpack.common.event;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.github.bsideup.jabel.Desugar;

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

            BackpackWrapper wrapper = getWrapper(stack, item);
            if (!stack.getTagCompound()
                .hasKey(BackpackWrapper.BACKPACK_NBT)) {
                wrapper.writeToItem();
                wrapperCache.put(
                    stack,
                    new CachedWrapper(
                        wrapper,
                        stack.getTagCompound()
                            .hashCode()));
                continue;
            }

            if (!(player.openContainer instanceof BackPackContainer)) {
                if (wrapper.tick(player)) {
                    wrapper.writeToItem();
                    wrapperCache.put(
                        stack,
                        new CachedWrapper(
                            wrapper,
                            stack.getTagCompound()
                                .hashCode()));
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
            BackpackWrapper wrapper = getWrapper(stack, item);
            if (!(player.openContainer instanceof BackPackContainer)) {
                if (wrapper.tick(player)) {
                    wrapper.writeToItem();
                    wrapperCache.put(
                        stack,
                        new CachedWrapper(
                            wrapper,
                            stack.getTagCompound()
                                .hashCode()));
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
                wrapper = getWrapper(stack, backpack);
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

    private static final Map<ItemStack, CachedWrapper> wrapperCache = new WeakHashMap<>();

    @Desugar
    private record CachedWrapper(BackpackWrapper wrapper, int nbtHash) {}

    private static BackpackWrapper getWrapper(ItemStack stack, BlockBackpack.ItemBackpack item) {
        CachedWrapper cached = wrapperCache.get(stack);
        int currentHash = stack.hasTagCompound() ? stack.getTagCompound()
            .hashCode() : 0;

        if (cached != null) {
            if (cached.nbtHash == currentHash) {
                return cached.wrapper;
            }
        }

        // tạo wrapper mới
        BackpackWrapper wrapper = new BackpackWrapper(stack, item);
        wrapperCache.put(stack, new CachedWrapper(wrapper, currentHash));
        return wrapper;
    }
}
