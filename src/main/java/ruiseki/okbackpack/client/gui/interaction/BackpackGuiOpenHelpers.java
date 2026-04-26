package ruiseki.okbackpack.client.gui.interaction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import baubles.common.container.InventoryBaubles;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;

public final class BackpackGuiOpenHelpers {

    private static final String PLAYER_INVENTORY_SLOT_GROUP = "player_inventory";

    private BackpackGuiOpenHelpers() {}

    public static boolean openFirstClient(EntityPlayer player, BackpackEntityHelpers.SearchOrder order) {
        if (player == null || player.capabilities.isCreativeMode) {
            return false;
        }

        return BackpackEntityHelpers.visitPlayerBackpacks(player, order, BackpackGuiOpenHelpers::openClient);
    }

    public static boolean openClient(EntityPlayer player, Slot slot) {
        return openClient(getSlotBackpackContext(player, slot));
    }

    public static boolean tryOpenClient(EntityPlayer player, Slot slot) {
        BackpackEntityHelpers.BackpackContext context = getSlotBackpackContext(player, slot);
        if (context == null || isCurrentOpenBackpack(player, context)) {
            return false;
        }
        return openClient(context);
    }

    public static boolean openClient(BackpackEntityHelpers.BackpackContext context) {
        if (context == null || context.getInventoryType() == null || context.getSlotIndex() < 0) {
            return false;
        }

        GuiFactories.playerInventory()
            .openClient(context.getInventoryType(), context.getSlotIndex());
        return true;
    }

    public static BackpackEntityHelpers.BackpackContext getSlotBackpackContext(EntityPlayer player, Slot slot) {
        if (player == null || slot == null
            || !slot.getHasStack()
            || !BackpackEntityHelpers.isBackpackStack(slot.getStack(), false)) {
            return null;
        }

        if (slot.inventory instanceof InventoryPlayer) {
            return BackpackEntityHelpers.getBackpack(player, InventoryTypes.PLAYER, slot.getSlotIndex());
        }

        if (slot.inventory instanceof InventoryBaubles) {
            return BackpackEntityHelpers.getBackpack(player, InventoryTypes.BAUBLES, slot.getSlotIndex());
        }

        if (slot instanceof ModularSlot modularSlot
            && PLAYER_INVENTORY_SLOT_GROUP.equals(modularSlot.getSlotGroupName())) {
            return BackpackEntityHelpers.getBackpack(player, InventoryTypes.PLAYER, modularSlot.getSlotIndex());
        }

        return null;
    }

    public static boolean isCurrentOpenBackpack(EntityPlayer player, BackpackEntityHelpers.BackpackContext context) {
        if (player == null || context == null || !(player.openContainer instanceof BackPackContainer container)) {
            return false;
        }

        return context.getInventoryType() == container.wrapper.getType()
            && context.getSlotIndex() == container.wrapper.getSlotIndex();
    }
}
