package ruiseki.backpack.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.BlockEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.backpack.api.IArrowResponseUpgrade;
import ruiseki.backpack.api.IAttackEntityResponseUpgrade;
import ruiseki.backpack.api.IBlockClickResponseUpgrade;
import ruiseki.backpack.api.IGhastTargetResponseUpgrade;
import ruiseki.backpack.api.IHarvestDropResponseUpgrade;
import ruiseki.backpack.api.ILivingClimbingResponseUpgrade;
import ruiseki.backpack.api.ILivingFallResponseUpgrade;
import ruiseki.backpack.api.ILivingHurtResponseUpgrade;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.backpack.helpers.InteractionHelpers;
import ruiseki.backpack.helpers.PlayerInventoryProvider;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okbackpack.common.block.BlockSleepingBagConfig;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.event.entity.GhastTargetEvent;
import ruiseki.okcore.event.entity.LivingClimbEvent;
import ruiseki.okcore.event.gui.SlotClickEvent;
import ruiseki.okcore.event.interaction.PlayerInteractEvent;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.helper.StorageInventoryHelpers;
import ruiseki.okstoragecore.upgrades.IPickupXpUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;

public class CommonEventHandler {

    public static final CommonEventHandler INSTANCE = new CommonEventHandler();
    private static final int BACKPACK_CHECK_COOLDOWN = 40;

    private final Map<Integer, Long> nextBackpackCheckTime = new HashMap<>();

    public CommonEventHandler() {}

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        int dimensionId = world.provider.dimensionId;

        boolean runDedupeLogic = GeneralConfig.tickDedupeLogicDisabled;
        boolean runSlownessLogic = GeneralConfig.tooManyBackpacksSlowness;
        if (event.phase != TickEvent.Phase.END || (!runSlownessLogic && !runDedupeLogic)
            || nextBackpackCheckTime.getOrDefault(world.provider.dimensionId, 0L) > event.world.getTotalWorldTime()) {
            return;
        }

        nextBackpackCheckTime.put(dimensionId, world.getTotalWorldTime() + BACKPACK_CHECK_COOLDOWN);

        Set<UUID> backpackIds = new HashSet<>();

