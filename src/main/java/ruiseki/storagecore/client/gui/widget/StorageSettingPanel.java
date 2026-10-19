package ruiseki.storagecore.client.gui.widget;

import static ruiseki.okbackpack.common.block.BackpackPanel.LAYERED_TAB_TEXTURE;

import java.util.ArrayList;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.setting.MemorySettingTab;
import ruiseki.storagecore.client.gui.widget.setting.SortingSettingTab;

public class StorageSettingPanel extends ModularPanel {

    protected final IStorageWrapper storageWrapper;
    protected final StoragePanel storagePanel;

    protected List<Tab> tabs = new ArrayList<>();
    protected Tab storageTab;
    protected Tab sortingTab;
    protected Tab memoryTab;

    public StorageSettingPanel(IStorageWrapper storageWrapper, StoragePanel storagePanel) {
        super("settings");
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;

        this.size(
            6,
            this.getStoragePanel()
                .getArea().height)
            .relative(this.getStoragePanel())
            .top(0)
            .right(0);
        this.addStorageTab();
        this.addSortingTab();
        this.addMemoryTab();
    }

    protected void addStorageTab() {
        this.storageTab = new Tab(1).tooltipStatic(
            tooltip -> tooltip.addLine(IKey.lang("gui.storage.storage_settings"))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        this.tabs.add(this.getStorageTab());
        this.child(this.getStorageTab());
    }

    protected void addSortingTab() {
        this.sortingTab = new Tab(2).tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (this.sortingTab.isShowExpanded()) {
                tooltip.addLine(IKey.lang("gui.storage.sorting_settings.tooltip_open_detail"));
            } else {
                tooltip.addLine(IKey.lang("gui.storage.sorting_settings"))
                    .addLine(
                        IKey.lang("gui.storage.sorting_settings.tooltip_detail")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        })
            .tooltipAutoUpdate(true);
        this.sortingTab.setTabIcon(OKBGuiTextures.NO_SORT_ICON);
        this.sortingTab.setExpandedWidget(
            new SortingSettingTab(this.getStorageWrapper(), this.getStoragePanel(), this, this.getSortingTab()));
        this.tabs.add(this.getSortingTab());
        this.child(this.getSortingTab());
    }

    protected void addMemoryTab() {
        this.memoryTab = new Tab(3).tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (this.memoryTab.isShowExpanded()) {
                tooltip.addLine(IKey.lang("gui.storage.memory_settings.tooltip_open_detail"));
            } else {
                tooltip.addLine(IKey.lang("gui.storage.memory_settings"))
                    .addLine(
                        IKey.lang("gui.storage.memory_settings.tooltip_detail")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        })
            .tooltipAutoUpdate(true);
        this.memoryTab.setTabIcon(OKBGuiTextures.BRAIN_ICON);
        this.memoryTab.setExpandedWidget(
            new MemorySettingTab(this.getStorageWrapper(), this.getStoragePanel(), this, this.getMemoryTab()));
        this.tabs.add(this.getMemoryTab());
        this.child(this.getMemoryTab());
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        super.postDraw(context, transformed);
        LAYERED_TAB_TEXTURE.draw(
            context,
            0,
            0,
            6,
            this.getArea().height,
            WidgetTheme.getDefault()
                .getTheme());
    }

    public void resetTabState() {
        for (Tab tab : this.tabs) {
            tab.setEnabled(true);
            tab.setShowExpanded(false);
            this.getContext()
                .getUISettings()
                .getRecipeViewerSettings()
                .removeExclusionArea(tab.getExpandedWidget());
        }
    }

    public void updateTabStates() {
        Integer openedIndex = null;
        int coveredSize = 0;
        for (Tab tab : this.tabs) {
            if (!tab.isShowExpanded()) {
                continue;
            }

            openedIndex = tab.getTabIndex();
            if (tab.getExpandedWidget() != null) {
                coveredSize = tab.getExpandedWidget()
                    .getCoveredTabSize();
            }
            break;
        }

        for (Tab tab : this.tabs) {
            boolean enabled = true;
            if (openedIndex != null) {
                int dist = tab.getTabIndex() - openedIndex;

                if (dist > 0 && dist < coveredSize) {
                    enabled = false;
                }
            }
            tab.setEnabled(enabled);
        }
    }

    public IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public Tab getStorageTab() {
        return storageTab;
    }

    public Tab getSortingTab() {
        return sortingTab;
    }

    public Tab getMemoryTab() {
        return this.memoryTab;
    }
}
