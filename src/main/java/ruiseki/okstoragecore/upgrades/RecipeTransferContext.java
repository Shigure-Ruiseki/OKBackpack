package ruiseki.okstoragecore.upgrades;

import java.util.List;

import net.minecraft.item.ItemStack;

public class RecipeTransferContext {

    private final int rows;
    private final int columns;
    private final List<RecipeIngredient> pattern;

    public RecipeTransferContext(int rows, int columns, List<RecipeIngredient> pattern) {
        this.rows = rows;
        this.columns = columns;
        this.pattern = pattern;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public List<RecipeIngredient> getPattern() {
        return pattern;
    }

    public static class RecipeIngredient {

        private final int slot;
        private final List<ItemStack> alternatives;
        private final int amount;

        public RecipeIngredient(int slot, List<ItemStack> alternatives, int amount) {
            this.slot = slot;
            this.alternatives = alternatives;
            this.amount = amount;
        }

        public int getSlot() {
            return slot;
        }

        public List<ItemStack> getAlternatives() {
            return alternatives;
        }

        public int getAmount() {
            return amount;
        }
    }
}
