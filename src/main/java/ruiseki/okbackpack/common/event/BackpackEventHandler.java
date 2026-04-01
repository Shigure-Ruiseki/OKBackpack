package ruiseki.okbackpack.common.event;

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
import ruiseki.okbackpack.compat.Mods;

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
    public void onPlayerPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.item.getEntityItem()
            .copy();

        if (Mods.Baubles.isLoaded()) {
            IInventory inventory = BaublesApi.getBaubles(player);
            stack = attemptPickup(player, inventory, stack, InventoryTypes.BAUBLES);
        }

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
            if (player.openContainer instanceof BackPackContainer container && type == container.wrapper.type
                && i == container.wrapper.slotIndex) {
                wrapper = container.wrapper;
            } else {
                wrapper = new BackpackWrapper(stack, backpack);
            }

            if (!wrapper.canPickupItem(pickupStack)) continue;

            ItemStack before = pickupStack.copy();

            ItemStack result = wrapper.insertItem(pickupStack, false);

            boolean changed = result == null || result.stackSize != before.stackSize;

            if (changed) {
                wrapper.writeToItem();
                if (player.openContainer instanceof BackPackContainer container && wrapper == container.wrapper) {
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
}
