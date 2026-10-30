package ruiseki.okstoragecore.client.gui.widget;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.TabTexture;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.SingleChildWidget;

public class Tab extends SingleChildWidget<Tab> implements Interactable {

    public enum ExpandDirection {
        LEFT,
        RIGHT
    }

    public static final TabTexture TAB_TEXTURE = GuiTextures.TAB_RIGHT;

    private final int tabIndex;
    private final ExpandDirection expandDirection;

    private boolean showExpanded = false;
    private ExpandedTab expandedWidget = null;
    private IDrawable tabIcon = null;

    public Tab(int tabIndex) {
        this(tabIndex, tabIndex * 30, ExpandDirection.RIGHT);
    }

    public Tab(int tabIndex, ExpandDirection expandDirection) {
        this(tabIndex, tabIndex * 30, expandDirection);
    }

    public Tab(int tabIndex, int top) {
        this(tabIndex, top, ExpandDirection.RIGHT);
    }

    public Tab(int tabIndex, int top, ExpandDirection expandDirection) {
        this.tabIndex = tabIndex;
        this.expandDirection = expandDirection;

        size(TAB_TEXTURE.getWidth(), TAB_TEXTURE.getHeight());
        top(top);

        switch (expandDirection) {
            case LEFT:
                left(-TAB_TEXTURE.getWidth() + 4);
                break;
            case RIGHT:
                right(-TAB_TEXTURE.getWidth() + 4);
                break;
        }
    }

    public int getTabIndex() {
        return this.tabIndex;
    }

    public ExpandDirection getExpandDirection() {
        return this.expandDirection;
    }

    public Tab setShowExpanded(boolean showExpanded) {
        this.showExpanded = showExpanded;
        return this;
    }

    public boolean isShowExpanded() {
        return this.showExpanded;
    }

    public ExpandedTab getExpandedWidget() {
        return this.expandedWidget;
    }

    public Tab setExpandedWidget(ExpandedTab value) {
        this.expandedWidget = value;
        if (value != null) {
            if (this.expandDirection == ExpandDirection.LEFT) {
                value.right(0);
            }
            this.child(value.setEnabledIf(expandedTabWidget -> this.showExpanded));
        }
        return this;
    }

    public Tab setTabIcon(IDrawable tabIcon) {
        this.tabIcon = tabIcon;
        return this;
    }

    public IDrawable getTabIcon() {
        return this.tabIcon;
    }

    @Override
    public void onInit() {
        getContext().getUISettings()
            .getRecipeViewerSettings()
            .addExclusionArea(this);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!isEnabled() || this.expandedWidget == null) {
            return Result.STOP;
        }

        if (mouseButton == 0) {
            this.expandedWidget.updateTabState();
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }

        return Result.STOP;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        if (!this.showExpanded && this.tabIcon != null) {
            this.tabIcon.draw(context, 8, 6, 16, 16, widgetTheme.getTheme());
        }
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawBackground(context, widgetTheme);
        if (this.showExpanded) return;
        int index = this.tabIndex == 0 ? -1 : 0;
        switch (this.expandDirection) {
            case LEFT:
                GuiTextures.TAB_LEFT.get(index, false)
                    .drawAtZero(context, TAB_TEXTURE.getWidth(), TAB_TEXTURE.getHeight(), widgetTheme.getTheme());
                break;
            case RIGHT:
                GuiTextures.TAB_RIGHT.get(index, false)
                    .drawAtZero(context, TAB_TEXTURE.getWidth(), TAB_TEXTURE.getHeight(), widgetTheme.getTheme());
                break;
        }
    }
}
