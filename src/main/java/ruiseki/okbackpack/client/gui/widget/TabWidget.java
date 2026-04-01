package ruiseki.okbackpack.client.gui.widget;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.TabTexture;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.SingleChildWidget;

import lombok.Getter;
import lombok.Setter;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;

public class TabWidget extends SingleChildWidget<TabWidget> implements Interactable {

    public enum ExpandDirection {
        LEFT,
        RIGHT
    }

    public static final TabTexture TAB_TEXTURE = GuiTextures.TAB_RIGHT;

    @Getter
    private final int tabIndex;
    private final ExpandDirection expandDirection;

    @Setter
    @Getter
    private boolean showExpanded = false;
    @Getter
    private ExpandedTabWidget expandedWidget = null;
    @Setter
    @Getter
    private IDrawable tabIcon = null;

    public TabWidget(int tabIndex) {
        this(tabIndex, tabIndex * 30, ExpandDirection.RIGHT);
    }

    public TabWidget(int tabIndex, ExpandDirection expandDirection) {
        this(tabIndex, tabIndex * 30, expandDirection);
    }

    public TabWidget(int tabIndex, int top) {
        this(tabIndex, top, ExpandDirection.RIGHT);
    }

    public TabWidget(int tabIndex, int top, ExpandDirection expandDirection) {
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

    public void setExpandedWidget(ExpandedTabWidget value) {
        this.expandedWidget = value;

        if (value != null) {
            if (expandDirection == ExpandDirection.LEFT) {
                value.right(0);
            }
            child(value.setEnabledIf(expandedTabWidget -> showExpanded));
        }
    }

    @Override
    public void onInit() {
        getContext().getUISettings()
            .getRecipeViewerSettings()
            .addExclusionArea(this);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!isEnabled() || expandedWidget == null) {
            return Result.STOP;
        }

        if (mouseButton == 0) {
            expandedWidget.updateTabState();
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }

        return Result.STOP;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);

        if (showExpanded) {
            return;
        }

        if (tabIcon != null) {
            tabIcon.draw(context, 8, 6, 16, 16, widgetTheme.getTheme());
        }
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawBackground(context, widgetTheme);
        if (showExpanded) {
            return;
        }

        int index = (tabIndex == 0) ? -1 : 0;

        switch (expandDirection) {
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
