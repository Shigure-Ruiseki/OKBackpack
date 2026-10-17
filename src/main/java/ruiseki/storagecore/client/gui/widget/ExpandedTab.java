package ruiseki.storagecore.client.gui.widget;

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

import lombok.Getter;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.widget.TabWidget;

public abstract class ExpandedTab extends ParentWidget<ExpandedTab> {

    public static final UITexture TAB_TEXTURE = UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls")
        .imageSize(256, 256)
        .xy(128, 0, 128, 256)
        .adaptable(4)
        .tiled()
        .build();

    protected final int coveredTabSize;
    protected final TabWidget.ExpandDirection expandDirection;

    protected final ExpandedTab.PhantomTabWidget phantomTabWidget;
    protected final TextWidget<?> titleKeyWidget;
    protected final Flow upperTabRow;

    public ExpandedTab(int coveredTabSize, IDrawable delegatedIcon, String titleKey) {
        this(coveredTabSize, delegatedIcon, titleKey, 75, TabWidget.ExpandDirection.RIGHT);
    }

    public ExpandedTab(int coveredTabSize, IDrawable delegatedIcon, String titleKey, int width) {
        this(coveredTabSize, delegatedIcon, titleKey, width, TabWidget.ExpandDirection.RIGHT);
    }

    public ExpandedTab(int coveredTabSize, IDrawable delegatedIcon, String titleKey, int width,
        TabWidget.ExpandDirection expandDirection) {
        this.coveredTabSize = coveredTabSize;
        this.expandDirection = expandDirection;

        this.phantomTabWidget = new ExpandedTab.PhantomTabWidget(delegatedIcon.asWidget()).top(0);

        this.titleKeyWidget = new TextWidget<>(IKey.lang(titleKey)).textAlign(Alignment.CENTER)
            .paddingRight(2)
            .topRel(0.5f);

        this.upperTabRow = Flow.row()
            .coverChildrenHeight();

        switch (expandDirection) {
            case LEFT:
                this.right(0);

                this.upperTabRow.child(
                    new Widget<>().width(4)
                        .name("placeholder"))
                    .child(titleKeyWidget)
                    .child(phantomTabWidget);
                break;

            case RIGHT:
                this.left(0);

                this.upperTabRow.width(width - 3)
                    .child(phantomTabWidget)
                    .child(titleKeyWidget.expanded());
                break;
        }

        this.width(width);
        this.height(coveredTabSize * 30);
        this.background(TAB_TEXTURE);
        this.child(upperTabRow);
    }

    public int getCoveredTabSize() {
        return coveredTabSize;
    }

    public abstract void updateTabState();

    protected class PhantomTabWidget extends SingleChildWidget<ExpandedTab.PhantomTabWidget> implements Interactable {

        @Getter
        private final Widget<?> tabIcon;

        public PhantomTabWidget(Widget<?> tabIcon) {
            this.tabIcon = tabIcon;
            this.size(28, 24);
            this.tabIcon.size(18)
                .top(5);

            switch (expandDirection) {
                case LEFT:
                    this.right(0);
                    tabIcon.right(7);
                    break;

                case RIGHT:
                    tabIcon.left(7);
                    break;
            }

            this.child(tabIcon);
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            if (!isEnabled()) {
                return Result.STOP;
            }

            if (mouseButton == 0) {
                updateTabState();
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
            }

            return Result.STOP;
        }
    }
}
