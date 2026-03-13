package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;

public class FilterSlot extends ItemSlot implements RecipeViewerGhostIngredientSlot<ItemStack> {

    private FilterSlotSH syncHandler;

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castOrThrow(FilterSlotSH.class);
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        if (!areAncestorsEnabled() || !this.syncHandler.isItemValid(draggedStack)) return false;
        draggedStack.stackSize = 0;
        this.syncHandler.updateFromClient(draggedStack);
        return true;
    }
}
