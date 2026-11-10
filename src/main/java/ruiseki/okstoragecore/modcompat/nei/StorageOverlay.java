package ruiseki.okstoragecore.modcompat.nei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.slot.CraftingSlotInfo;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularStorageSlot;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class StorageOverlay extends DefaultOverlayHandler {

    public StorageOverlay() {
        super(0, 0);
    }

    @Override
    public boolean canMoveFrom(Slot slot, GuiContainer gui) {
        if (!(slot instanceof ModularSlot modularSlot)) return false;
        if (slot instanceof IndexedModularSlot upgradeSlot) {
            CraftingSlotInfo info = this.getCraftingInfo(gui);
            if (info == null) return false;
            return upgradeSlot.getUpgradeIndex() == info.getSlotIndex();
        }
        if (slot instanceof ModularStorageSlot) return true;
        if (StorageContainer.PLAYER_INV.equals(modularSlot.getSlotGroupName())) return true;
        return false;
    }

    @Override
    protected Set<Slot> getCraftMatrixSlots(GuiContainer gui, IRecipeHandler handler) {
        final Set<Slot> slots = new HashSet<>();
        CraftingSlotInfo info = this.getCraftingInfo(gui);
        if (info == null) return slots;
        slots.addAll(Arrays.asList(info.getCraftingMatrixSlots()));
        return slots;
    }

    protected CraftingSlotInfo getCraftingInfo(GuiContainer gui) {
        if (!(gui.inventorySlots instanceof StorageContainer container)) return null;
        StoragePanel panel = container.getStoragePanel();
        if (panel == null) return null;
        return panel.getActiveUpgradeSlotUpdate()
            .map(UpgradeSlotUpdate::getCraftingInfo)
            .orElse(null);
    }

    @Override
    public Slot[][] mapIngredSlots(GuiContainer gui, List<PositionedStack> ingredients) {

        Slot[][] recipeSlotList = new Slot[ingredients.size()][];

        for (int i = 0; i < ingredients.size(); i++) {
            recipeSlotList[i] = new Slot[0];
        }

        CraftingSlotInfo info = this.getCraftingInfo(gui);
        if (info == null) return recipeSlotList;

        ModularSlot[] matrix = info.getCraftingMatrixSlots();
        for (int i = 0; i < ingredients.size(); i++) {

            PositionedStack ps = ingredients.get(i);
            if (ps == null) continue;

            int index = getSlotIndex(ps, info);
            if (index >= 0 && index < matrix.length && matrix[index] != null) {
                recipeSlotList[i] = new Slot[] { matrix[index] };
            }
        }

        return recipeSlotList;
    }

    @Override
    public List<GuiOverlayButton.ItemOverlayState> presenceOverlay(GuiContainer firstGui, IRecipeHandler recipe,
        int recipeIndex) {

        final List<GuiOverlayButton.ItemOverlayState> itemPresenceSlots = new ArrayList<>();
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);

        if (!(firstGui.inventorySlots instanceof StorageContainer container)) {
            return itemPresenceSlots;
        }

        EntityPlayer player = container.getPlayer();

        final List<ItemStack> invStacks = new ArrayList<>();

        // backpack inventory
        for (int i = 0; i < container.getStorageWrapper()
            .getInventoryHandler()
            .getSlots(); i++) {
            addPresenceStack(
                invStacks,
                container.getStorageWrapper()
                    .getInventoryHandler()
                    .getStackInSlot(i));
        }

        // inner inventories of storage upgrades, which includes crafting and arcane grids
        for (IUpgradeWrapper upgradeWrapper : container.getStorageWrapper()
            .getUpgradeHandler()
            .getSlotWrappers()
            .values()) {
            if (upgradeWrapper instanceof IStorageUpgrade storageUpgrade) {
                addStoragePresenceStacks(invStacks, storageUpgrade.getStorage());
            }
        }

        // player inventory
        if (player != null) {
            for (ItemStack stack : player.inventory.mainInventory) {
                addPresenceStack(invStacks, stack);
            }
        }

        for (PositionedStack stack : ingredients) {

            ItemStack used = null;
            for (ItemStack is : invStacks) {
                if (is.stackSize > 0 && stack.contains(is)) {
                    used = is;
                    break;
                }
            }

            itemPresenceSlots.add(new GuiOverlayButton.ItemOverlayState(stack, used != null));
            if (used != null) {
                used.stackSize -= 1;
            }
        }

        return itemPresenceSlots;
    }

    private static void addPresenceStack(List<ItemStack> target, ItemStack stack) {
        if (stack != null && stack.stackSize > 0) {
            target.add(stack.copy());
        }
    }

    private static void addStoragePresenceStacks(List<ItemStack> target, IItemHandlerModifiable storage) {
        if (storage == null) return;
        for (int slot = 0; slot < storage.getSlots(); slot++) {
            addPresenceStack(target, storage.getStackInSlot(slot));
        }
    }

    private static int getSlotIndex(PositionedStack ps, CraftingSlotInfo info) {
        int dx = ps.relx - info.getRecipeX();
        int dy = ps.rely - info.getRecipeY();

        int spacingX = info.getRecipeSpacingX();
        int spacingY = info.getRecipeSpacingY();

        if (dx < 0 || dy < 0 || spacingX <= 0 || spacingY <= 0) {
            return -1;
        }

        if (dx % spacingX != 0 || dy % spacingY != 0) {
            return -1;
        }

        int col = dx / spacingX;
        int row = dy / spacingY;

        if (col >= info.getColumns() || row >= info.getRows()) {
            return -1;
        }

        return row * info.getColumns() + col;
    }
}
