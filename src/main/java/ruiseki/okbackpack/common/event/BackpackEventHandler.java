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

    public BackpackEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SubscribeEvent
    public void onPlayerPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player.openContainer instanceof BackPackContainer) return;
        ItemStack stack = event.item.getEntityItem()
            .copy();

        if (Mods.Baubles.isLoaded()) {
            IInventory inventory = BaublesApi.getBaubles(player);
            stack = attemptPickup(inventory, stack, InventoryTypes.BAUBLES);
        }

        if (stack != null) {
            IInventory inventory = player.inventory;
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
            if (backpackStack == null || backpackStack.stackSize <= 0) continue;

            if (!(backpackStack.getItem() instanceof BlockBackpack.ItemBackpack backpack)) continue;

            BackpackWrapper wrapper = new BackpackWrapper(backpackStack, backpack);

            if (!wrapper.canPickupItem(stack)) continue;

            ItemStack before = stack.copy();

            ItemStack result = wrapper.insertItem(stack, false);

            boolean changed = result == null || result.stackSize != before.stackSize;

            if (changed) {
                OKBackpack.instance.getPacketHandler()
                    .sendToServer(new PacketBackpackNBT(i, wrapper.getTagCompound(), type));
            }

            stack = result;

            if (stack == null || stack.stackSize <= 0) {
                return null;
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
            return;
        }

        if (!player.capabilities.isCreativeMode && player.ticksExisted % 20 == 0) {
            attemptFeed(player);
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

            BackpackWrapper wrapper = new BackpackWrapper(stack, backpack);

            boolean result = wrapper.feed(player, wrapper);

            if (result) {
                OKBackpack.instance.getPacketHandler()
                    .sendToServer(new PacketBackpackNBT(i, stack.getTagCompound(), type));
            }

            return result;
        }

        return false;
    }

    @SubscribeEvent
    public void onPlayerTickMagnet(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof EntityPlayerMP player)) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (player.ticksExisted % 2 == 0) attemptMagnet(player);
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

            BackpackWrapper wrapper = new BackpackWrapper(stack, backpack);

            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
                player.posX - ModConfig.magnetRange,
                player.posY - ModConfig.magnetRange,
                player.posZ - ModConfig.magnetRange,
                player.posX + ModConfig.magnetRange,
                player.posY + ModConfig.magnetRange,
                player.posZ + ModConfig.magnetRange);

            List<Entity> entities = wrapper.getMagnetEntities(player.worldObj, aabb);
            if (entities.isEmpty()) {
                continue;
            }
            int pulled = 0;
            for (Entity entity : entities) {
                if (pulled++ > 20) {
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
