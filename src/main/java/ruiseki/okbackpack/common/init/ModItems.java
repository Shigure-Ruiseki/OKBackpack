package ruiseki.okbackpack.common.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Level;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.common.item.anvil.ItemAnvilUpgrade;
import ruiseki.okbackpack.common.item.compacting.ItemAdvancedCompactingUpgrade;
import ruiseki.okbackpack.common.item.compacting.ItemCompactingUpgrade;
import ruiseki.okbackpack.common.item.crafting.ItemCraftingUpgrade;
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
import ruiseki.okbackpack.common.item.smelter.ItemAutoBlastingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemAutoSmeltingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemAutoSmokingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemBlastingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemSmeltingUpgrade;
import ruiseki.okbackpack.common.item.smelter.ItemSmokingUpgrade;
import ruiseki.okbackpack.common.item.stack.ItemStackUpgrade;
import ruiseki.okbackpack.common.item.toolswapper.ItemAdvancedToolSwapperUpgrade;
import ruiseki.okbackpack.common.item.toolswapper.ItemToolSwapperUpgrade;
import ruiseki.okbackpack.common.item.voiding.ItemAdvancedVoidUpgrade;
import ruiseki.okbackpack.common.item.voiding.ItemVoidUpgrade;
import ruiseki.okbackpack.compat.Mods;
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
    ANVIL_UPGRADE(new ItemAnvilUpgrade()),
    INFINITY_UPGRADE(new ItemInfinityUpgrade()),
    SURVIVAL_INFINITY_UPGRADE(new ItemSurvivalInfinityUpgrade()),

    //
    ;
    // spotless: on

    public static final ModItems[] VALUES = values();

    public static void preInit() {
        for (ModItems item : VALUES) {
            try {
                if (item.requiredMod != null && !item.requiredMod.isLoaded()) {
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

    ModItems(IItem item) {
        this(item, null);
    }

    ModItems(IItem item, Mods requiredMod) {
        this.item = item;
        this.requiredMod = requiredMod;
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

}
