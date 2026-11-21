package ruiseki.okstoragecore.client.gui.slot;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

public class CraftingSlotInfo {

    private final ModularSlot[] craftingMatrixSlots;
    private final ModularCraftingSlot craftingOutputSlot;

    // NEI recipe display coordinates
    private final int recipeX;
    private final int recipeY;
    private final int recipeSpacingX;
    private final int recipeSpacingY;

    private final int slotIndex;
    private final int columns;
    private final int rows;

    public CraftingSlotInfo(int slotIndex, ModularSlot[] craftingMatrixSlots, ModularCraftingSlot craftingOutputSlot,
        int columns, int rows, int recipeX, int recipeY, int recipeSpacingX, int recipeSpacingY) {
        this.slotIndex = slotIndex;
        this.craftingMatrixSlots = craftingMatrixSlots;
        this.craftingOutputSlot = craftingOutputSlot;
        this.columns = columns;
        this.rows = rows;
        this.recipeX = recipeX;
        this.recipeY = recipeY;
        this.recipeSpacingX = recipeSpacingX;
        this.recipeSpacingY = recipeSpacingY;
    }

    public ModularCraftingSlot getCraftingOutputSlot() {
        return craftingOutputSlot;
    }

    public ModularSlot[] getCraftingMatrixSlots() {
        return craftingMatrixSlots;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public int getRecipeX() {
        return recipeX;
    }

    public int getRecipeY() {
        return recipeY;
    }

    public int getRecipeSpacingX() {
        return recipeSpacingX;
    }

    public int getRecipeSpacingY() {
        return recipeSpacingY;
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}
