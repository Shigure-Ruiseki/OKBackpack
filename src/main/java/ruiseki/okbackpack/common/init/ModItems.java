package ruiseki.okbackpack.common.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Level;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.item.ItemAdvancedCompactingUpgrade;
import ruiseki.okbackpack.common.item.ItemAdvancedFeedingUpgrade;
import ruiseki.okbackpack.common.item.ItemAdvancedFilterUpgrade;
import ruiseki.okbackpack.common.item.ItemAdvancedMagnetUpgrade;
import ruiseki.okbackpack.common.item.ItemAdvancedPickupUpgrade;
import ruiseki.okbackpack.common.item.ItemAdvancedVoidUpgrade;
import ruiseki.okbackpack.common.item.ItemCompactingUpgrade;
import ruiseki.okbackpack.common.item.ItemCraftingUpgrade;
import ruiseki.okbackpack.common.item.ItemEverlastingUpgrade;
import ruiseki.okbackpack.common.item.ItemFeedingUpgrade;
import ruiseki.okbackpack.common.item.ItemFilterUpgrade;
import ruiseki.okbackpack.common.item.ItemInceptionUpgrade;
import ruiseki.okbackpack.common.item.ItemMagnetUpgrade;
import ruiseki.okbackpack.common.item.ItemPickupUpgrade;
import ruiseki.okbackpack.common.item.ItemStackUpgrade;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.common.item.ItemVoidUpgrade;
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

    //
    ;
    // spotless: on

    public static final ModItems[] VALUES = values();

    public static void preInit() {
        for (ModItems item : VALUES) {
            try {
                item.item.init();
                OKBackpack.okLog(Level.INFO, "Successfully initialized " + item.name());
            } catch (Exception e) {
                OKBackpack.okLog(Level.ERROR, "Failed to initialize item: +" + item.name());
            }
        }
    }

    private final IItem item;

    ModItems(IItem item) {
        this.item = item;
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
