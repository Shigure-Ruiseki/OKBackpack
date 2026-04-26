package ruiseki.okbackpack.common.init;

import java.util.function.BooleanSupplier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Level;

import ruiseki.okbackpack.OKBackpack;
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
import ruiseki.okcore.item.IItem;

public enum ModItems {

    // spotless: off

    BASE_UPGRADE(new ItemUpgrade<>()),
    STACK_UPGRADE(new ItemStackUpgrade()),
    CRAFTING_UPGRADE(new ItemCraftingUpgrade()),
    MAGNET_UPGRADE(new ItemMagnetUpgrade()),
    ADVANCED_MAGNET_UPGRADE(new ItemAdvancedMagnetUpgrade()),
    FEEDING_UPGRADE(new ItemFeedingUpgrade()),
    ADVANCED_FEEDING_UPGRADE(new ItemAdvancedFeedingUpgrade()),
    PICKUP_UPGRADE(new ItemPickupUpgrade()),
    ADVANCED_PICKUP_UPGRADE(new ItemAdvancedPickupUpgrade()),
    VOID_UPGRADE(new ItemVoidUpgrade()),
    ADVANCED_VOID_UPGRADE(new ItemAdvancedVoidUpgrade()),
    EVERLASTING_UPGRADE(new ItemEverlastingUpgrade()),
    INCEPTION_UPGRADE(new ItemInceptionUpgrade()),
    FILTER_UPGRADE(new ItemFilterUpgrade()),
    ADVANCED_FILTER_UPGRADE(new ItemAdvancedFilterUpgrade()),
    COMPACTING_UPGRADE(new ItemCompactingUpgrade()),
    ADVANCED_COMPACTING_UPGRADE(new ItemAdvancedCompactingUpgrade()),
    JUKEBOX_UPGRADE(new ItemJukeboxUpgrade()),
    ADVANCED_JUKEBOX_UPGRADE(new ItemAdvancedJukeboxUpgrade()),
    SMELTING_UPGRADE(new ItemSmeltingUpgrade()),
    AUTO_SMELTING_UPGRADE(new ItemAutoSmeltingUpgrade()),
    SMOKING_UPGRADE(new ItemSmokingUpgrade(), Mods.EtFuturum),
    AUTO_SMOKING_UPGRADE(new ItemAutoSmokingUpgrade(), Mods.EtFuturum),
    BLASTING_UPGRADE(new ItemBlastingUpgrade(), Mods.EtFuturum),
    AUTO_BLASTING_UPGRADE(new ItemAutoBlastingUpgrade(), Mods.EtFuturum),
    TOOL_SWAPPER_UPGRADE(new ItemToolSwapperUpgrade()),
    ADVANCED_TOOL_SWAPPER_UPGRADE(new ItemAdvancedToolSwapperUpgrade()),
    REFILL_UPGRADE(new ItemRefillUpgrade()),
    ADVANCED_REFILL_UPGRADE(new ItemAdvancedRefillUpgrade()),
    DEPOSIT_UPGRADE(new ItemDepositUpgrade()),
    ADVANCED_DEPOSIT_UPGRADE(new ItemAdvancedDepositUpgrade()),
    RESTOCK_UPGRADE(new ItemRestockUpgrade()),
    ADVANCED_RESTOCK_UPGRADE(new ItemAdvancedRestockUpgrade()),
    ANVIL_UPGRADE(new ItemAnvilUpgrade()),
    BATTERY_UPGRADE(new ItemBatteryUpgrade()),
    TANK_UPGRADE(new ItemTankUpgrade()),
    INFINITY_UPGRADE(new ItemInfinityUpgrade()),
    SURVIVAL_INFINITY_UPGRADE(new ItemSurvivalInfinityUpgrade()),
    ARCANE_CRAFTING_UPGRADE(new ItemArcaneCraftingUpgrade(), Mods.Thaumcraft, ModItems::arcaneCraftingEnabled),
    ENERGIZED_NODE_UPGRADE(new ItemEnergizedNodeUpgrade(), Mods.Thaumcraft),
    REDSTONE_UPGRADE(new ItemRedstoneUpgrade(), ModItems::travelersUpgradesEnabled),
    GLOWSTONE_UPGRADE(new ItemGlowstoneUpgrade(), ModItems::travelersUpgradesEnabled),
    RAINBOW_UPGRADE(new ItemRainbowUpgrade(), ModItems::travelersUpgradesEnabled),
    CACTUS_UPGRADE(new ItemCactusUpgrade(), ModItems::travelersUpgradesEnabled),
    COW_UPGRADE(new ItemCowUpgrade(), ModItems::travelersUpgradesEnabled),
    BAT_UPGRADE(new ItemBatUpgrade(), ModItems::travelersUpgradesEnabled),
    SQUID_UPGRADE(new ItemSquidUpgrade(), ModItems::travelersUpgradesEnabled),
    WITHER_UPGRADE(new ItemWitherUpgrade(), ModItems::travelersUpgradesEnabled),
    CAKE_UPGRADE(new ItemCakeUpgrade(), ModItems::travelersUpgradesEnabled),
    SLIME_UPGRADE(new ItemSlimeUpgrade(), ModItems::travelersUpgradesEnabled),
    BOOKSHELF_UPGRADE(new ItemBookshelfUpgrade(), ModItems::travelersUpgradesEnabled),
    WOLF_UPGRADE(new ItemWolfUpgrade(), ModItems::travelersUpgradesEnabled),
    OCELOT_UPGRADE(new ItemOcelotUpgrade(), ModItems::travelersUpgradesEnabled),
    QUIVER_UPGRADE(new ItemQuiverUpgrade(), ModItems::travelersUpgradesEnabled),
    CHICKEN_UPGRADE(new ItemChickenUpgrade(), ModItems::travelersUpgradesEnabled),
    MAGMA_CUBE_UPGRADE(new ItemMagmaCubeUpgrade(), ModItems::travelersUpgradesEnabled),
    DRAGON_UPGRADE(new ItemDragonUpgrade(), ModItems::travelersUpgradesEnabled),
    BLAZE_UPGRADE(new ItemBlazeUpgrade(), ModItems::travelersUpgradesEnabled),
    SPONGE_UPGRADE(new ItemSpongeUpgrade(), ModItems::travelersUpgradesEnabled),
    CREEPER_UPGRADE(new ItemCreeperUpgrade(), ModItems::travelersUpgradesEnabled),
    GHAST_UPGRADE(new ItemGhastUpgrade(), ModItems::travelersUpgradesEnabled),
    SPIDER_UPGRADE(new ItemSpiderUpgrade(), ModItems::travelersUpgradesEnabled),
    LAPIS_UPGRADE(new ItemLapisUpgrade(), ModItems::travelersUpgradesEnabled),
    QUARTZ_UPGRADE(new ItemQuartzUpgrade(), ModItems::travelersUpgradesEnabled),
    HAY_UPGRADE(new ItemHayUpgrade(), ModItems::travelersUpgradesEnabled),

