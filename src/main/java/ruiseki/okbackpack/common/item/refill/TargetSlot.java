package ruiseki.okbackpack.common.item.refill;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import lombok.Getter;

public enum TargetSlot {

    MAIN_HAND("main_hand", "gui.backpack.refill.target.main_hand.acronym", "gui.backpack.refill.target.main_hand",
        (player,
            filter) -> getMissingCountForSlot(player.inventory.mainInventory[player.inventory.currentItem], filter),
        (player, stack) -> fillSlot(player, player.inventory.currentItem, stack)),

    ANY("any", "gui.backpack.refill.target.any.acronym", "gui.backpack.refill.target.any",
        TargetSlot::getMissingCountAnywhere, TargetSlot::fillAnywhere),

    HOTBAR_1("hotbar_1", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[0], filter),
        (player, stack) -> fillSlot(player, 0, stack)),
    HOTBAR_2("hotbar_2", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[1], filter),
        (player, stack) -> fillSlot(player, 1, stack)),
    HOTBAR_3("hotbar_3", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[2], filter),
        (player, stack) -> fillSlot(player, 2, stack)),
    HOTBAR_4("hotbar_4", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[3], filter),
        (player, stack) -> fillSlot(player, 3, stack)),
    HOTBAR_5("hotbar_5", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[4], filter),
        (player, stack) -> fillSlot(player, 4, stack)),
    HOTBAR_6("hotbar_6", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[5], filter),
        (player, stack) -> fillSlot(player, 5, stack)),
    HOTBAR_7("hotbar_7", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[6], filter),
        (player, stack) -> fillSlot(player, 6, stack)),
    HOTBAR_8("hotbar_8", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[7], filter),
        (player, stack) -> fillSlot(player, 7, stack)),
    HOTBAR_9("hotbar_9", null, "gui.backpack.refill.target.hotbar",
        (player, filter) -> getMissingCountForSlot(player.inventory.mainInventory[8], filter),
        (player, stack) -> fillSlot(player, 8, stack));

    private static final TargetSlot[] VALUES = values();
    private static final Map<String, TargetSlot> NAME_MAP = new HashMap<>();

    static {
        for (TargetSlot slot : VALUES) {
            NAME_MAP.put(slot.name, slot);
        }
    }

    private final String name;
    @Getter
    private final String acronymKey;
    @Getter
    private final String tooltipKey;
    private final BiFunction<EntityPlayer, ItemStack, Integer> missingCountGetter;
    private final BiFunction<EntityPlayer, ItemStack, ItemStack> filler;

    TargetSlot(String name, String acronymKey, String tooltipKey,
        BiFunction<EntityPlayer, ItemStack, Integer> missingCountGetter,
        BiFunction<EntityPlayer, ItemStack, ItemStack> filler) {
        this.name = name;
        this.acronymKey = acronymKey;
        this.tooltipKey = tooltipKey;
        this.missingCountGetter = missingCountGetter;
        this.filler = filler;
    }

    public String getSerializedName() {
        return name;
    }

    public int getHotbarIndex() {
        return ordinal() - 2; // HOTBAR_1 is at ordinal 2, maps to 0
    }

    public int getMissingCount(EntityPlayer player, ItemStack filter) {
        return missingCountGetter.apply(player, filter);
    }

    /**
     * Fill the target slot(s) with the given stack.
     *
     * @return remaining items that weren't placed
     */
    public ItemStack fill(EntityPlayer player, ItemStack stackToAdd) {
        return filler.apply(player, stackToAdd);
    }

    public TargetSlot next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public TargetSlot previous() {
        return VALUES[Math.floorMod(ordinal() - 1, VALUES.length)];
    }

    public static TargetSlot fromName(String name) {
        return NAME_MAP.getOrDefault(name, ANY);
    }

    public static TargetSlot fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) return ANY;
        return VALUES[ordinal];
    }

    private static int getMissingCountForSlot(ItemStack slotStack, ItemStack filter) {
        if (slotStack == null) {
            return filter.getMaxStackSize();
        }
        return 0;
    }

    private static int getMissingCountAnywhere(EntityPlayer player, ItemStack filter) {
        InventoryPlayer inv = player.inventory;
        for (int i = 0; i < inv.mainInventory.length; i++) {
            if (inv.mainInventory[i] == null) {
                return filter.getMaxStackSize();
            }
        }
        return 0;
    }

    private static ItemStack fillSlot(EntityPlayer player, int slotIndex, ItemStack stackToAdd) {
        InventoryPlayer inv = player.inventory;
        if (inv.mainInventory[slotIndex] == null) {
            inv.mainInventory[slotIndex] = stackToAdd.copy();
            return null;
        }
        return stackToAdd;
    }

    private static ItemStack fillAnywhere(EntityPlayer player, ItemStack stackToAdd) {
        InventoryPlayer inv = player.inventory;
        for (int i = 0; i < inv.mainInventory.length; i++) {
            if (inv.mainInventory[i] == null) {
                inv.mainInventory[i] = stackToAdd.copy();
                return null;
            }
        }
        return stackToAdd;
    }
}
