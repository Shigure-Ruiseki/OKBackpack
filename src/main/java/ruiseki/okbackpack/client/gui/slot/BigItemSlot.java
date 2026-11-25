package ruiseki.okbackpack.client.gui.slot;

import static ruiseki.okbackpack.client.gui.OKBGuiTextures.BIG_SLOT_TEXTURE;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

public class BigItemSlot extends ItemSlot {

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (context != null) {
            BIG_SLOT_TEXTURE.draw(context, -4, -4, 26, 26, widgetTheme.getTheme());
        }
        super.draw(context, widgetTheme);
    }
}
