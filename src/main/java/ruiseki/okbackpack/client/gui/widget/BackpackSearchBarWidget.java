package ruiseki.okbackpack.client.gui.widget;

import static ruiseki.okbackpack.client.gui.OKBGuiTextures.VANILLA_SEARCH_BACKGROUND;

import java.util.ArrayList;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ruiseki.okbackpack.client.gui.slot.BackpackSlot;
import ruiseki.okbackpack.common.block.BackpackPanel;

public class BackpackSearchBarWidget extends TextFieldWidget {

    protected String prevText = "";
    private final BackpackPanel panel;
    private List<BackpackSlot> originalOrder;

    public BackpackSearchBarWidget(BackpackPanel panel) {
        this.panel = panel;
        background(VANILLA_SEARCH_BACKGROUND);
        value(new StringValue(prevText));
        tooltip().addLine(IKey.lang("gui.search_bar.tool_tip"))
            .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
    }

    private void cacheOriginalOrder() {
        Column backpackSlots = panel.getBackpackInvCol();
        if (backpackSlots == null) return;

        originalOrder = new ArrayList<>();
        for (IWidget child : panel.getBackpackInvCol()
            .getChildren()) {
            if (child instanceof BackpackSlot slot) {
                originalOrder.add(slot);
            }
        }
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IDrawable bg = getCurrentBackground(context.getTheme(), widgetTheme);
        if (bg != null) {
            bg.draw(context, 2, -1, getArea().width - 4, getArea().height + 1, widgetTheme.getTheme());
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        String txt = getText();

        if (!txt.equals(prevText)) {
            doSearch(txt);
            prevText = txt;
        }
    }

    @Override
    public void onInit() {
        super.onInit();
        cacheOriginalOrder();
        if (panel.getWrapper()
            .isSearchBackpack()) {
            prevText = "";
            value(new StringValue(prevText));
        } else {
            doSearch(prevText);
        }
    }

    public void research() {
        doSearch(prevText);
    }

    public void doSearch(String search) {

    }

}
