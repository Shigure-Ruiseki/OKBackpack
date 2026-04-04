package ruiseki.okbackpack.common.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.github.bsideup.jabel.Desugar;

/**
 * Caches compacting recipes (2x2 and 3x3 grids filled with a single item type)
 * and optionally checks reversibility (output can be uncrafted back to input).
 */
public class CompactingRecipeCache {

    private static CompactingRecipeCache instance;

    private boolean initialized = false;

    // Key: registry name of input item (Item + meta), Value: CompactingResult
    private final Map<String, CompactingResult> cache2x2 = new HashMap<>();
    private final Map<String, CompactingResult> cache3x3 = new HashMap<>();

    // Reverse map: output item key -> input item key (for reversibility checks)
    private final Map<String, String> reverseMap2x2 = new HashMap<>();
    private final Map<String, String> reverseMap3x3 = new HashMap<>();

    public static CompactingRecipeCache getInstance() {
        if (instance == null) {
            instance = new CompactingRecipeCache();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public void initialize(World world) {
        if (initialized) return;
        initialized = true;

        List<IRecipe> recipes = CraftingManager.getInstance()
            .getRecipeList();

        for (IRecipe recipe : recipes) {
            tryCache(recipe, world);
        }

        // Build reverse maps for reversibility check
        for (Map.Entry<String, CompactingResult> entry : cache2x2.entrySet()) {
            String outputKey = makeKey(entry.getValue().output);
            reverseMap2x2.put(outputKey, entry.getKey());
        }
        for (Map.Entry<String, CompactingResult> entry : cache3x3.entrySet()) {
            String outputKey = makeKey(entry.getValue().output);
            reverseMap3x3.put(outputKey, entry.getKey());
        }
    }

    private void tryCache(IRecipe recipe, World world) {
        // Check if it's a shaped recipe with uniform input in a 2x2 or 3x3 grid
        int width = -1;
        int height = -1;
        ItemStack[] inputs = null;

        if (recipe instanceof ShapedRecipes shaped) {
            width = shaped.recipeWidth;
            height = shaped.recipeHeight;
            inputs = shaped.recipeItems;
        } else if (recipe instanceof ShapedOreRecipe ore) {
            width = ore.width;
            height = ore.height;
            Object[] oreInputs = ore.getInput();
            inputs = resolveOreInputs(oreInputs);
        }

        if (inputs == null || width < 0 || height < 0) return;

        // Check for 2x2 or 3x3 uniform grid
        if (width == 2 && height == 2 && inputs.length >= 4) {
            tryCacheUniform(recipe, inputs, 4, cache2x2, world);
        } else if (width == 3 && height == 3 && inputs.length >= 9) {
            tryCacheUniform(recipe, inputs, 9, cache3x3, world);
        }
    }

    private void tryCacheUniform(IRecipe recipe, ItemStack[] inputs, int gridSize, Map<String, CompactingResult> cache,
        World world) {
        // All input slots must be the same non-null item
        ItemStack first = inputs[0];
        if (first == null) return;

        for (int i = 1; i < gridSize; i++) {
            if (inputs[i] == null) return;
            if (!inputs[i].isItemEqual(first)) return;
        }

        // Verify recipe produces a valid output
        ItemStack output = getRecipeOutput(recipe, first, gridSize, world);
        if (output == null) return;

        // Don't cache if output is same as input
        if (output.isItemEqual(first)) return;

        String key = makeKey(first);

        // Only cache the first found recipe per input item (deterministic)
        if (!cache.containsKey(key)) {
            cache.put(key, new CompactingResult(output.copy(), gridSize));
        }
    }

    private ItemStack getRecipeOutput(IRecipe recipe, ItemStack input, int gridSize, World world) {
        int dim = gridSize == 4 ? 2 : 3;
        InventoryCrafting crafting = new InventoryCrafting(new DummyContainer(), dim, dim);
        for (int i = 0; i < gridSize; i++) {
            ItemStack copy = input.copy();
            copy.stackSize = 1;
            crafting.setInventorySlotContents(i, copy);
        }

        if (recipe.matches(crafting, world)) {
            return recipe.getCraftingResult(crafting);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private ItemStack[] resolveOreInputs(Object[] oreInputs) {
        if (oreInputs == null) return null;
        ItemStack[] resolved = new ItemStack[oreInputs.length];
        for (int i = 0; i < oreInputs.length; i++) {
            Object input = oreInputs[i];
            if (input instanceof ItemStack stack) {
                resolved[i] = stack;
            } else if (input instanceof List) {
                List<ItemStack> list = (List<ItemStack>) input;
                if (list.isEmpty()) return null;
                resolved[i] = list.get(0);
            } else {
                return null;
            }
        }
        return resolved;
    }

    /**
     * Find a compacting result for the given item.
     *
     * @param stack          The item to compact
     * @param allow3x3       Whether to also check 3x3 recipes
     * @param onlyReversible If true, only return results where decomposition recipe exists
     * @return The compacting result, or null if none found
     */
    public CompactingResult findCompactingRecipe(ItemStack stack, boolean allow3x3, boolean onlyReversible) {
        if (stack == null) return null;

        String key = makeKey(stack);

        // Prefer 3x3 (higher compression) over 2x2
        if (allow3x3) {
            CompactingResult result3x3 = cache3x3.get(key);
            if (result3x3 != null) {
                if (!onlyReversible || isReversible(result3x3.output, 9)) {
                    return result3x3;
                }
            }
        }

        CompactingResult result2x2 = cache2x2.get(key);
        if (result2x2 != null) {
            if (!onlyReversible || isReversible(result2x2.output, 4)) {
                return result2x2;
            }
        }

        return null;
    }

    /**
     * Check if an output item can be decomposed back to its inputs.
     * This means: the output item has a recipe where it produces gridSize of some item.
     */
    private boolean isReversible(ItemStack output, int gridSize) {
        String outputKey = makeKey(output);

        // Check if any recipe uses this output as input in the other direction
        // We look for a recipe where 1x output -> gridSize x input
        // This is done by checking if the output itself appears as a compacting input
        // OR by checking all recipes for a decomposition pattern

        // Simple check: does a reverse mapping exist?
        if (gridSize == 4) {
            return reverseMap2x2.containsKey(outputKey);
        } else if (gridSize == 9) {
            return reverseMap3x3.containsKey(outputKey);
        }
        return false;
    }

    public void invalidate() {
        initialized = false;
        cache2x2.clear();
        cache3x3.clear();
        reverseMap2x2.clear();
        reverseMap3x3.clear();
    }

    private static String makeKey(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "";
        return stack.getItem()
            .getUnlocalizedName() + "@"
            + stack.getItemDamage();
    }

    /**
     * @param inputCount 4 for 2x2, 9 for 3x3
     */
    @Desugar
    public record CompactingResult(ItemStack output, int inputCount) {}

    /**
     * Minimal container needed for InventoryCrafting construction.
     */
    private static class DummyContainer extends net.minecraft.inventory.Container {

        @Override
        public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer player) {
            return false;
        }

        @Override
        public void onCraftMatrixChanged(net.minecraft.inventory.IInventory inventory) {
            // no-op
        }
    }
}
