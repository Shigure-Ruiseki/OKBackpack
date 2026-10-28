package ruiseki.backpack.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.backpack.api.IAttackEntityResponseUpgrade;
import ruiseki.backpack.api.IBlockClickResponseUpgrade;
import ruiseki.backpack.backpack.UUIDDeduplicator;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.backpack.helpers.PlayerInventoryProvider;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.event.interaction.PlayerInteractEvent;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.helper.StorageInventoryHelpers;
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
        if (event.getWorld().isRemote) {
            return;
        }
        EntityPlayer player = event.getEntity();
        BlockPos pos = event.getPos();
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, inventoryHandlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> UUIDDeduplicator.getWrapper(player, wrapper))
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

    private void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player.worldObj.isRemote) {
            return;
        }
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, inventoryHandlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> UUIDDeduplicator.getWrapper(player, wrapper))
                    .map(wrapper -> {
                        for (IAttackEntityResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IAttackEntityResponseUpgrade.class)) {
                            if (upgrade.onAttackEntity(player)) {
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
        if (ItemHelpers.isEmpty(itemEntity.getEntityItem())) {
            return;
        }

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
                    .map(wrapper -> UUIDDeduplicator.getWrapper(player, wrapper))
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
                        .map(wrapper -> UUIDDeduplicator.getWrapper(player, wrapper))
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
            itemEntity.setEntityItemStack(remainingStack.get());
            event.setCanceled(true); // cancelling even when the stack isn't empty at this point to prevent full stack
                                     // from before pickup to be picked up by player
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote || player.isDead) return;
        this.tickBackpacks(player);
    }

    private void tickBackpacks(EntityPlayer player) {
        BlockPos pos = new BlockPos(player);
        PlayerInventoryProvider.get()
            .runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
                CapabilityHelpers.getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> UUIDDeduplicator.getWrapper(player, wrapper))
                    .ifPresent(wrapper -> {
                        wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(ITickableUpgrade.class)
                            .forEach(upgrade -> upgrade.tick(player, player.worldObj, pos));
                    });
                return false;
            }, GeneralConfig.onlyWornBackpackTriggersUpgrades);
    }

}
