package ruiseki.okbackpack.common.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.github.bsideup.jabel.Desugar;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.okbackpack.api.wrapper.IWitherUpgrade;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okbackpack.common.item.travelers.blaze.BlazeUpgradeWrapper;
import ruiseki.okbackpack.common.item.travelers.creeper.CreeperUpgradeWrapper;
import ruiseki.okbackpack.common.item.travelers.ghast.GhastUpgradeWrapper;
import ruiseki.okbackpack.common.item.travelers.hay.HayUpgradeWrapper;
import ruiseki.okbackpack.common.item.travelers.lapis.LapisUpgradeWrapper;
import ruiseki.okbackpack.common.item.travelers.quiver.QuiverUpgradeWrapper;
import ruiseki.okbackpack.common.item.travelers.rainbow.RainbowUpgradeWrapper;
import ruiseki.okbackpack.common.item.travelers.slime.SlimeUpgradeWrapper;

public class BackpackEventHandler {

    private static final ItemStack SINGLE_ARROW = new ItemStack(Items.arrow);
    private static final float LUCKY_LAPIS_CHANCE = 0.15F;
    private static final float HAY_DOUBLE_CROP_CHANCE = 0.40F;
    private static final float HAY_GRASS_DROP_CHANCE = 0.15F;
    private static final double GHAST_RANGE = 20.0D;
    private static final int GHAST_RETALIATE_TICKS = 200;
    private static final String GHAST_ANGER_PLAYER_TAG = "OKBGhastAngerPlayer";
    private static final String GHAST_ANGER_UNTIL_TAG = "OKBGhastAngerUntil";

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

