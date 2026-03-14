package ruiseki.okbackpack.common.event;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import org.joml.Vector3d;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;

public class BackpackEventHandler {

    private static int feedTickCounter = 0;
    private static int magnetTickCounter = 0;

    public BackpackEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SubscribeEvent
    public void onPlayerPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        IInventory inventory = player.inventory;
        ItemStack stack = event.item.getEntityItem()
            .copy();

        if (player.openContainer instanceof BackPackContainer) {
            return;
        }

        if (Mods.Baubles.isLoaded()) {
            IInventory baublesInventory = BaublesApi.getBaubles(player);
            stack = attemptPickup(baublesInventory, stack, InventoryTypes.BAUBLES);
        }

        if (stack == null) {
            stack = attemptPickup(inventory, stack, InventoryTypes.PLAYER);
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

    private ItemStack attemptPickup(IInventory targetInventory, ItemStack stack, InventoryType type) {

        for (int i = 0; i < targetInventory.getSizeInventory(); i++) {
            ItemStack backpackStack = targetInventory.getStackInSlot(i);
            if (backpackStack == null || backpackStack.stackSize <= 0) {
                continue;
            }

            if (!(backpackStack.getItem() instanceof BlockBackpack.ItemBackpack backpack)) {
                continue;
            }

            BackpackWrapper handler = new BackpackWrapper(backpackStack, backpack);

            if (!handler.canPickupItem(stack)) {
                continue;
            }

            stack = handler.insertItem(stack, false);

            OKBackpack.instance.getPacketHandler()
                .sendToServer(new PacketBackpackNBT(i, handler.getTagCompound(), type));
            if (stack == null) {
                break;
            }
        }

        return stack;
    }

    @SubscribeEvent
    public void onPlayerTickFeed(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof EntityPlayerMP player)) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (player.openContainer instanceof BackPackContainer) {
            return;
        }

        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() instanceof BlockBackpack.ItemBackpack) {
            feedTickCounter = -100;
            return;
        }

        feedTickCounter++;
        if (feedTickCounter % 20 == 0) {
            feedTickCounter = 0;
            if (!player.capabilities.isCreativeMode) {
                attemptFeed(player);
            }
        }
    }

    public void attemptFeed(EntityPlayer player) {
        boolean result = false;

        if (Mods.Baubles.isLoaded()) {
            IInventory baublesInventory = BaublesApi.getBaubles(player);
            result = attemptFeed(player, baublesInventory, InventoryTypes.BAUBLES);
        }

        if (!result) {
            attemptFeed(player, player.inventory, InventoryTypes.PLAYER);
        }
    }

    public boolean attemptFeed(EntityPlayer player, IInventory searchInventory, InventoryType type) {
        int size = searchInventory.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack stack = searchInventory.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) {
                continue;
            }

            if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack backpack)) {
                continue;
            }

            BackpackWrapper handler = new BackpackWrapper(stack.copy(), backpack);

            ItemStack feedingStack = handler.getFeedingStack(
                player.getFoodStats()
                    .getFoodLevel(),
                player.getHealth(),
                player.getMaxHealth());

            if (feedingStack == null || feedingStack.stackSize <= 0) {
                continue;
            }

            feedingStack.onFoodEaten(player.worldObj, player);

            OKBackpack.instance.getPacketHandler()
                .sendToServer(new PacketBackpackNBT(i, handler.getTagCompound(), type));
            return true;
        }

        return false;
    }

    @SubscribeEvent
    public void onPlayerTickMagnet(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof EntityPlayerMP player)) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        magnetTickCounter++;
        if (magnetTickCounter % 2 == 0) {
            magnetTickCounter = 0;
            attemptMagnet(player);
        }
    }

    public void attemptMagnet(EntityPlayer player) {
        boolean result = false;

        if (Mods.Baubles.isLoaded()) {
            IInventory baublesInventory = BaublesApi.getBaubles(player);
            result = attemptMagnet(player, baublesInventory);
        }

        if (!result) {
            attemptMagnet(player, player.inventory);
        }
    }

    public boolean attemptMagnet(EntityPlayer player, IInventory searchInventory) {
        int size = searchInventory.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack stack = searchInventory.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) {
                continue;
            }

            if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack backpack)) {
                continue;
            }

            BackpackWrapper handler = new BackpackWrapper(stack.copy(), backpack);

            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
                player.posX - ModConfig.magnetRange,
                player.posY - ModConfig.magnetRange,
                player.posZ - ModConfig.magnetRange,
                player.posX + ModConfig.magnetRange,
                player.posY + ModConfig.magnetRange,
                player.posZ + ModConfig.magnetRange);

            List<Entity> entities = handler.getMagnetEntities(player.worldObj, aabb);
            if (entities.isEmpty()) {
                return false;
            }
            int pulled = 0;
            for (Entity entity : entities) {
                if (pulled++ > 200) {
                    break;
                }
                Vector3d target = new Vector3d(
                    player.posX,
                    player.posY - (player.worldObj.isRemote ? 1.62 : 0) + 0.75,
                    player.posZ);
                setEntityMotionFromVector(entity, target, 0.45F);
            }

            return true;
        }

        return false;
    }

    private void setEntityMotionFromVector(Entity entity, Vector3d target, float modifier) {
        Vector3d current = fromEntityCenter(entity);

        Vector3d motion = new Vector3d(target.x - current.x, target.y - current.y, target.z - current.z);

        if (motion.length() > 1.0) {
            motion.normalize();
        }

        entity.motionX = motion.x * modifier;
        entity.motionY = motion.y * modifier;
        entity.motionZ = motion.z * modifier;
    }

    public Vector3d fromEntityCenter(Entity e) {
        return new Vector3d(e.posX, e.posY - e.yOffset + e.height / 2.0, e.posZ);
    }
}
