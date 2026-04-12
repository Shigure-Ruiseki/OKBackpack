package ruiseki.okbackpack.client.gui.widget.upgrade;

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
import com.cleanroommc.modularui.widgets.layout.Row;

import lombok.Getter;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.widget.TabWidget.ExpandDirection;

public abstract class ExpandedTabWidget extends ParentWidget<ExpandedTabWidget> {

    public static final UITexture TAB_TEXTURE = UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls")
        .imageSize(256, 256)
        .xy(128, 0, 128, 256)
        .adaptable(4)
        .tiled()
        .build();

    @Getter
    protected final int coveredTabSize;
    protected final ExpandDirection expandDirection;

    protected final PhantomTabWidget phantomTabWidget;
    protected final TextWidget<?> titleKeyWidget;
    protected final Row upperTabRow;

    public ExpandedTabWidget(int coveredTabSize, IDrawable delegatedIcon, String titleKey) {
        this(coveredTabSize, delegatedIcon, titleKey, 75, ExpandDirection.RIGHT);
    }

    public ExpandedTabWidget(int coveredTabSize, IDrawable delegatedIcon, String titleKey, int width) {
        this(coveredTabSize, delegatedIcon, titleKey, width, ExpandDirection.RIGHT);
    }

    public ExpandedTabWidget(int coveredTabSize, IDrawable delegatedIcon, String titleKey, int width,
        ExpandDirection expandDirection) {
        this.coveredTabSize = coveredTabSize;
        this.expandDirection = expandDirection;

        this.phantomTabWidget = new PhantomTabWidget(delegatedIcon.asWidget()).top(0);

        this.titleKeyWidget = new TextWidget<>(IKey.lang(titleKey)).textAlign(Alignment.CENTER)
            .paddingRight(2)
            .topRel(0.5f);

        this.upperTabRow = (Row) new Row().coverChildrenHeight();

        switch (expandDirection) {
            case LEFT:
                right(0);

                upperTabRow.child(
                    new Widget<>().width(4)
                        .name("placeholder"))
                    .child(titleKeyWidget)
                    .child(phantomTabWidget);
                break;

            case RIGHT:
                left(0);

                upperTabRow.width(width - 3)
                    .child(phantomTabWidget)
                    .child(titleKeyWidget.expanded());
                break;
        }

        width(width);
        height(coveredTabSize * 30);
        background(TAB_TEXTURE);
        child(upperTabRow);
    }

    public abstract void updateTabState();

    protected class PhantomTabWidget extends SingleChildWidget<PhantomTabWidget> implements Interactable {

        @Getter
        private final Widget<?> tabIcon;

        public PhantomTabWidget(Widget<?> tabIcon) {
            this.tabIcon = tabIcon;
            size(28, 24);
            tabIcon.size(18)
                .top(5);

            switch (expandDirection) {
                case LEFT:
                    right(0);
                    tabIcon.right(7);
                    break;

                case RIGHT:
                    tabIcon.left(7);
                    break;
            }

            child(tabIcon);
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
