package ruiseki.okbackpack.common.init;

import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraftforge.fluids.FluidRegistry;

import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.common.item.anvil.ItemAnvilUpgrade;
import ruiseki.okbackpack.common.item.arcane.ItemArcaneCraftingUpgrade;
import ruiseki.okbackpack.common.item.battery.ItemBatteryUpgrade;
import ruiseki.okbackpack.common.item.compacting.ItemAdvancedCompactingUpgrade;
import ruiseki.okbackpack.common.item.compacting.ItemCompactingUpgrade;
import ruiseki.okbackpack.common.item.crafting.ItemCraftingUpgrade;
import ruiseki.okbackpack.common.item.deposit.ItemAdvancedDepositUpgrade;
import ruiseki.okbackpack.common.item.deposit.ItemDepositUpgrade;
import ruiseki.okbackpack.common.item.energizednode.ItemEnergizedNodeUpgrade;
import ruiseki.okbackpack.common.item.everlasting.ItemEverlastingUpgrade;
import ruiseki.okbackpack.common.item.feeding.ItemAdvancedFeedingUpgrade;
import ruiseki.okbackpack.common.item.feeding.ItemFeedingUpgrade;
import ruiseki.okbackpack.common.item.filter.ItemAdvancedFilterUpgrade;
import ruiseki.okbackpack.common.item.filter.ItemFilterUpgrade;
import ruiseki.okbackpack.common.item.inception.ItemInceptionUpgrade;
import ruiseki.okbackpack.common.item.infinity.ItemInfinityUpgrade;
import ruiseki.okbackpack.common.item.infinity.ItemSurvivalInfinityUpgrade;
import ruiseki.okbackpack.common.item.jukebox.ItemAdvancedJukeboxUpgrade;
import ruiseki.okbackpack.common.item.jukebox.ItemJukeboxUpgrade;
import ruiseki.okbackpack.common.item.magnet.ItemAdvancedMagnetUpgrade;
import ruiseki.okbackpack.common.item.magnet.ItemMagnetUpgrade;
import ruiseki.okbackpack.common.item.pickup.ItemAdvancedPickupUpgrade;
import ruiseki.okbackpack.common.item.pickup.ItemPickupUpgrade;
import ruiseki.okbackpack.common.item.pump.ItemAdvancedPumpUpgrade;
import ruiseki.okbackpack.common.item.pump.ItemPumpUpgrade;
import ruiseki.okbackpack.common.item.pump.xp.ItemXpPumpUpgrade;
import ruiseki.okbackpack.common.item.refill.ItemAdvancedRefillUpgrade;
import ruiseki.okbackpack.common.item.refill.ItemRefillUpgrade;
import ruiseki.okbackpack.common.item.restock.ItemAdvancedRestockUpgrade;
import ruiseki.okbackpack.common.item.restock.ItemRestockUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemAutoBlastingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemAutoSmeltingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemAutoSmokingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemBlastingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemSmeltingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemSmokingUpgrade;
import ruiseki.okbackpack.common.item.stack.ItemStackUpgrade;
import ruiseki.okbackpack.common.item.tank.ItemTankUpgrade;
import ruiseki.okbackpack.common.item.toolswapper.ItemAdvancedToolSwapperUpgrade;
import ruiseki.okbackpack.common.item.toolswapper.ItemToolSwapperUpgrade;
import ruiseki.okbackpack.common.item.travelers.bat.ItemBatUpgrade;
import ruiseki.okbackpack.common.item.travelers.blaze.ItemBlazeUpgrade;
import ruiseki.okbackpack.common.item.travelers.bookshelf.ItemBookshelfUpgrade;
import ruiseki.okbackpack.common.item.travelers.cactus.ItemCactusUpgrade;
import ruiseki.okbackpack.common.item.travelers.cake.ItemCakeUpgrade;
import ruiseki.okbackpack.common.item.travelers.chicken.ItemChickenUpgrade;
import ruiseki.okbackpack.common.item.travelers.cow.ItemCowUpgrade;
import ruiseki.okbackpack.common.item.travelers.creeper.ItemCreeperUpgrade;
import ruiseki.okbackpack.common.item.travelers.dragon.ItemDragonUpgrade;
import ruiseki.okbackpack.common.item.travelers.ghast.ItemGhastUpgrade;
import ruiseki.okbackpack.common.item.travelers.glowstone.ItemGlowstoneUpgrade;
import ruiseki.okbackpack.common.item.travelers.hay.ItemHayUpgrade;
import ruiseki.okbackpack.common.item.travelers.lapis.ItemLapisUpgrade;
import ruiseki.okbackpack.common.item.travelers.magma.ItemMagmaCubeUpgrade;
import ruiseki.okbackpack.common.item.travelers.ocelot.ItemOcelotUpgrade;
import ruiseki.okbackpack.common.item.travelers.quartz.ItemQuartzUpgrade;
import ruiseki.okbackpack.common.item.travelers.quiver.ItemQuiverUpgrade;
import ruiseki.okbackpack.common.item.travelers.rainbow.ItemRainbowUpgrade;
import ruiseki.okbackpack.common.item.travelers.redstone.ItemRedstoneUpgrade;
import ruiseki.okbackpack.common.item.travelers.slime.ItemSlimeUpgrade;
import ruiseki.okbackpack.common.item.travelers.spider.ItemSpiderUpgrade;
import ruiseki.okbackpack.common.item.travelers.sponge.ItemSpongeUpgrade;
import ruiseki.okbackpack.common.item.travelers.squid.ItemSquidUpgrade;
import ruiseki.okbackpack.common.item.travelers.wither.ItemWitherUpgrade;
import ruiseki.okbackpack.common.item.travelers.wolf.ItemWolfUpgrade;
import ruiseki.okbackpack.common.item.voiding.ItemAdvancedVoidUpgrade;
import ruiseki.okbackpack.common.item.voiding.ItemVoidUpgrade;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.Reference;
import ruiseki.okcore.item.IItem;
import ruiseki.okcore.registries.DeferredRegister;
import ruiseki.okcore.registries.RegistryObject;
import ruiseki.okcore.tag.Registries;