        for (Object object : world.playerEntities) {
            if (!(object instanceof EntityPlayer player) || player.isDead) {
                continue;
            }

            AtomicInteger numberOfBackpacks = new AtomicInteger(0);
            PlayerInventoryProvider.get()
                .runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
                    if (runSlownessLogic) {
                        numberOfBackpacks.incrementAndGet();
                    }
                    if (runDedupeLogic) {
                        CapabilityHelpers.getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                            .ifPresent(wrapper -> addBackpackIdIfUniqueOrDedupe(backpackIds, wrapper));
                    }
                    return false;
                });

            if (runSlownessLogic) {
                int maxNumberOfBackpacks = GeneralConfig.maxNumberOfBackpacks;
                if (numberOfBackpacks.get() > maxNumberOfBackpacks) {
                    int numberOfSlownessLevels = Math.min(
                        10,
                        (int) Math.ceil(
                            (numberOfBackpacks.get() - maxNumberOfBackpacks)
                                * GeneralConfig.slownessLevelsPerAdditionalBackpack));
                    player.addPotionEffect(
                        new PotionEffect(
                            Potion.moveSlowdown.id,
                            BACKPACK_CHECK_COOLDOWN * 2,
                            numberOfSlownessLevels - 1,
                            false));
                }
            }
        }
    }

    private static void addBackpackIdIfUniqueOrDedupe(Set<UUID> backpackIds, IBackpackWrapper backpackWrapper) {
        backpackWrapper.getContentsUuid()
            .ifPresent(backpackId -> {
                if (backpackIds.contains(backpackId)) {
                    backpackWrapper.removeContentsUUIDTag();
                    backpackWrapper.onContentsNbtUpdated();
                } else {
                    backpackIds.add(backpackId);
                }
            });
    }

    @SubscribeEvent
    public void onBlockClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getWorld().isRemote) return;
        EntityPlayer player = event.getEntity();
        BlockPos pos = event.getPos();
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, inventoryHandlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (IBlockClickResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IBlockClickResponseUpgrade.class)) {
                            if (upgrade.onBlockClick(player, pos)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null || player.worldObj.isRemote) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, inventoryHandlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (IAttackEntityResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IAttackEntityResponseUpgrade.class)) {
                            if (upgrade.onAttackEntity(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity == null || entity.worldObj.isRemote || !(entity instanceof EntityPlayer player)) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, handlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (ILivingHurtResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(ILivingHurtResponseUpgrade.class)) {
                            if (upgrade.onLivingHurt(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity == null || entity.worldObj.isRemote || !(entity instanceof EntityPlayer player)) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, handlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (ILivingFallResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(ILivingFallResponseUpgrade.class)) {
                            if (upgrade.onLivingFall(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        EntityPlayer player = event.harvester;
        if (player == null || player.worldObj.isRemote) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, handlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (IHarvestDropResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IHarvestDropResponseUpgrade.class)) {
                            if (upgrade.onHarvestDrops(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onLivingClimb(LivingClimbEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity == null || entity.worldObj.isRemote || !(entity instanceof EntityPlayer player)) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, handlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (ILivingClimbingResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(ILivingClimbingResponseUpgrade.class)) {
                            if (upgrade.onLivingClimb(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onArrowNock(ArrowNockEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity == null || entity.worldObj.isRemote || !(entity instanceof EntityPlayer player)) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, handlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (IArrowResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IArrowResponseUpgrade.class)) {
                            if (upgrade.onArrowNock(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onArrowLoose(ArrowLooseEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity == null || entity.worldObj.isRemote || !(entity instanceof EntityPlayer player)) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, handlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (IArrowResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IArrowResponseUpgrade.class)) {
                            if (upgrade.onArrowLoose(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onGhastTarget(GhastTargetEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity == null || entity.worldObj.isRemote || !(entity instanceof EntityPlayer player)) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, handlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (IGhastTargetResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IGhastTargetResponseUpgrade.class)) {
                            if (upgrade.onGhastTarget(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onPickupXp(PlayerPickupXpEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null || player.worldObj.isRemote) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, handlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (IPickupXpUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IPickupXpUpgrade.class)) {
                            if (upgrade.onPickupXp(event)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        EntityItem itemEntity = event.item;
        if (ItemHelpers.isEmpty(itemEntity.getEntityItem())) return;

        AtomicReference<ItemStack> remainingStackSimulated = new AtomicReference<>(
            ItemHelpers.copy(itemEntity.getEntityItem()));
        EntityPlayer player = event.entityPlayer;
        World world = player.worldObj;
        if (world.isRemote || player.isDead) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, inventoryHandlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        remainingStackSimulated.set(
                            StorageInventoryHelpers.runPickupOnPickupResponseUpgrades(
                                world,
                                wrapper.getUpgradeHandler(),
                                remainingStackSimulated.get(),
                                true));
                        return ItemHelpers.isEmpty(remainingStackSimulated.get());
                    })
                    .orElse(false),
                GeneralConfig.onlyWornBackpackTriggersUpgrades);

        if (ItemHelpers.getCount(remainingStackSimulated.get()) != ItemHelpers.getCount(itemEntity.getEntityItem())) {
            AtomicReference<ItemStack> remainingStack = new AtomicReference<>(
                ItemHelpers.copy(itemEntity.getEntityItem()));
            PlayerInventoryProvider.get()
                .runOnBackpacks(
                    player,
                    (backpack, inventoryHandlerName, identifier, slot) -> CapabilityHelpers
                        .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                        .map(wrapper -> {
                            remainingStack.set(
                                StorageInventoryHelpers.runPickupOnPickupResponseUpgrades(
                                    world,
                                    player,
                                    wrapper.getUpgradeHandler(),
                                    remainingStack.get(),
                                    false));
                            return ItemHelpers.isEmpty(remainingStack.get());
                        })
                        .orElse(false),
                    GeneralConfig.onlyWornBackpackTriggersUpgrades);

            ItemStack remainder = remainingStack.get();
            if (ItemHelpers.isEmpty(remainder)) itemEntity.setDead();
            itemEntity.setEntityItemStack(remainder);
            event.setCanceled(true); // cancelling even when the stack isn't empty at this point to prevent full stack
                                     // from before pickup to be picked up by player
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if (player == null || player.worldObj.isRemote || player.isDead) return;
        this.tickBackpacks(player);
    }

    private void tickBackpacks(EntityPlayer player) {
        BlockPos pos = new BlockPos(player);
        PlayerInventoryProvider.get()
            .runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
                CapabilityHelpers.getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .ifPresent(wrapper -> {
                        wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(ITickableUpgrade.class)
                            .forEach(upgrade -> upgrade.tick(player, player.worldObj, pos));
                    });
                return false;
            }, GeneralConfig.onlyWornBackpackTriggersUpgrades);
    }

    @SubscribeEvent
    public void onSlotClick(SlotClickEvent.Pre event) {
        if (event.isCanceled()) return;
        if (InteractionHelpers.slotClick(
            event.getContainer(),
            event.getPlayer(),
            event.getSlotId(),
            event.getMouseButton(),
            event.getMode())) {
            event.setResultStack(null);
            event.setCanceled(true);
        }
    }

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
}
