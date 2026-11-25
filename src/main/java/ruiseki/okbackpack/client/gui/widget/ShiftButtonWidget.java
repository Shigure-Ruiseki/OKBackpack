package ruiseki.okbackpack.client.gui.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ButtonWidget;

public class ShiftButtonWidget extends ButtonWidget<ShiftButtonWidget> {

    private final IDrawable matchedIcon;
    private final IDrawable allIcon;

    public ShiftButtonWidget(IDrawable matchedIcon, IDrawable allIcon) {
        this.matchedIcon = matchedIcon;
        this.allIcon = allIcon;
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);
        if (Interactable.hasShiftDown()) {
            allIcon.drawAtZero(context, getArea(), widgetTheme.getTheme());
        } else {
            matchedIcon.drawAtZero(context, getArea(), widgetTheme.getTheme());
        }
    }
}
