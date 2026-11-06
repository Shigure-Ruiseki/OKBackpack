
package ruiseki.okstoragecore.helper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.inventory.ItemStackKey;

public class RecipeHelpers {

    private static final int MAX_FOLLOW_UP_COMPACTING_RECIPES = 30;

    private static WeakReference<World> world = new WeakReference<>(null);

    private static final Map<CompactedItem, CompactingResult> COMPACTING_RESULTS = new HashMap<>();
    private static final Map<ItemStackKey, UncompactingResult> UNCOMPACTING_RESULTS = new HashMap<>();
    private static final Map<ItemStackKey, Set<CompactingShape>> ITEM_COMPACTING_SHAPES = new HashMap<>();

    private RecipeHelpers() {}

    public static void setWorld(World newWorld) {
        if (getWorld() != newWorld) clearCache();
        world = new WeakReference<>(newWorld);
    }

    private static World getWorld() {
        return world.get();
    }

    public static void clearCache() {
        COMPACTING_RESULTS.clear();
        UNCOMPACTING_RESULTS.clear();
        ITEM_COMPACTING_SHAPES.clear();
        ItemStackKey.clearCache();
    }

    public static Set<CompactingShape> getItemCompactingShapes(ItemStack stack) {
        if (ItemHelpers.isEmpty(stack) || getWorld() == null) {
            return Collections.emptySet();
        }

        ItemStackKey key = ItemStackKey.of(stack);
        if (key == null) {
            return Collections.emptySet();
        }

        Set<CompactingShape> cached = ITEM_COMPACTING_SHAPES.get(key);
        if (cached != null) {
            return cached;
        }

        Set<CompactingShape> result = Collections.unmodifiableSet(getCompactingShapes(stack));
        ITEM_COMPACTING_SHAPES.put(key, result);

        return result;
    }

    private static Set<CompactingShape> getCompactingShapes(ItemStack stack) {
        World w = getWorld();

        if (w == null || ItemHelpers.isEmpty(stack)) {
            return Collections.emptySet();
        }

        Set<CompactingShape> shapes = new HashSet<>();

        CompactingShape shape2x2 = getCompactingShape(
            stack,
            w,
            2,
            2,
            CompactingShape.TWO_BY_TWO_UNCRAFTABLE,
            CompactingShape.TWO_BY_TWO);

        CompactingShape shape3x3 = getCompactingShape(
            stack,
            w,
            3,
            3,
            CompactingShape.THREE_BY_THREE_UNCRAFTABLE,
            CompactingShape.THREE_BY_THREE);

        if (shape2x2 != null) {
            shapes.add(shape2x2);
        }

        if (shape3x3 != null) {
            shapes.add(shape3x3);
        }

        if (shapes.isEmpty()) {
            shapes.add(CompactingShape.NONE);
        }

        return shapes;
    }

    private static CompactingShape getCompactingShape(ItemStack input, World w, int width, int height,
        CompactingShape uncraftableShape, CompactingShape shape) {

        CompactingResult result = getCompactingResult(input, w, width, height);
        ItemStack output = result.getResult();

        if (ItemHelpers.isEmpty(output) || ItemHelpers.areItemsEqual(input, output)) {
            return null;
        }

        if (isPartOfCompactingLoop(input, output, w)) {
            return null;
        }

        if (uncompactMatchesItem(output, w, input, width * height)) {
            return uncraftableShape;
        }

        return shape;
    }

    private static boolean isPartOfCompactingLoop(ItemStack firstCompacted, ItemStack firstCompactResult, World w) {

        Set<ItemStackKey> visited = new HashSet<>();
        Queue<ItemStack> queue = new LinkedList<>();

        queue.add(firstCompactResult.copy());

        int iterations = 0;

        while (!queue.isEmpty()) {
            ItemStack current = queue.poll();

            if (ItemHelpers.isEmpty(current)) {
                continue;
            }

            ItemStackKey currentKey = ItemStackKey.of(current);

            if (currentKey == null || !visited.add(currentKey)) {
                continue;
            }

            if (++iterations > MAX_FOLLOW_UP_COMPACTING_RECIPES) {
                return true;
            }

            if (checkCompactingLoopForShape(firstCompacted, current, w, 2, 2, visited, queue)) {
                return true;
            }

            if (checkCompactingLoopForShape(firstCompacted, current, w, 3, 3, visited, queue)) {
                return true;
            }
        }

        return false;
    }

