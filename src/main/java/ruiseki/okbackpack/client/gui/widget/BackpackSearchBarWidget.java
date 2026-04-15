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
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.search.ItemStackKey;
import ruiseki.okbackpack.common.search.SearchNode;
import ruiseki.okbackpack.common.search.SearchParser;

public class BackpackSearchBarWidget extends TextFieldWidget {

    protected String prevText = "";
    private final BackpackPanel panel;
    private List<BackpackSlot> originalOrder;

    public BackpackSearchBarWidget(BackpackPanel panel) {
        this.panel = panel;
        this.prevText = panel.wrapper.getSearchPhrase();
        background(VANILLA_SEARCH_BACKGROUND);
        value(new StringValue(prevText));
        tooltip().addLine(IKey.lang("gui.search_bar.tool_tip"))
            .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
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
        if (txt == null) txt = "";
        if (!txt.equals(prevText)) {
            final String searchText = txt;
            panel.wrapper.setSearchPhrase(txt);
            panel.backpackSyncHandler.syncToServer(
                BackpackSH.getId(BackpackSHRegisters.UPDATE_SEARCH_PHRASE),
                buffer -> buffer.writeStringToBuffer(searchText));
            doSearch(txt);
            prevText = txt;
        }
    }

    @Override
    public void onInit() {
        super.onInit();
        cacheOriginalOrder();
        prevText = getText();
        doSearch(prevText);
    }

    public void cacheOriginalOrder() {
        Column backpackSlots = panel.backpackInvCol;
        if (backpackSlots == null) return;

        originalOrder = new ArrayList<>();
        for (IWidget child : backpackSlots.getChildren()) {
            if (child instanceof BackpackSlot slot) {
                originalOrder.add(slot);
            }
        }
    }

    public void research() {
        doSearch(prevText);
    }

    public void clearSearch() {
        prevText = "";
        setText("");
        doSearch("");
    }

    public void doSearch(String search) {
        Column backpackSlots = panel.backpackInvCol;
        if (backpackSlots == null) return;

        IWidget parent = backpackSlots.getParent();
        if (!(parent instanceof BackpackList backpackList)) return;

        int columns = panel.getUsableRowSize();
        int slotSize = BackpackSlot.SIZE;

        SearchNode compiledSearch = search.isEmpty() ? null : SearchParser.parse(search);

        if (compiledSearch == null) {
            for (int i = 0; i < originalOrder.size(); i++) {
                BackpackSlot slot = originalOrder.get(i);
                slot.setFocus(true);

                int x = (i % columns) * slotSize;
                int y = (i / columns) * slotSize;
                slot.left(x)
                    .top(y);
            }
            return;
        }

        List<BackpackSlot> matched = new ArrayList<>();
        List<BackpackSlot> others = new ArrayList<>();

        for (BackpackSlot slot : originalOrder) {
            if (!slot.getSlot()
                .getHasStack()) {
                slot.setFocus(false);
                others.add(slot);
                continue;
            }

            ItemStackKey key = ItemStackKey.of(
                slot.getSlot()
                    .getStack());
            boolean match = compiledSearch.matches(key);
            slot.setFocus(match);

            if (match) matched.add(slot);
            else others.add(slot);
        }

        matched.addAll(others);

        for (int i = 0; i < matched.size(); i++) {
            BackpackSlot slot = matched.get(i);
            int x = (i % columns) * slotSize;
            int y = (i / columns) * slotSize;
            slot.left(x)
                .top(y);
            slot.scheduleResize();
        }

        backpackList.clearPendingScroll();
        backpackList.getScrollData()
            .scrollTo(backpackList.getScrollArea(), 0);
    }

}