    //
    ;
    // spotless: on

    public static final ModItems[] VALUES = values();

    public static void preInit() {
        for (ModItems item : VALUES) {
            try {
                if (!item.isEnabled()) {
                    OKBackpack.okLog(Level.INFO, "Skipping " + item.name() + " (disabled by config)");
                    continue;
                }
                if (item.requiredMod != null && !item.requiredMod.isModLoaded()) {
                    OKBackpack
                        .okLog(Level.INFO, "Skipping " + item.name() + " (requires " + item.requiredMod.modid + ")");
                    continue;
                }
                item.item.init();
                OKBackpack.okLog(Level.INFO, "Successfully initialized " + item.name());
            } catch (Exception e) {
                OKBackpack.okLog(Level.ERROR, "Failed to initialize item: +" + item.name());
            }
        }
    }

    private final IItem item;
    private final Mods requiredMod;
    private final BooleanSupplier enabledCheck;

    ModItems(IItem item) {
        this(item, null, () -> true);
    }

    ModItems(IItem item, BooleanSupplier enabledCheck) {
        this(item, null, enabledCheck);
    }

    ModItems(IItem item, Mods requiredMod) {
        this(item, requiredMod, () -> true);
    }

    ModItems(IItem item, Mods requiredMod, BooleanSupplier enabledCheck) {
        this.item = item;
        this.requiredMod = requiredMod;
        this.enabledCheck = enabledCheck == null ? () -> true : enabledCheck;
    }

    public Item getItem() {
        return item.getItem();
    }

    public String getName() {
        return item.getItem()
            .getUnlocalizedName()
            .replace("item.", "");
    }

    public ItemStack newItemStack() {
        return newItemStack(1);
    }

    public ItemStack newItemStack(int count) {
        return newItemStack(count, 0);
    }

    public ItemStack newItemStack(int count, int meta) {
        return new ItemStack(this.getItem(), count, meta);
    }

    public boolean isEnabled() {
        return enabledCheck.getAsBoolean();
    }

    public static boolean travelersUpgradesEnabled() {
        return ModConfig.enableTravelersUpgrades;
    }

    public static boolean arcaneCraftingEnabled() {
        return ModConfig.enableArcaneCraftingUpgrade;
    }

}
