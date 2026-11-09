package ruiseki.okbackpack.common.event;

import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
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
import ruiseki.okbackpack.common.item.travelers.quiver.QuiverUpgradeWrapper;
import ruiseki.okbackpack.common.item.travelers.slime.SlimeUpgradeWrapper;

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
    public void onPlayerFall(LivingFallEvent event) {
        if (!(event.entity instanceof EntityPlayer player)) return;
        if (player.worldObj.isRemote) return;

        if (hasTravelersUpgrade(player, SlimeUpgradeWrapper.class)) {
            player.fallDistance = 0F;
            event.setCanceled(true);
            if (player.isSneaking()) return;

            double bounceVelocity = SlimeUpgradeWrapper.calculateBounceVelocity(event.distance);
            if (bounceVelocity <= 0D) return;

            double deltaY = bounceVelocity - player.motionY;
            player.addVelocity(0D, deltaY, 0D);
            player.onGround = false;
            player.isAirBorne = true;
            player.velocityChanged = true;
            return;
        }
    }

    @SubscribeEvent
    public void onArrowNock(ArrowNockEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null) return;
        if (player.capabilities.isCreativeMode) return;
        if (player.inventory.hasItem(Items.arrow)) return;
        if (!hasTravelersUpgrade(player, QuiverUpgradeWrapper.class)) return;
        if (findQuiverSource(player) == null) return;

        player.setItemInUse(event.result, event.result.getMaxItemUseDuration());
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onArrowLoose(ArrowLooseEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null) return;
        if (!hasTravelersUpgrade(player, QuiverUpgradeWrapper.class)) return;
        if (player.capabilities.isCreativeMode) return;
        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, event.bow) > 0) return;

        BackpackEntityHelpers.BackpackContext quiverSource = findQuiverSource(player);
        if (quiverSource == null) return;

        event.setCanceled(true);
        if (player.worldObj.isRemote) return;

        shootArrowFromQuiver(player, event.bow, event.charge, quiverSource);
    }

    public BackpackEntityHelpers.BackpackContext findQuiverSource(EntityPlayer player) {
        final BackpackEntityHelpers.BackpackContext[] result = new BackpackEntityHelpers.BackpackContext[1];
        BackpackEntityHelpers.visitWornBackpacks(player, context -> {
            if (context.getWrapper()
                .gatherCapabilityUpgrades(QuiverUpgradeWrapper.class)
                .isEmpty()) {
                return false;
            }

            ItemStack extracted = context.getWrapper()
                .extractItem(SINGLE_ARROW, 1, true);
            if (extracted == null || extracted.stackSize <= 0) return false;

            result[0] = context;
            return true;
        });
        return result[0];
    }

    public void shootArrowFromQuiver(EntityPlayer player, ItemStack bow, int charge,
        BackpackEntityHelpers.BackpackContext source) {
        float velocity = getArrowVelocity(charge);
        if ((double) velocity < 0.1D) return;

        EntityArrow arrow = new EntityArrow(player.worldObj, player, velocity * 2.0F);
        if (velocity == 1.0F) {
            arrow.setIsCritical(true);
        }

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        if (power > 0) {
            arrow.setDamage(arrow.getDamage() + power * 0.5D + 0.5D);
        }

        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        if (punch > 0) {
            arrow.setKnockbackStrength(punch);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
            arrow.setFire(100);
        }

        bow.damageItem(1, player);
        source.getWrapper()
            .extractItem(SINGLE_ARROW, 1, false);
        BackpackEntityHelpers.persistBackpack(source);

        player.worldObj.playSoundAtEntity(
            player,
            "random.bow",
            1.0F,
            1.0F / (player.getRNG()
                .nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
        player.worldObj.spawnEntityInWorld(arrow);
    }

    public float getArrowVelocity(int charge) {
        float velocity = charge / 20.0F;
        velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
        if (velocity > 1.0F) {
            velocity = 1.0F;
        }
        return velocity;
    }

    public boolean hasTravelersUpgrade(EntityPlayer player, Class<?> upgradeClass) {
        return visitTravelersUpgrades(player, upgradeClass, ctx -> true);
    }

    public <T> boolean visitUpgrades(EntityPlayer player, Class<T> upgradeClass, UpgradeContextVisitor<T> visitor) {
        return BackpackEntityHelpers
            .visitPlayerBackpacks(player, BackpackEntityHelpers.SearchOrder.PLAYER_THEN_BAUBLES, backpack -> {
                Map<Integer, T> upgrades = backpack.getWrapper()
                    .gatherCapabilityUpgrades(upgradeClass);
                for (Map.Entry<Integer, T> entry : upgrades.entrySet()) {
                    if (visitor.visit(new PlayerUpgradeContext<>(backpack, entry.getKey(), entry.getValue()))) {
                        return true;
                    }
                }
                return false;
            });
    }

    public <T> boolean visitTravelersUpgrades(EntityPlayer player, Class<T> upgradeClass,
        UpgradeContextVisitor<T> visitor) {
        return BackpackEntityHelpers.visitWornBackpacks(player, backpack -> {
            Map<Integer, T> upgrades = backpack.getWrapper()
                .gatherCapabilityUpgrades(upgradeClass);
            for (Map.Entry<Integer, T> entry : upgrades.entrySet()) {
                if (visitor.visit(new PlayerUpgradeContext<>(backpack, entry.getKey(), entry.getValue()))) {
                    return true;
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
