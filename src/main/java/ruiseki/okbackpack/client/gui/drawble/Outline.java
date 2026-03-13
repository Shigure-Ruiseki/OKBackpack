package ruiseki.okbackpack.client.gui.drawble;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

public class Outline implements IDrawable {

    private final int color;

    public Outline(int color) {
        this.color = color;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawBorderInsideLTRB(x, y, width, height, color);
    }
}
