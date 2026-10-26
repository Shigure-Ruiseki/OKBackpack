package ruiseki.okstoragecore.client.gui.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.widget.IParentWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.Widget;

public class IndexedChildWidget<T extends IWidget> extends Widget<IndexedChildWidget<T>>
    implements IParentWidget<T, IndexedChildWidget<T>> {

    private final List<T> children = new ArrayList<>();

    @Override
    public @NotNull List<IWidget> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @NotNull
    public List<T> getTypeChildren() {
        return Collections.unmodifiableList(this.children);
    }

    public Optional<T> getChild(int index) {
        if (index < 0 || index >= this.getSize()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.get(index));
    }

    public T get(int index) {
        return this.children.get(index);
    }

    @Override
    public boolean addChild(T child, int index) {
        if (child == null || child == this) {
            return false;
        }

        int oldIndex = this.children.indexOf(child);

        // Existing child: reorder only
        if (oldIndex >= 0) {
            if (index < 0) {
                this.moveFront(child);
            } else {
                this.setChildIndex(child, index);
            }
            return true;
        }

        // New child: insert
        int insertIndex = index < 0 ? this.children.size() : Math.min(index, this.children.size());

        this.children.add(insertIndex, child);

        if (this.isValid()) {
            child.initialise(this, true);
            this.scheduleResize();
        }

        return true;
    }

    public boolean removeChild(T child) {
        if (!this.children.remove(child)) {
            return false;
        }

        child.dispose();
        this.scheduleResize();
        return true;
    }

    public IndexedChildWidget<T> setChildIndex(T child, int index) {
        int oldIndex = this.children.indexOf(child);
        if (oldIndex < 0) return this;

        int newIndex = Math.max(0, Math.min(index, this.children.size() - 1));
        if (oldIndex == newIndex) return this;

        this.children.remove(oldIndex);
        this.children.add(newIndex, child);

        return this;
    }

    public IndexedChildWidget<T> moveFront(T child) {
        return this.setChildIndex(child, this.children.size() - 1);
    }

    public IndexedChildWidget<T> moveBack(T child) {
        return this.setChildIndex(child, 0);
    }

    public int getChildIndex(T child) {
        return this.children.indexOf(child);
    }

    public int getSize() {
        return this.children.size();
    }

    public boolean hasChild(T child) {
        return this.children.contains(child);
    }
}
