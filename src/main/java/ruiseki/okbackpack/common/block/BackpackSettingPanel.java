package ruiseki.okbackpack.common.block;

import static ruiseki.okbackpack.common.block.BackpackPanel.LAYERED_TAB_TEXTURE;

import java.util.ArrayList;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.BackpackSettingWidget;
import ruiseki.okbackpack.client.gui.widget.MemorySettingWidget;
import ruiseki.okbackpack.client.gui.widget.TabWidget;
import ruiseki.okbackpack.client.gui.widget.TabWidget.ExpandDirection;
import ruiseki.okbackpack.client.gui.widget.upgrade.SortingSettingWidget;

public class BackpackSettingPanel extends ModularPanel {

    private final IStoragePanel<?> parent;

    private final List<TabWidget> tabs = new ArrayList<>();
    private final TabWidget backpackTab;
    private final TabWidget memoryTab;
    private final TabWidget sortTab;

    public BackpackSettingPanel(IStoragePanel<?> parent) {
        super("backpack_settings");
        this.parent = parent;

        size(6, parent.getArea().height).relative(parent)
            .top(0)
            .right(0);

        backpackTab = new TabWidget(1, ExpandDirection.RIGHT);
        backpackTab.tooltipStatic(
            tooltip -> tooltip.addLine(IKey.lang("gui.backpack.backpack_settings"))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        backpackTab.setExpandedWidget(new BackpackSettingWidget(parent, this, backpackTab));
        backpackTab.setTabIcon(OKBGuiTextures.BACKPACK_ICON);

        memoryTab = new TabWidget(2, ExpandDirection.RIGHT);
        memoryTab.tooltipStatic(
            tooltip -> tooltip.addLine(IKey.lang("gui.backpack.memory_settings"))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        memoryTab.setExpandedWidget(new MemorySettingWidget(parent, this, memoryTab));
        memoryTab.setTabIcon(OKBGuiTextures.BRAIN_ICON);

        sortTab = new TabWidget(3, ExpandDirection.RIGHT);
        sortTab.tooltipStatic(
            tooltip -> tooltip.addLine(IKey.lang("gui.backpack.sorting_settings"))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        sortTab.setExpandedWidget(new SortingSettingWidget(parent, this, sortTab));
        sortTab.setTabIcon(OKBGuiTextures.NO_SORT_ICON);

        tabs.add(backpackTab);
        tabs.add(memoryTab);
        tabs.add(sortTab);

        child(backpackTab).child(memoryTab)
            .child(sortTab);
    }

    public void updateTabState(int openIndex) {
        TabWidget[] tabs = { backpackTab, memoryTab, sortTab };

        for (TabWidget tab : tabs) {
            tab.setEnabled(true);
        }

        for (int i = 0; i < tabs.length; i++) {
            if (i != openIndex) {
                tabs[i].setShowExpanded(false);
            }
        }

        if (tabs[openIndex].isShowExpanded()) {
            for (int i = openIndex + 1; i < tabs.length; i++) {
                tabs[i].setEnabled(false);
            }
        }
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public void onOpen(ModularScreen screen) {
        super.onOpen(screen);
        parent.setMemorySettingTabOpened(memoryTab.isShowExpanded());
        parent.setShouldMemorizeRespectNBT(((MemorySettingWidget) memoryTab.getExpandedWidget()).isRespectNBT());
        parent.setSortingSettingTabOpened(sortTab.isShowExpanded());
    }

    @Override
    public void onClose() {
        super.onClose();
        parent.setMemorySettingTabOpened(false);
        parent.setShouldMemorizeRespectNBT(false);
        parent.setSortingSettingTabOpened(false);
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        super.postDraw(context, transformed);
        LAYERED_TAB_TEXTURE.draw(
            context,
            0,
            0,
            6,
            getArea().height,
            WidgetTheme.getDefault()
                .getTheme());
    }

}