        tickBackpacks(player);
        neutralizeNearbyGhasts(player);
    }

    private void tickBackpacks(EntityPlayer player) {
        boolean backpackOpen = player.openContainer instanceof BackPackContainer;
        BackpackEntityHelper
            .visitPlayerBackpacks(player, BackpackEntityHelper.SearchOrder.PLAYER_THEN_BAUBLES, context -> {
                ItemStack stack = context.getStack();
                if (stack.getTagCompound() == null || !stack.getTagCompound()
                    .hasKey(BackpackWrapper.BACKPACK_NBT)) {
                    BackpackEntityHelper.persistBackpack(context);
                    return false;
                }

                if (!backpackOpen && context.getWrapper()
                    .tick(player)) {
                    BackpackEntityHelper.persistBackpack(context);
                }
                return false;
            });
    }

    @SubscribeEvent
    public void onPlayerFall(LivingFallEvent event) {
        if (!(event.entity instanceof EntityPlayer player)) return;
        if (player.worldObj.isRemote) return;

        if (hasUpgrade(player, RainbowUpgradeWrapper.class)) {
            player.fallDistance = 0F;
            event.setCanceled(true);
            return;
        }

        if (hasUpgrade(player, SlimeUpgradeWrapper.class)) {
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

        if (hasUpgrade(player, BlazeUpgradeWrapper.class)) {
            player.fallDistance = 0F;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer player)) return;
        if (player.worldObj.isRemote) return;

        if (isBlazeFireballDamage(event) && hasUpgrade(player, BlazeUpgradeWrapper.class)) {
            event.setCanceled(true);
            return;
        }

        if (event.source == DamageSource.wither && hasEnabledWitherUpgrade(player)) {
            event.setCanceled(true);
            return;
        }

        if (isGhastDamage(event) && hasUpgrade(player, GhastUpgradeWrapper.class)
            && !isPlayerProvokingGhasts(player, player.worldObj.getTotalWorldTime())) {
            event.setCanceled(true);
            return;
        }

        if (wouldBeFatal(player, event) && event.source != DamageSource.outOfWorld && triggerCreeperRescue(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null || player.worldObj.isRemote) return;
        if (!(event.target instanceof EntityLivingBase livingTarget)) return;

        if (livingTarget instanceof EntityGhast ghast && hasUpgrade(player, GhastUpgradeWrapper.class)) {
            markGhastRetaliation(ghast, player);
        }

        if (hasEnabledWitherUpgrade(player)) {
            livingTarget.addPotionEffect(new PotionEffect(Potion.wither.id, 60, 1, true));
        }
    }

    @SubscribeEvent
    public void onArrowNock(ArrowNockEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null) return;
        if (player.capabilities.isCreativeMode) return;
        if (player.inventory.hasItem(Items.arrow)) return;
        if (!hasUpgrade(player, QuiverUpgradeWrapper.class)) return;
        if (findQuiverSource(player) == null) return;

        player.setItemInUse(event.result, event.result.getMaxItemUseDuration());
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onArrowLoose(ArrowLooseEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null) return;
        if (!hasUpgrade(player, QuiverUpgradeWrapper.class)) return;
        if (player.capabilities.isCreativeMode) return;
        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, event.bow) > 0) return;

        BackpackEntityHelper.BackpackContext quiverSource = findQuiverSource(player);
        if (quiverSource == null) return;

        event.setCanceled(true);
        if (player.worldObj.isRemote) return;

        shootArrowFromQuiver(player, event.bow, event.charge, quiverSource);
    }

    @SubscribeEvent
    public void onPlayerPickupXp(PlayerPickupXpEvent event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj.isRemote) return;
        if (!hasUpgrade(event.entityPlayer, LapisUpgradeWrapper.class)) return;
        if (event.entityPlayer.getRNG()
            .nextFloat() < LUCKY_LAPIS_CHANCE) {
            event.orb.xpValue *= 2;
        }
    }

    @SubscribeEvent
    public void onHarvestDrops(HarvestDropsEvent event) {
        if (event.harvester == null || event.world.isRemote) return;
        if (!hasUpgrade(event.harvester, HayUpgradeWrapper.class)) return;

        if (isCropBlock(event.block) && event.world.rand.nextFloat() < HAY_DOUBLE_CROP_CHANCE) {
            List<ItemStack> extraDrops = new ArrayList<>();
            for (ItemStack drop : event.drops) {
                if (drop != null) {
                    extraDrops.add(drop.copy());
                }
            }
            event.drops.addAll(extraDrops);
        }

        if (event.block == Blocks.tallgrass && event.world.rand.nextFloat() < HAY_GRASS_DROP_CHANCE) {
            event.drops.add(getRandomGrassCrop(event.world));
        }
    }

    private boolean isCropBlock(Block block) {
        return block instanceof BlockCrops || block instanceof BlockNetherWart
            || block == Blocks.carrots
            || block == Blocks.potatoes;
    }

    private ItemStack getRandomGrassCrop(World world) {
        ItemStack[] candidates = new ItemStack[] { new ItemStack(Items.carrot), new ItemStack(Items.potato),
            new ItemStack(Items.wheat) };
        return candidates[world.rand.nextInt(candidates.length)].copy();
    }

    private boolean isBlazeFireballDamage(LivingHurtEvent event) {
        return event.source.getSourceOfDamage() instanceof EntitySmallFireball
            && event.source.getEntity() instanceof EntityBlaze;
    }

    private boolean isGhastDamage(LivingHurtEvent event) {
        return event.source.getSourceOfDamage() instanceof EntitySmallFireball
            && event.source.getEntity() instanceof EntityGhast;
    }

    private boolean wouldBeFatal(EntityPlayer player, LivingHurtEvent event) {
        return player.getHealth() - event.ammount <= 0.0F;
    }

    private boolean triggerCreeperRescue(EntityPlayer player) {
        long worldTime = player.worldObj.getTotalWorldTime();
        return visitUpgrades(player, CreeperUpgradeWrapper.class, ctx -> {
            if (!ctx.upgrade.isReady(worldTime)) return false;

            ctx.upgrade.trigger(worldTime);
            player.setHealth(1.0F);
            player.extinguish();
            player.hurtResistantTime = 20;
            player.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 2400, 0, true));
            player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 100, 1, true));
            player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 2400, 0, true));
            playRescueExplosion(player);
            BackpackEntityHelper.persistBackpack(ctx.backpack);
            return true;
        });
    }

    private void playRescueExplosion(EntityPlayer player) {
        player.worldObj.playSoundAtEntity(player, "random.explode", 1.0F, 1.0F);
        if (player.worldObj instanceof WorldServer worldServer) {
            worldServer.func_147487_a(
                "hugeexplosion",
                player.posX,
                player.posY + player.height * 0.5D,
                player.posZ,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D);
        }
    }

    private void neutralizeNearbyGhasts(EntityPlayer player) {
        if (!hasUpgrade(player, GhastUpgradeWrapper.class)) return;

        long worldTime = player.worldObj.getTotalWorldTime();
        List<EntityGhast> ghasts = player.worldObj
            .getEntitiesWithinAABB(EntityGhast.class, player.boundingBox.expand(GHAST_RANGE, 8.0D, GHAST_RANGE));
        for (EntityGhast ghast : ghasts) {
            if (!isGhastAggressiveToPlayer(ghast, player, worldTime) && ghast.getAttackTarget() == player) {
                ghast.setAttackTarget(null);
                ghast.setRevengeTarget(null);
            }
        }
    }

    private void markGhastRetaliation(EntityGhast ghast, EntityPlayer player) {
        ghast.getEntityData()
            .setString(
                GHAST_ANGER_PLAYER_TAG,
                player.getUniqueID()
                    .toString());
        ghast.getEntityData()
            .setLong(GHAST_ANGER_UNTIL_TAG, player.worldObj.getTotalWorldTime() + GHAST_RETALIATE_TICKS);
        ghast.setAttackTarget(player);
        ghast.setRevengeTarget(player);
    }

    private boolean isPlayerProvokingGhasts(EntityPlayer player, long worldTime) {
        List<EntityGhast> ghasts = player.worldObj
            .getEntitiesWithinAABB(EntityGhast.class, player.boundingBox.expand(GHAST_RANGE, 8.0D, GHAST_RANGE));
        for (EntityGhast ghast : ghasts) {
            if (isGhastAggressiveToPlayer(ghast, player, worldTime)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGhastAggressiveToPlayer(EntityGhast ghast, EntityPlayer player, long worldTime) {
        NBTTagCompound data = ghast.getEntityData();
        long angerUntil = data.getLong(GHAST_ANGER_UNTIL_TAG);
        if (angerUntil <= worldTime) {
            data.removeTag(GHAST_ANGER_PLAYER_TAG);
            data.removeTag(GHAST_ANGER_UNTIL_TAG);
            return false;
        }
        return player.getUniqueID()
            .toString()
            .equals(data.getString(GHAST_ANGER_PLAYER_TAG));
    }

    private BackpackEntityHelper.BackpackContext findQuiverSource(EntityPlayer player) {
        final BackpackEntityHelper.BackpackContext[] result = new BackpackEntityHelper.BackpackContext[1];
        BackpackEntityHelper
            .visitPlayerBackpacks(player, BackpackEntityHelper.SearchOrder.PLAYER_THEN_BAUBLES, context -> {
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

    private void shootArrowFromQuiver(EntityPlayer player, ItemStack bow, int charge,
        BackpackEntityHelper.BackpackContext source) {
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
        BackpackEntityHelper.persistBackpack(source);

        player.worldObj.playSoundAtEntity(
            player,
            "random.bow",
            1.0F,
            1.0F / (player.getRNG()
                .nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
        player.worldObj.spawnEntityInWorld(arrow);
    }

    private float getArrowVelocity(int charge) {
        float velocity = charge / 20.0F;
        velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
        if (velocity > 1.0F) {
            velocity = 1.0F;
        }
        return velocity;
    }

    private boolean hasUpgrade(EntityPlayer player, Class<?> upgradeClass) {
        return visitUpgrades(player, upgradeClass, ctx -> true);
    }

    private boolean hasEnabledWitherUpgrade(EntityPlayer player) {
        return visitUpgrades(player, IWitherUpgrade.class, ctx -> ctx.upgrade.isEnabled());
    }

    private <T> boolean visitUpgrades(EntityPlayer player, Class<T> upgradeClass, UpgradeContextVisitor<T> visitor) {
        return BackpackEntityHelper
            .visitPlayerBackpacks(player, BackpackEntityHelper.SearchOrder.PLAYER_THEN_BAUBLES, backpack -> {
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
        if (targetInventory == null) return pickupStack;

        for (int i = 0; i < targetInventory.getSizeInventory(); i++) {
            BackpackEntityHelper.BackpackContext context = BackpackEntityHelper.getBackpack(player, type, i);
            if (context == null) continue;
            if (!context.getWrapper()
                .canPickupItem(pickupStack)) continue;

            ItemStack before = pickupStack.copy();
            ItemStack result = context.getWrapper()
                .insertItem(pickupStack, false);
            boolean changed = result == null || result.stackSize != before.stackSize;

            if (changed) {
                BackpackEntityHelper.persistBackpack(context);
            }

            pickupStack = result;
            if (pickupStack == null || pickupStack.stackSize <= 0) {
                return null;
            }
        }

        return pickupStack;
    }

    private interface UpgradeContextVisitor<T> {

        boolean visit(PlayerUpgradeContext<T> context);
    }

    @Desugar
    private record PlayerUpgradeContext<T> (BackpackEntityHelper.BackpackContext backpack, int upgradeSlot,
        T upgrade) {}
}
