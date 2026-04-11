package ruiseki.okbackpack.client.gui.slot;

import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

public class CustomBackgroundSlot extends ItemSlot {

    private final UITexture emptyIcon;

    public CustomBackgroundSlot(UITexture emptyIcon) {
        this.emptyIcon = emptyIcon;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawBackground(context, widgetTheme);
        if (context != null && getSlot().getStack() == null) {
            emptyIcon.draw(context, 1, 1, 16, 16, widgetTheme.getTheme());
        }
    }
}