    private static boolean checkCompactingLoopForShape(ItemStack firstCompacted, ItemStack current, World w, int width,
        int height, Set<ItemStackKey> visited, Queue<ItemStack> queue) {

        CompactingResult result = getCompactingResult(current, w, width, height);
        ItemStack next = result.getResult();

        if (ItemHelpers.isEmpty(next)) {
            return false;
        }

        if (ItemHelpers.areItemsEqual(next, firstCompacted)) {
            return true;
        }

        ItemStackKey nextKey = ItemStackKey.of(next);

        if (nextKey != null && !visited.contains(nextKey)) {
            queue.add(next);
        }

        return false;
    }

    private static boolean uncompactMatchesItem(ItemStack compactedStack, World w, ItemStack expectedStack,
        int expectedCount) {

        if (ItemHelpers.isEmpty(compactedStack) || ItemHelpers.isEmpty(expectedStack)) {
            return false;
        }

        List<ItemStack> results = getUncompactResultItems(w, compactedStack);

        for (ItemStack stack : results) {
            if (ItemHelpers.isEmpty(stack)) {
                continue;
            }

            if (ItemHelpers.getCount(stack) == expectedCount && ItemHelpers.areItemsEqual(stack, expectedStack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compatibility overload.
     * Metadata and NBT cannot be specified through this overload.
     */
    public static UncompactingResult getUncompactingResult(Item resultItem) {
        return resultItem == null ? UncompactingResult.EMPTY : getUncompactingResult(new ItemStack(resultItem));
    }

    public static UncompactingResult getUncompactingResult(ItemStack resultStack) {
        if (ItemHelpers.isEmpty(resultStack)) {
            return UncompactingResult.EMPTY;
        }

        ItemStackKey key = ItemStackKey.of(resultStack);
        if (key == null) {
            return UncompactingResult.EMPTY;
        }

        UncompactingResult cached = UNCOMPACTING_RESULTS.get(key);
        if (cached != null) {
            return cached;
        }

        World w = getWorld();
        if (w == null) {
            return UncompactingResult.EMPTY;
        }

        UncompactingResult result = findUncompactingResult(resultStack, w);

        UNCOMPACTING_RESULTS.put(key, result);

        return result;
    }

    private static UncompactingResult findUncompactingResult(ItemStack resultStack, World w) {
        for (ItemStack uncompactResult : getUncompactResultItems(w, resultStack)) {
            if (ItemHelpers.isEmpty(uncompactResult)) {
                continue;
            }

            int count = ItemHelpers.getCount(uncompactResult);
            int width;
            int height;
            CompactingShape shape;

            if (count == 9) {
                width = 3;
                height = 3;
                shape = CompactingShape.THREE_BY_THREE_UNCRAFTABLE;
            } else if (count == 4) {
                width = 2;
                height = 2;
                shape = CompactingShape.TWO_BY_TWO_UNCRAFTABLE;
            } else {
                continue;
            }

            CompactingResult compacting = getCompactingResult(uncompactResult, w, width, height);
            ItemStack compacted = compacting.getResult();

            if (!ItemHelpers.isEmpty(compacted) && ItemHelpers.areItemsEqual(compacted, resultStack)) {
                return new UncompactingResult(uncompactResult, shape);
            }
        }

        return UncompactingResult.EMPTY;
    }

    private static List<ItemStack> getUncompactResultItems(World w, ItemStack itemToUncompact) {
        if (w == null || ItemHelpers.isEmpty(itemToUncompact)) {
            return Collections.emptyList();
        }

        InventoryCrafting inventory = getFilledCraftingInventory(itemToUncompact, 1, 1);
        List<IRecipe> recipes = getMatchingRecipes(inventory, w);

        List<ItemStack> results = new ArrayList<>();

        for (IRecipe recipe : recipes) {
            ItemStack result = getRecipeResult(recipe, inventory);

            if (!ItemHelpers.isEmpty(result)) {
                results.add(result.copy());
            }
        }

        return results;
    }

    public static CompactingResult getCompactingResult(Item item, CompactingShape shape) {
        return item == null ? CompactingResult.EMPTY : getCompactingResult(new ItemStack(item), shape);
    }

    public static CompactingResult getCompactingResult(ItemStack stack, CompactingShape shape) {
        if (shape == CompactingShape.TWO_BY_TWO || shape == CompactingShape.TWO_BY_TWO_UNCRAFTABLE) {
            return getCompactingResult(stack, 2, 2);
        }

        if (shape == CompactingShape.THREE_BY_THREE || shape == CompactingShape.THREE_BY_THREE_UNCRAFTABLE) {
            return getCompactingResult(stack, 3, 3);
        }

        return CompactingResult.EMPTY;
    }

    public static CompactingResult getCompactingResult(ItemStack stack, int width, int height) {
        World w = getWorld();

        if (w == null || ItemHelpers.isEmpty(stack) || !isValidGridSize(width, height)) {
            return CompactingResult.EMPTY;
        }

        return getCompactingResult(stack, w, width, height);
    }

    private static CompactingResult getCompactingResult(ItemStack stack, World w, int width, int height) {

        if (w == null || ItemHelpers.isEmpty(stack) || !isValidGridSize(width, height)) {
            return CompactingResult.EMPTY;
        }

        CompactedItem key = new CompactedItem(stack, width, height);

        CompactingResult cached = COMPACTING_RESULTS.get(key);
        if (cached != null) {
            return cached;
        }

        InventoryCrafting inventory = getFilledCraftingInventory(stack, width, height);
        List<IRecipe> recipes = getMatchingRecipes(inventory, w);

        if (recipes.isEmpty()) {
            COMPACTING_RESULTS.put(key, CompactingResult.EMPTY);
            return CompactingResult.EMPTY;
        }

        ItemStack selectedOutput = null;

        // Prefer a recipe whose output can be uncrafted into the original input.
        for (IRecipe recipe : recipes) {
            ItemStack output = getRecipeResult(recipe, inventory);

            if (ItemHelpers.isEmpty(output)) {
                continue;
            }

            if (selectedOutput == null) {
                selectedOutput = output.copy();
            }

            if (uncompactMatchesItem(output, w, stack, width * height)) {
                selectedOutput = output.copy();
                break;
            }
        }

        if (ItemHelpers.isEmpty(selectedOutput)) {
            COMPACTING_RESULTS.put(key, CompactingResult.EMPTY);
            return CompactingResult.EMPTY;
        }

        CompactingResult result = new CompactingResult(selectedOutput, Collections.<ItemStack>emptyList());

        COMPACTING_RESULTS.put(key, result);

        return result;
    }

    private static boolean isValidGridSize(int width, int height) {
        return (width == 2 && height == 2) || (width == 3 && height == 3);
    }

    private static List<IRecipe> getMatchingRecipes(InventoryCrafting inventory, World w) {
        List<IRecipe> matches = new ArrayList<>();

        List recipes = CraftingManager.getInstance()
            .getRecipeList();

        for (Object object : recipes) {
            if (!(object instanceof IRecipe)) {
                continue;
            }

            IRecipe recipe = (IRecipe) object;

            try {
                if (recipe.matches(inventory, w)) {
                    matches.add(recipe);
                }
            } catch (Throwable ignored) {
                // Ignore invalid third-party recipes.
            }
        }

        return matches;
    }

    private static ItemStack getRecipeResult(IRecipe recipe, InventoryCrafting inventory) {
        try {
            ItemStack result = recipe.getCraftingResult(inventory);
            return ItemHelpers.isEmpty(result) ? null : result.copy();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static InventoryCrafting getFilledCraftingInventory(ItemStack stack, int width, int height) {

        InventoryCrafting inventory = new InventoryCrafting(new DummyContainer(), width, height);

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack copy = stack.copy();
            ItemHelpers.setCount(copy, 1);
            inventory.setInventorySlotContents(i, copy);
        }

        return inventory;
    }

    public enum CompactingShape {

        NONE(false, 0),
        THREE_BY_THREE(false, 9),
        TWO_BY_TWO(false, 4),
        THREE_BY_THREE_UNCRAFTABLE(true, 9),
        TWO_BY_TWO_UNCRAFTABLE(true, 4);

        private final int numberOfIngredients;
        private final boolean uncraftable;

        CompactingShape(boolean uncraftable, int numberOfIngredients) {
            this.uncraftable = uncraftable;
            this.numberOfIngredients = numberOfIngredients;
        }

        public boolean isUncraftable() {
            return uncraftable;
        }

        public int getNumberOfIngredients() {
            return numberOfIngredients;
        }
    }

    public static class CompactingResult {

        public static final CompactingResult EMPTY = new CompactingResult(null, Collections.<ItemStack>emptyList());

        private final ItemStack result;
        private final List<ItemStack> remainingItems;

        public CompactingResult(ItemStack result, List<ItemStack> remainingItems) {
            this.result = result == null ? null : result.copy();
            this.remainingItems = copyStacks(remainingItems);
        }

        public ItemStack getResult() {
            return result == null ? null : result.copy();
        }

        public List<ItemStack> getRemainingItems() {
            return copyStacks(remainingItems);
        }

        private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
            if (stacks == null || stacks.isEmpty()) {
                return Collections.emptyList();
            }

            List<ItemStack> copies = new ArrayList<>();

            for (ItemStack stack : stacks) {
                copies.add(stack == null ? null : stack.copy());
            }

            return Collections.unmodifiableList(copies);
        }
    }

    public static class UncompactingResult {

        public static final UncompactingResult EMPTY = new UncompactingResult(null, CompactingShape.NONE);

        private final ItemStack result;
        private final CompactingShape compactUsingShape;

        public UncompactingResult(ItemStack result, CompactingShape compactUsingShape) {
            this.result = result == null ? null : result.copy();
            this.compactUsingShape = compactUsingShape;
        }

        /**
         * Compatibility getter. Returns only the Item, without metadata or NBT.
         */
        public Item getResult() {
            return result == null ? null : result.getItem();
        }

        /**
         * Returns the complete result stack, including metadata and NBT.
         */
        public ItemStack getResultStack() {
            return result == null ? null : result.copy();
        }

        public CompactingShape getCompactUsingShape() {
            return compactUsingShape;
        }
    }

    private static class CompactedItem {

        private final ItemStackKey stackKey;
        private final int width;
        private final int height;

        private CompactedItem(ItemStack stack, int width, int height) {
            this.stackKey = ItemStackKey.of(stack);
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }

            if (!(object instanceof CompactedItem)) {
                return false;
            }

            CompactedItem other = (CompactedItem) object;

            return width == other.width && height == other.height
                && (stackKey == null ? other.stackKey == null : stackKey.equals(other.stackKey));
        }

        @Override
        public int hashCode() {
            int result = stackKey == null ? 0 : stackKey.hashCode();
            result = 31 * result + width;
            result = 31 * result + height;
            return result;
        }
    }

    private static class DummyContainer extends Container {

        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return false;
        }

        @Override
        public void onCraftMatrixChanged(net.minecraft.inventory.IInventory inventory) {
            // No-op.
        }
    }
}
