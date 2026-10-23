package ruiseki.okbackpack.client.gui.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ButtonWidget;

public class ShiftButtonWidget extends ButtonWidget<ShiftButtonWidget> {

    private IDrawable notShiftIcon;
    private IDrawable shiftIcon;
    private IDrawable notShiftHoverIcon;
    private IDrawable shiftHoverIcon;

    private boolean drawInBackground;

    public ShiftButtonWidget() {

    }

    public ShiftButtonWidget drawInBackground(boolean drawInBackground) {
        this.drawInBackground = drawInBackground;
        return this;
    }

    public ShiftButtonWidget setNotShift(IDrawable notShiftIcon) {
        this.notShiftIcon = notShiftIcon;
        return this;
    }

    public ShiftButtonWidget setShift(IDrawable shiftIcon) {
        this.shiftIcon = shiftIcon;
        return this;
    }

    public ShiftButtonWidget setNotShiftHover(IDrawable notShiftHoverIcon) {
        this.notShiftHoverIcon = notShiftHoverIcon;
        return this;
    }

    public ShiftButtonWidget setShiftHover(IDrawable shiftHoverIcon) {
        this.shiftHoverIcon = shiftHoverIcon;
        return this;
    }

    private IDrawable getCurrentIcon() {
        boolean shiftDown = Interactable.hasShiftDown();

        if (isHovering()) {
            IDrawable hoverIcon = shiftDown ? shiftHoverIcon : notShiftHoverIcon;

            if (hoverIcon != null && hoverIcon != IDrawable.NONE) {
                return hoverIcon;
            }
        }

        return shiftDown ? shiftIcon : notShiftIcon;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawBackground(context, widgetTheme);

        if (drawInBackground) {
            drawIcon(context, widgetTheme);
        }
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);

        if (!drawInBackground) {
            drawIcon(context, widgetTheme);
        }
    }

    private void drawIcon(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IDrawable icon = getCurrentIcon();
        if (icon != null && icon != IDrawable.NONE) {
            icon.drawAtZero(context, getArea(), widgetTheme.getTheme());
        }
    }

    protected IDrawable getCurrentButtonBackground() {
        return getCurrentIcon();
    }

    protected IDrawable getButtonBackground() {
        return Interactable.hasShiftDown() ? shiftIcon : notShiftIcon;
    }

    protected IDrawable getButtonHoverBackground() {
        return Interactable.hasShiftDown() ? shiftHoverIcon : notShiftHoverIcon;
    }
}
