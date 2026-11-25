package ruiseki.okbackpack.client.gui.interaction;

import net.minecraft.item.ItemStack;

public final class BackpackInventoryInteractionResult {

    public static final BackpackInventoryInteractionResult NONE = new BackpackInventoryInteractionResult(
        BackpackInventoryInteractionKind.NONE,
        null,
        null,
        false,
        false,
        0,
        0,
        null);

    private final BackpackInventoryInteractionKind kind;
    private final OverlaySymbol overlaySymbol;
    private final OverlayState overlayState;
    private final boolean outputSlot;
    private final boolean fullFitRequired;
    private final int sourceCount;
    private final int insertedCount;
    private final ItemStack backpackStack;

    private BackpackInventoryInteractionResult(BackpackInventoryInteractionKind kind, OverlaySymbol overlaySymbol,
        OverlayState overlayState, boolean outputSlot, boolean fullFitRequired, int sourceCount, int insertedCount,
        ItemStack backpackStack) {
        this.kind = kind;
        this.overlaySymbol = overlaySymbol;
        this.overlayState = overlayState;
        this.outputSlot = outputSlot;
        this.fullFitRequired = fullFitRequired;
        this.sourceCount = sourceCount;
        this.insertedCount = insertedCount;
        this.backpackStack = backpackStack;
    }

    public static BackpackInventoryInteractionResult of(BackpackInventoryInteractionKind kind,
        OverlaySymbol overlaySymbol, OverlayState overlayState, boolean outputSlot, boolean fullFitRequired,
        int sourceCount, int insertedCount, ItemStack backpackStack) {
        if (kind == BackpackInventoryInteractionKind.NONE || overlaySymbol == null
            || overlayState == null
            || insertedCount <= 0
            || sourceCount <= 0) {
            return NONE;
        }
        return new BackpackInventoryInteractionResult(
            kind,
            overlaySymbol,
            overlayState,
            outputSlot,
            fullFitRequired,
            sourceCount,
            insertedCount,
            backpackStack);
    }

    public static BackpackInventoryInteractionResult forOutputInsert(int sourceCount, int insertedCount,
        boolean hasMatchingStackSpace) {
        return of(
            BackpackInventoryInteractionKind.INSERT_SLOT_INTO_CARRIED_BACKPACK,
            OverlaySymbol.MINUS,
            hasMatchingStackSpace ? OverlayState.GREEN : OverlayState.YELLOW,
            true,
            true,
            sourceCount,
            insertedCount,
            null);
    }

    public static BackpackInventoryInteractionResult forNormalInsert(int sourceCount, int insertedCount,
        boolean hasMatchingStackSpace) {
        return of(
            BackpackInventoryInteractionKind.INSERT_SLOT_INTO_CARRIED_BACKPACK,
            OverlaySymbol.MINUS,
            hasMatchingStackSpace ? OverlayState.GREEN : OverlayState.YELLOW,
            false,
            false,
            sourceCount,
            insertedCount,
            null);
    }

    public BackpackInventoryInteractionKind getKind() {
        return kind;
    }

    public OverlaySymbol getOverlaySymbol() {
        return overlaySymbol;
    }

    public OverlayState getOverlayState() {
        return overlayState;
    }

    public boolean isOutputSlot() {
        return outputSlot;
    }

    public boolean isFullFitRequired() {
        return fullFitRequired;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public int getInsertedCount() {
        return insertedCount;
    }

    public int getRemainingCount() {
        return Math.max(0, sourceCount - insertedCount);
    }

    public ItemStack getBackpackStack() {
        return backpackStack;
    }

    public boolean isInteractable() {
        return kind != BackpackInventoryInteractionKind.NONE;
    }

    public boolean canRenderOverlay() {
        if (!isInteractable()) {
            return false;
        }
        if (outputSlot && fullFitRequired) {
            return insertedCount == sourceCount;
        }
        return insertedCount > 0;
    }

    public boolean canExecuteTransfer() {
        return canRenderOverlay();
    }

    public enum OverlayState {
        YELLOW,
        GREEN
    }

    public enum OverlaySymbol {
        PLUS,
        MINUS
    }
}