public final class OKBackpackItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Reference.MOD_ID);

    public static final RegistryObject<Item> BASE_UPGRADE = register("upgrade_base", () -> new ItemUpgrade<>());
    public static final RegistryObject<Item> STACK_UPGRADE = register("stack_upgrade", ItemStackUpgrade::new);
    public static final RegistryObject<Item> CRAFTING_UPGRADE = register("crafting_upgrade", ItemCraftingUpgrade::new);
    public static final RegistryObject<Item> MAGNET_UPGRADE = register("magnet_upgrade", ItemMagnetUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_MAGNET_UPGRADE = register(
        "advanced_magnet_upgrade",
        ItemAdvancedMagnetUpgrade::new);
    public static final RegistryObject<Item> FEEDING_UPGRADE = register("feeding_upgrade", ItemFeedingUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_FEEDING_UPGRADE = register(
        "advanced_feeding_upgrade",
        ItemAdvancedFeedingUpgrade::new);
    public static final RegistryObject<Item> PICKUP_UPGRADE = register("pickup_upgrade", ItemPickupUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_PICKUP_UPGRADE = register(
        "advanced_pickup_upgrade",
        ItemAdvancedPickupUpgrade::new);
    public static final RegistryObject<Item> VOID_UPGRADE = register("void_upgrade", ItemVoidUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_VOID_UPGRADE = register(
        "advanced_void_upgrade",
        ItemAdvancedVoidUpgrade::new);
    public static final RegistryObject<Item> EVERLASTING_UPGRADE = register(
        "everlasting_upgrade",
        ItemEverlastingUpgrade::new);
    public static final RegistryObject<Item> INCEPTION_UPGRADE = register(
        "inception_upgrade",
        ItemInceptionUpgrade::new);
    public static final RegistryObject<Item> FILTER_UPGRADE = register("filter_upgrade", ItemFilterUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_FILTER_UPGRADE = register(
        "advanced_filter_upgrade",
        ItemAdvancedFilterUpgrade::new);
    public static final RegistryObject<Item> COMPACTING_UPGRADE = register(
        "compacting_upgrade",
        ItemCompactingUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_COMPACTING_UPGRADE = register(
        "advanced_compacting_upgrade",
        ItemAdvancedCompactingUpgrade::new);
    public static final RegistryObject<Item> JUKEBOX_UPGRADE = register("jukebox_upgrade", ItemJukeboxUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_JUKEBOX_UPGRADE = register(
        "advanced_jukebox_upgrade",
        ItemAdvancedJukeboxUpgrade::new);
    public static final RegistryObject<Item> SMELTING_UPGRADE = register("smelting_upgrade", ItemSmeltingUpgrade::new);
    public static final RegistryObject<Item> AUTO_SMELTING_UPGRADE = register(
        "auto_smelting_upgrade",
        ItemAutoSmeltingUpgrade::new);

    public static final RegistryObject<Item> SMOKING_UPGRADE = register(
        "smoking_upgrade",
        Mods.EtFuturum::isModLoaded,
        ItemSmokingUpgrade::new);
    public static final RegistryObject<Item> AUTO_SMOKING_UPGRADE = register(
        "auto_smoking_upgrade",
        Mods.EtFuturum::isModLoaded,
        ItemAutoSmokingUpgrade::new);
    public static final RegistryObject<Item> BLASTING_UPGRADE = register(
        "blasting_upgrade",
        Mods.EtFuturum::isModLoaded,
        ItemBlastingUpgrade::new);
    public static final RegistryObject<Item> AUTO_BLASTING_UPGRADE = register(
        "auto_blasting_upgrade",
        Mods.EtFuturum::isModLoaded,
        ItemAutoBlastingUpgrade::new);

    public static final RegistryObject<Item> TOOL_SWAPPER_UPGRADE = register(
        "tool_swapper_upgrade",
        ItemToolSwapperUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_TOOL_SWAPPER_UPGRADE = register(
        "advanced_tool_swapper_upgrade",
        ItemAdvancedToolSwapperUpgrade::new);
    public static final RegistryObject<Item> REFILL_UPGRADE = register("refill_upgrade", ItemRefillUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_REFILL_UPGRADE = register(
        "advanced_refill_upgrade",
        ItemAdvancedRefillUpgrade::new);
    public static final RegistryObject<Item> DEPOSIT_UPGRADE = register("deposit_upgrade", ItemDepositUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_DEPOSIT_UPGRADE = register(
        "advanced_deposit_upgrade",
        ItemAdvancedDepositUpgrade::new);
    public static final RegistryObject<Item> RESTOCK_UPGRADE = register("restock_upgrade", ItemRestockUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_RESTOCK_UPGRADE = register(
        "advanced_restock_upgrade",
        ItemAdvancedRestockUpgrade::new);
    public static final RegistryObject<Item> ANVIL_UPGRADE = register("anvil_upgrade", ItemAnvilUpgrade::new);
    public static final RegistryObject<Item> BATTERY_UPGRADE = register("battery_upgrade", ItemBatteryUpgrade::new);
    public static final RegistryObject<Item> TANK_UPGRADE = register("tank_upgrade", ItemTankUpgrade::new);
    public static final RegistryObject<Item> PUMP_UPGRADE = register("pump_upgrade", ItemPumpUpgrade::new);
    public static final RegistryObject<Item> ADVANCED_PUMP_UPGRADE = register(
        "advanced_pump_upgrade",
        ItemAdvancedPumpUpgrade::new);
    public static final RegistryObject<Item> XP_PUMP_UPGRADE = register(
        "xp_pump_upgrade",
        ItemXpPumpUpgrade::new);
    public static final RegistryObject<Item> INFINITY_UPGRADE = register("infinity_upgrade", ItemInfinityUpgrade::new);
    public static final RegistryObject<Item> SURVIVAL_INFINITY_UPGRADE = register(
        "survival_infinity_upgrade",
        ItemSurvivalInfinityUpgrade::new);

    public static final RegistryObject<Item> ARCANE_CRAFTING_UPGRADE = register(
        "arcane_crafting_upgrade",
        () -> ModConfig.enableArcaneCraftingUpgrade && Mods.Thaumcraft.isModLoaded(),
        ItemArcaneCraftingUpgrade::new);
    public static final RegistryObject<Item> ENERGIZED_NODE_UPGRADE = register(
        "energized_node_upgrade",
        Mods.Thaumcraft::isModLoaded,
        ItemEnergizedNodeUpgrade::new);

    public static final RegistryObject<Item> REDSTONE_UPGRADE = register(
        "redstone_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemRedstoneUpgrade::new);
    public static final RegistryObject<Item> GLOWSTONE_UPGRADE = register(
        "glowstone_upgrade",
        ItemGlowstoneUpgrade::new);
    public static final RegistryObject<Item> RAINBOW_UPGRADE = register(
        "rainbow_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemRainbowUpgrade::new);
    public static final RegistryObject<Item> CACTUS_UPGRADE = register(
        "cactus_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemCactusUpgrade::new);
    public static final RegistryObject<Item> COW_UPGRADE = register(
        "cow_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemCowUpgrade::new);
    public static final RegistryObject<Item> BAT_UPGRADE = register(
        "bat_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemBatUpgrade::new);
    public static final RegistryObject<Item> SQUID_UPGRADE = register(
        "squid_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemSquidUpgrade::new);
    public static final RegistryObject<Item> WITHER_UPGRADE = register(
        "wither_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemWitherUpgrade::new);
    public static final RegistryObject<Item> CAKE_UPGRADE = register(
        "cake_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemCakeUpgrade::new);
    public static final RegistryObject<Item> SLIME_UPGRADE = register(
        "slime_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemSlimeUpgrade::new);
    public static final RegistryObject<Item> BOOKSHELF_UPGRADE = register(
        "bookshelf_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemBookshelfUpgrade::new);
    public static final RegistryObject<Item> WOLF_UPGRADE = register(
        "wolf_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemWolfUpgrade::new);
    public static final RegistryObject<Item> OCELOT_UPGRADE = register(
        "ocelot_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemOcelotUpgrade::new);
    public static final RegistryObject<Item> QUIVER_UPGRADE = register(
        "quiver_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemQuiverUpgrade::new);
    public static final RegistryObject<Item> CHICKEN_UPGRADE = register(
        "chicken_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemChickenUpgrade::new);
    public static final RegistryObject<Item> MAGMA_CUBE_UPGRADE = register(
        "magma_cube_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemMagmaCubeUpgrade::new);
    public static final RegistryObject<Item> DRAGON_UPGRADE = register(
        "dragon_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemDragonUpgrade::new);
    public static final RegistryObject<Item> BLAZE_UPGRADE = register(
        "blaze_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemBlazeUpgrade::new);
    public static final RegistryObject<Item> SPONGE_UPGRADE = register(
        "sponge_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemSpongeUpgrade::new);
    public static final RegistryObject<Item> CREEPER_UPGRADE = register(
        "creeper_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemCreeperUpgrade::new);
    public static final RegistryObject<Item> GHAST_UPGRADE = register(
        "ghast_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemGhastUpgrade::new);
    public static final RegistryObject<Item> SPIDER_UPGRADE = register(
        "spider_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemSpiderUpgrade::new);
    public static final RegistryObject<Item> LAPIS_UPGRADE = register(
        "lapis_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemLapisUpgrade::new);
    public static final RegistryObject<Item> QUARTZ_UPGRADE = register(
        "quartz_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemQuartzUpgrade::new);
    public static final RegistryObject<Item> HAY_UPGRADE = register(
        "hay_upgrade",
        () -> ModConfig.enableTravelersUpgrades,
        ItemHayUpgrade::new);

    private static RegistryObject<Item> register(String name, Supplier<IItem> itemSupplier) {
        return register(name, () -> true, itemSupplier);
    }

    private static RegistryObject<Item> register(String name, Supplier<Boolean> configCondition,
        Supplier<IItem> itemSupplier) {
        if (!configCondition.get()) {
            return RegistryObject.empty();
        }

        return ITEMS.register(
            name,
            () -> itemSupplier.get()
                .get());
    }

    public static void register() {
        ITEMS.register();
    }

    private OKBackpackItems() {}
}
