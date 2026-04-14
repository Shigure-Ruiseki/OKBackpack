package ruiseki.okbackpack.compat.nei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.client.gui.slot.CraftingSlotInfo;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingSlot;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeSlot;
import ruiseki.okbackpack.common.block.BackpackPanel;

public class BackpackOverlay extends DefaultOverlayHandler {

    private static final int ARCANE_X0 = 47;
    private static final int ARCANE_Y0 = 38;
    private static final int ARCANE_DX = 28;
    private static final int ARCANE_DY = 27;

    private static final int CRAFTING_X0 = 25;
    private static final int CRAFTING_Y0 = 6;
    private static final int CRAFTING_SLOT_SIZE = 18;

    public BackpackOverlay() {
        super(0, 0);
    }

    @Override
    public boolean canMoveFrom(Slot slot, GuiContainer gui) {
        return !(slot instanceof IndexedModularCraftingSlot) && !(slot instanceof ModularFilterSlot)
            && !(slot instanceof ModularUpgradeSlot);
    }

    @Override
    protected Set<Slot> getCraftMatrixSlots(GuiContainer gui, IRecipeHandler handler) {

        final Set<Slot> slots = new HashSet<>();

        if (!(gui.inventorySlots instanceof BackPackContainer container)) {
            return slots;
        }

        BackpackPanel panel = getPanel(container);
        if (panel == null) {
            return slots;
        }

        int craftingUpgradeSlot = panel.getOpenCraftingUpgradeSlot();
        if (craftingUpgradeSlot < 0) {
            return slots;
        }

        ICraftingUpgrade wrapper = panel.getOpenCraftingUpgradeWrapper();
        if (wrapper == null) {
            return slots;
        }

        CraftingSlotInfo info = panel.getCraftingInfo(craftingUpgradeSlot, wrapper.getCraftingInfoKey());
        if (info == null) {
            return slots;
        }

        slots.addAll(Arrays.asList(info.getCraftingMatrixSlots()));

        return slots;
    }

    @Override
    public Slot[][] mapIngredSlots(GuiContainer gui, List<PositionedStack> ingredients) {

        Slot[][] recipeSlotList = new Slot[ingredients.size()][];

        for (int i = 0; i < ingredients.size(); i++) {
            recipeSlotList[i] = new Slot[0];
        }

        if (!(gui.inventorySlots instanceof BackPackContainer container)) {
            return recipeSlotList;
        }

        BackpackPanel panel = getPanel(container);
        if (panel == null) {
            return recipeSlotList;
        }

        int craftingUpgradeSlot = panel.getOpenCraftingUpgradeSlot();
        if (craftingUpgradeSlot < 0) {
            return recipeSlotList;
        }

        ICraftingUpgrade wrapper = panel.getOpenCraftingUpgradeWrapper();
        if (wrapper == null) {
            return recipeSlotList;
        }

        CraftingSlotInfo info = panel.getCraftingInfo(craftingUpgradeSlot, wrapper.getCraftingInfoKey());
        if (info == null) {
            return recipeSlotList;
        }

        ModularSlot[] matrix = info.getCraftingMatrixSlots();

        for (int i = 0; i < ingredients.size(); i++) {

            PositionedStack ps = ingredients.get(i);
            if (ps == null) continue;

            int index = getSlotIndex(ps);

            if (index >= 0 && index < matrix.length) {
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

        if (!(firstGui.inventorySlots instanceof BackPackContainer container)) {
            return itemPresenceSlots;
        }

        EntityPlayer player = container.getPlayer();

        final List<ItemStack> invStacks = new ArrayList<>();

        // backpack inventory
        for (int i = 0; i < container.wrapper.getSlots(); i++) {
            ItemStack stack = container.wrapper.getStackInSlot(i);
            if (stack != null && stack.stackSize > 0) {
                invStacks.add(stack.copy());
            }
        }

        // player inventory
        if (player != null) {
            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack != null && stack.stackSize > 0) {
                    invStacks.add(stack.copy());
                }
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

    private static int getSlotIndex(PositionedStack ps) {
        int col, row;
        if ((ps.relx - ARCANE_X0) % ARCANE_DX == 0 && (ps.rely - ARCANE_Y0) % ARCANE_DY == 0) {
            col = (ps.relx - ARCANE_X0) / ARCANE_DX;
            row = (ps.rely - ARCANE_Y0) / ARCANE_DY;
        } else {
            col = (ps.relx - CRAFTING_X0) / CRAFTING_SLOT_SIZE;
            row = (ps.rely - CRAFTING_Y0) / CRAFTING_SLOT_SIZE;
        }
        return row * 3 + col;
    }

    private BackpackPanel getPanel(BackPackContainer container) {
        ModularScreen screen = container.getScreen();
        if (!container.isInitialized() || !(screen.getPanelManager()
            .getMainPanel() instanceof BackpackPanel)) return null;
        return (BackpackPanel) screen.getPanelManager()
            .getMainPanel();
    }
}
