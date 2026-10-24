
package ruiseki.okstoragecore.client.gui.widget;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.Reference;

public abstract class ExpandedTab extends ParentWidget<ExpandedTab> {

    public static final UITexture TAB_TEXTURE = UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls")
        .imageSize(256, 256)
        .xy(128, 0, 128, 256)
        .adaptable(4)
        .tiled()
        .build();

    protected final String titleKey;
    protected final Tab.ExpandDirection expandDirection;

    protected final PhantomTab phantomTab;
    protected final TextWidget<?> titleField;
    protected final Flow headerRow;

    protected final Flow contentsColumn;

    public ExpandedTab(IDrawable icon, String titleKey) {
        this(icon, titleKey, Tab.ExpandDirection.RIGHT);
    }

    public ExpandedTab(IDrawable icon, String titleKey, Tab.ExpandDirection expandDirection) {
        this.titleKey = titleKey;
        this.expandDirection = expandDirection;

        switch (expandDirection) {
            case LEFT:
                this.right(0);
                break;
            case RIGHT:
                this.left(0);
                break;
        }

        this.background(TAB_TEXTURE);
        this.coverChildren();

        this.phantomTab = this.addPhantomTab(icon);
        this.titleField = this.addTitleField();
        this.headerRow = this.addHeaderRow();

        this.contentsColumn = this.addContentsColumn();
        this.child(this.contentsColumn);
    }

    protected Flow addContentsColumn() {
        Flow column = Flow.column()
            .coverChildren()
            .padding(7)
            .paddingTop(5)
            .childPadding(2)
            .crossAxisAlignment(Alignment.CrossAxis.START);
        column.addChild(this.getHeaderRow(), 0);
        return column;
    }

    protected PhantomTab addPhantomTab(IDrawable icon) {
        return new PhantomTab(icon.asWidget()).top(0);
    }

    protected TextWidget<?> addTitleField() {
        return new TextWidget<>(IKey.lang(this.getTitleKey())).textAlign(Alignment.TopCenter)
            .paddingLeft(2)
            .maxWidth(75);
    }

    protected Flow addHeaderRow() {
        Flow row = Flow.row();
        switch (this.getExpandDirection()) {
            case LEFT:
                row.coverChildren()
                    .child(this.getTitleField())
                    .child(this.getPhantomTab());
                break;
            case RIGHT:
                row.coverChildren()
                    .child(this.getPhantomTab())
                    .child(this.getTitleField());
                break;
        }
        return row;
    }

    public int getCoveredTabSize() {
        return Math.max(1, this.getArea().height / 30);
    }

    public String getTitleKey() {
        return titleKey;
    }

    public Tab.ExpandDirection getExpandDirection() {
        return expandDirection;
    }

    protected PhantomTab getPhantomTab() {
        return phantomTab;
    }

    protected TextWidget<?> getTitleField() {
        return titleField;
    }

    protected Flow getHeaderRow() {
        return headerRow;
    }

    protected Flow getContentsColumn() {
        return contentsColumn;
    }

    public abstract void updateTabState();

    protected class PhantomTab extends SingleChildWidget<PhantomTab> implements Interactable {

        private final Widget<?> tabIcon;

        public PhantomTab(Widget<?> tabIcon) {
            this.tabIcon = tabIcon;
            this.tabIcon.size(18);
            this.size(18);

            switch (expandDirection) {
                case LEFT:
                    this.right(0);
                    break;

                case RIGHT:
                    break;
            }

            this.child(tabIcon);
        }

        public Widget<?> getTabIcon() {
            return tabIcon;
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            if (!isEnabled()) return Result.STOP;

            if (mouseButton == 0) {
                updateTabState();
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
            }

            return Result.STOP;
        }
    }
}
