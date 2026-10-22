package ruiseki.okstoragecore.client.gui.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

public class StorageSlotGrid<T extends ItemSlot> extends ParentWidget<StorageSlotGrid<T>> {

    private int slotSize = 18;

    private String slotGroupName;
    private SlotGroup slotGroup;

    private boolean collapseDisabledChild = true;
    private boolean compactRows = true;

    private int gridColumns;

    private final Map<IWidget, GridPosition> childPositions = new IdentityHashMap<>();

    public StorageSlotGrid() {}

    public static <T extends ItemSlot> Builder<T> builder() {
        return new Builder<>();
    }

    public StorageSlotGrid<T> slotGroup(String slotGroupName) {
        this.slotGroupName = slotGroupName;
        return getThis();
    }

    public StorageSlotGrid<T> slotGroup(SlotGroup slotGroup) {
        this.slotGroup = slotGroup;
        return getThis();
    }

    public StorageSlotGrid<T> collapseDisabledChild() {
        return collapseDisabledChild(true);
    }

    public StorageSlotGrid<T> collapseDisabledChild(boolean collapse) {
        this.collapseDisabledChild = collapse;
        return getThis();
    }

    public StorageSlotGrid<T> compactRows() {
        return compactRows(true);
    }

    public StorageSlotGrid<T> compactRows(boolean compactRows) {
        this.compactRows = compactRows;
        return getThis();
    }

    public StorageSlotGrid<T> slotSize(int slotSize) {
        this.slotSize = slotSize;
        return getThis();
    }

    @Override
    protected void onChildAdd(IWidget child) {
        super.onChildAdd(child);
        if (child instanceof ItemSlot slot && slot.isSynced() && slot.getSlot() != null) {
            if (this.slotGroup != null) {
                slot.getSlot()
                    .slotGroup(this.slotGroup);
            } else if (this.slotGroupName != null) {
                slot.getSlot()
                    .slotGroup(this.slotGroupName);
            }
        }
    }

    private void setGridPosition(IWidget widget, int column, int row) {
        this.childPositions.put(widget, new GridPosition(column, row));
    }

    /**
     * Compact enabled slots across the entire grid, not independently
     * within each original row.
     */
    public void refreshGridLayout() {
        int maxColumn = 0;
        int maxRow = 0;
        int visibleIndex = 0;

        for (IWidget child : getChildren()) {
            GridPosition original = this.childPositions.get(child);

            if (original == null) continue;

            if (this.collapseDisabledChild && !child.isEnabled()) {
                continue;
            }

            int column;
            int row;

            if (this.collapseDisabledChild && this.compactRows) {
                if (this.gridColumns <= 0) continue;

                column = visibleIndex % this.gridColumns;
                row = visibleIndex / this.gridColumns;
                visibleIndex++;
            } else {
                column = original.column;
                row = original.row;
            }

            child.resizer()
                .left(column * this.slotSize)
                .top(row * this.slotSize);

            maxColumn = Math.max(maxColumn, column + 1);
            maxRow = Math.max(maxRow, row + 1);
        }

        resizer().size(maxColumn * this.slotSize, maxRow * this.slotSize);

        scheduleResize();
    }

    private static final class GridPosition {

        private final int column;
        private final int row;

        private GridPosition(int column, int row) {
            this.column = column;
            this.row = row;
        }
    }

    public static class Builder<T extends ItemSlot> {

        private final List<String> matrix = new ArrayList<>();

        private final Char2ObjectMap<IntFunction<T>> keys = new Char2ObjectOpenHashMap<>();

        private int maxSlots = Integer.MAX_VALUE;
        private String slotGroupName;
        private SlotGroup slotGroup;

        private boolean collapseDisabledChild = true;
        private boolean compactRows = true;

        private int slotSize = 18;

        private Builder() {
            this.keys.put(' ', null);
        }

        public Builder<T> matrix(int columns, int rows, char key) {
            if (columns <= 0 || rows <= 0) {
                throw new IllegalArgumentException("Columns and rows must be greater than 0");
            }

            this.matrix.clear();

            String row = String.valueOf(key)
                .repeat(columns);

            for (int r = 0; r < rows; r++) {
                this.matrix.add(row);
            }

            return this;
        }

        public Builder<T> maxSlots(int maxSlots) {
            if (maxSlots < 0) {
                throw new IllegalArgumentException("maxSlots must not be negative");
            }
            this.maxSlots = maxSlots;
            return this;
        }

        public Builder<T> key(char c, IntFunction<T> widgetFactory) {
            this.keys.put(c, widgetFactory);
            return this;
        }

        public Builder<T> slotGroup(String slotGroupName) {
            this.slotGroupName = slotGroupName;
            return this;
        }

        public Builder<T> slotGroup(SlotGroup slotGroup) {
            this.slotGroup = slotGroup;
            return this;
        }

        public Builder<T> collapseDisabledChild(boolean collapse) {
            this.collapseDisabledChild = collapse;
            return this;
        }

        public Builder<T> compactRows(boolean compactRows) {
            this.compactRows = compactRows;
            return this;
        }

        public Builder<T> slotSize(int slotSize) {
            this.slotSize = slotSize;
            return this;
        }

        public StorageSlotGrid<T> build() {
            StorageSlotGrid<T> grid = new StorageSlotGrid<>();

            grid.slotGroup(this.slotGroup);
            grid.slotGroup(this.slotGroupName);
            grid.compactRows(this.compactRows);
            grid.collapseDisabledChild(this.collapseDisabledChild);

            int rowIndex = 0;
            int maxColumns = 0;
            int createdSlots = 0;

            Map<Character, Integer> charCount = new HashMap<>();

            for (String row : this.matrix) {
                for (int col = 0; col < row.length(); col++) {
                    char c = row.charAt(col);

                    int index = charCount.getOrDefault(c, 0);
                    charCount.put(c, index + 1);

                    IntFunction<T> factory = this.keys.get(c);
                    if (factory == null) {
                        continue;
                    }

                    if (createdSlots >= this.maxSlots) {
                        continue;
                    }

                    T widget = factory.apply(index);
                    if (widget == null) {
                        continue;
                    }

                    grid.setGridPosition(widget, col, rowIndex);
                    grid.child(widget);
                    createdSlots++;
                }

                maxColumns = Math.max(maxColumns, row.length());
                rowIndex++;
            }

            grid.gridColumns = maxColumns;
            grid.slotSize(this.slotSize);
            grid.refreshGridLayout();

            return grid;
        }
    }
}
