package ruiseki.okstoragecore.client.gui.widget;

import java.io.File;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.widget.setting.MemorySettingTab;
import ruiseki.okstoragecore.client.gui.widget.setting.NoSortingSettingTab;
import ruiseki.okstoragecore.client.gui.widget.setting.StorageMainSettingTab;

public class StorageSettingPanel extends ModularPanel {

    private static final File SETTINGS_TEMPLATE_DIR = new File("config/" + Reference.MOD_ID + "/dump");
    private static final int KEY_ESCAPE = 1;
    private static final int KEY_RETURN = 28;
    private static final int KEY_NUMPADENTER = 156;
    private static final int SETTINGS_BUTTON_SIZE = 18;
    private static final int SETTINGS_BUTTON_BOTTOM_OFFSET = 11;
    private static final int SETTINGS_BUTTON_RIGHT_OFFSET = -21;
    private static final int SETTINGS_BUTTON_COMPACT_RIGHT_OFFSET = SETTINGS_BUTTON_RIGHT_OFFSET
        - (SETTINGS_BUTTON_SIZE * 2);
    private static final int SETTINGS_BUTTON_SINGLE_COLUMN_HEIGHT = SETTINGS_BUTTON_SIZE * 5;
    private static final int SETTINGS_BUTTON_COMPACT_COLUMN_HEIGHT = SETTINGS_BUTTON_SIZE * 2;
    private static final int SETTINGS_BUTTON_COMPACT_WIDTH = SETTINGS_BUTTON_SIZE * 3;
    private static final int SETTINGS_BUTTON_RESERVED_TOP = 90;

    protected final StoragePanel storagePanel;

    protected IndexedChildWidget<Tab> tabContainer;

    protected Tab globalTab;
    protected Tab sortingTab;
    protected Tab memoryTab;

    public StorageSettingPanel(StoragePanel storagePanel) {
        super("settings");
        this.storagePanel = storagePanel;

        this.size(
            6,
            this.getStoragePanel()
                .getArea().height)
            .relative(this.getStoragePanel())
            .top(0)
            .right(0);

        this.tabContainer = new IndexedChildWidget<Tab>().size(0)
            .right(0)
            .left(0);
        this.child(this.getTabContainer());

        this.addGlobalTab();
        this.addSortingTab();
        this.addMemoryTab();
    }

    protected void addGlobalTab() {
        this.globalTab = new Tab(1).tooltipStatic(tooltip -> {
            tooltip.clearText();
            tooltip.addLine(IKey.lang("gui.okstoragecore.storage_settings"));
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        });

        this.globalTab.setTabIcon(OSCTextures.BACKPACK);
        this.globalTab.setExpandedWidget(
            new StorageMainSettingTab(this.getStorageWrapper(), this.getStoragePanel(), this, this.getGlobalTab()));
        this.tabContainer.child(this.globalTab);
    }

    protected void addSortingTab() {
        this.sortingTab = new Tab(2).tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (this.sortingTab.isShowExpanded()) {
                tooltip.addLine(IKey.lang("gui.okstoragecore.settings.no_sort.tooltip_open_detail"));
            } else {
                tooltip.addLine(IKey.lang("gui.okstoragecore.settings.no_sort.tooltip"))
                    .addLine(
                        IKey.lang("gui.okstoragecore.settings.no_sort.tooltip_detail")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        })
            .tooltipAutoUpdate(true);
        this.sortingTab.setTabIcon(OSCTextures.NO_SORT);
        this.sortingTab.setExpandedWidget(
            new NoSortingSettingTab(this.getStorageWrapper(), this.getStoragePanel(), this, this.getSortingTab()));

        this.tabContainer.child(this.sortingTab);
    }

    protected void addMemoryTab() {
        this.memoryTab = new Tab(3).tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (this.memoryTab.isShowExpanded()) {
                tooltip.addLine(IKey.lang("gui.okstoragecore.settings.memory.tooltip_open_detail"));
            } else {
                tooltip.addLine(IKey.lang("gui.okstoragecore.settings.memory.tooltip"))
                    .addLine(
                        IKey.lang("gui.okstoragecore.settings.memory.tooltip_detail")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        })
            .tooltipAutoUpdate(true);
        this.memoryTab.setTabIcon(OSCTextures.BRAIN);
        this.memoryTab.setExpandedWidget(
            new MemorySettingTab(this.getStorageWrapper(), this.getStoragePanel(), this, this.getMemoryTab()));
        this.tabContainer.child(this.memoryTab);
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        super.postDraw(context, transformed);
        StoragePanel.LAYERED_TAB_TEXTURE.draw(
            context,
            0,
            0,
            6,
            this.getArea().height,
            WidgetTheme.getDefault()
                .getTheme());
    }

    public void resetTabState() {
        for (Tab tab : this.tabContainer.getTypeChildren()) {
            tab.setShowExpanded(false);
            this.getContext()
                .getUISettings()
                .getRecipeViewerSettings()
                .removeExclusionArea(tab.getExpandedWidget());
        }
        this.updateTabStates();
    }

    public void updateTabStates() {
        Tab openedTab = null;
        for (Tab tab : this.tabContainer.getTypeChildren()) {
            if (tab.isShowExpanded()) {
                openedTab = tab;
                break;
            }
        }

        // Restore reversed render order based on logical tab index
        this.tabContainer.getTypeChildren()
            .stream()
            .sorted((a, b) -> Integer.compare(b.getTabIndex(), a.getTabIndex()))
            .forEach(this.tabContainer::moveFront);

        // Expanded tab always renders last
        if (openedTab != null) {
            this.tabContainer.moveFront(openedTab);
        }
    }

    public IndexedChildWidget<Tab> getTabContainer() {
        return this.tabContainer;
    }

    public IStorageWrapper getStorageWrapper() {
        return this.getStoragePanel()
            .getWrapper();
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public Tab getGlobalTab() {
        return this.globalTab;
    }

    public Tab getSortingTab() {
        return this.sortingTab;
    }

    public Tab getMemoryTab() {
        return this.memoryTab;
    }

    public static int getCompactMinimumPanelHeight() {
        return SETTINGS_BUTTON_RESERVED_TOP + SETTINGS_BUTTON_BOTTOM_OFFSET + SETTINGS_BUTTON_COMPACT_COLUMN_HEIGHT;
    }

    private static int getSingleColumnMinimumPanelHeight() {
        return SETTINGS_BUTTON_RESERVED_TOP + SETTINGS_BUTTON_BOTTOM_OFFSET + SETTINGS_BUTTON_SINGLE_COLUMN_HEIGHT;
    }
}
