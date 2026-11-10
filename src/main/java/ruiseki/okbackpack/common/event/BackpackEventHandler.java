package ruiseki.okbackpack.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.github.bsideup.jabel.Desugar;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okbackpack.common.block.BlockSleepingBagConfig;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.common.helpers.BaublesHelpers;

public class BackpackEventHandler {

    public static final BackpackEventHandler INSTANCE = new BackpackEventHandler();

    private static final ItemStack SINGLE_ARROW = new ItemStack(Items.arrow);

    public BackpackEventHandler() {}

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) {
            for (BackpackTier tier : TierRegistry.getAllTiers()) {
                var clipLocation = tier.getClipTexturePath();
                event.map.registerIcon(clipLocation.toString());
            }
        }
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
            == BlockSleepingBagConfig._instance.getInstance()) {
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

        tickBackpacks(player);
    }

    private void tickBackpacks(EntityPlayer player) {
        boolean backpackOpen = player.openContainer instanceof BackPackContainer;
        BackpackEntityHelpers
            .visitPlayerBackpacks(player, BackpackEntityHelpers.SearchOrder.PLAYER_THEN_BAUBLES, context -> {
                ItemStack stack = context.getStack();
                if (stack.getTagCompound() == null || !stack.getTagCompound()
                    .hasKey(BackpackWrapper.BACKPACK_NBT)) {
                    BackpackEntityHelpers.persistBackpack(context);
                    return false;
                }

                if (!backpackOpen) {
                    boolean isWorn = BackpackEntityHelpers.isWornContext(player, context);
                    boolean dirty = isWorn ? context.getWrapper()
                        .tick(player)
                        : context.getWrapper()
                            .tickNonTravelers(player);
                    if (dirty) {
                        BackpackEntityHelpers.persistBackpack(context);
                    }
                }
                return false;
            });
    }

    @SubscribeEvent
    public void onPlayerPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null || player.worldObj.isRemote) return;
        if (event.item == null || event.item.isDead) {
            event.setCanceled(true);
            return;
        }

        ItemStack entityStack = event.item.getEntityItem();
        if (entityStack == null || entityStack.stackSize <= 0) {
            event.item.setDead();
            event.setCanceled(true);
            return;
        }

        ItemStack stack = entityStack.copy();
        int initialSize = stack.stackSize;

        IInventory baubles = BaublesHelpers.getBaubles(player);
        stack = attemptPickup(player, baubles, stack, InventoryTypes.BAUBLES);

        if (stack != null) {
            IInventory inventory = player.inventory;
            stack = attemptPickup(player, inventory, stack, InventoryTypes.PLAYER);
        }

        if (event.item.isDead) {
            event.setCanceled(true);
            return;
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
        } else if (stack.stackSize != initialSize) {
            event.item.setEntityItemStack(stack.copy());
            event.setCanceled(true);
        }
    }

    public static ItemStack attemptPickup(EntityPlayer player, IInventory targetInventory, ItemStack pickupStack,
        InventoryType type) {
        if (targetInventory == null) return pickupStack;

        for (int i = 0; i < targetInventory.getSizeInventory(); i++) {
            BackpackEntityHelpers.BackpackContext context = BackpackEntityHelpers.getBackpack(player, type, i);
            if (context == null) continue;
            if (!context.getWrapper()
                .canPickupItem(pickupStack)) continue;

            ItemStack before = pickupStack.copy();
            ItemStack result = context.getWrapper()
                .insertItem(pickupStack.copy(), false);
            boolean changed = result == null || result.stackSize != before.stackSize;

            if (changed) {
                BackpackEntityHelpers.persistBackpack(context);
            }

            pickupStack = result;
            if (pickupStack == null || pickupStack.stackSize <= 0) {
                return null;
            }
        }

        return pickupStack;
    }

    public interface UpgradeContextVisitor<T> {

        boolean visit(PlayerUpgradeContext<T> context);
    }

    @Desugar
    public record PlayerUpgradeContext<T> (BackpackEntityHelpers.BackpackContext backpack, int upgradeSlot,
        T upgrade) {}
}
