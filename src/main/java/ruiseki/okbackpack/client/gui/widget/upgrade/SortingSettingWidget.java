package ruiseki.okbackpack.client.gui.widget.upgrade;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Row;

import lombok.Getter;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.TabWidget;
import ruiseki.okbackpack.client.gui.widget.TabWidget.ExpandDirection;
import ruiseki.okbackpack.common.block.BackpackSettingPanel;
import ruiseki.okbackpack.common.block.BackpackWrapper;

public class SortingSettingWidget extends ExpandedTabWidget {

    public static final int[] SLOT_COLORS = { 0x7FFF00, // Chartreuse (default)
        0xFF0000, // Red
        0x00FF00, // Green
        0x0000FF, // Blue
        0xFFFF00, // Yellow
        0xFF00FF, // Magenta
        0x00FFFF, // Cyan
        0xFF8000, // Orange
        0x8000FF, // Purple
        0x0080FF, // Sky Blue
        0xFF0080, // Rose
        0x80FF00, // Lime
        0x00FF80, // Spring Green
        0xFF8080, // Light Red
        0x80FF80, // Light Green
        0x8080FF, // Light Blue
    };

    @Getter
    private int currentColorIndex;

    private final IStoragePanel<?> panel;
    private final BackpackWrapper wrapper;
    private final BackpackSettingPanel settingPanel;
    private final TabWidget parentTabWidget;

    public SortingSettingWidget(IStoragePanel<?> panel, BackpackSettingPanel settingPanel, TabWidget parentTabWidget) {
        super(2, OKBGuiTextures.NO_SORT_ICON, "gui.backpack.sorting_settings", 80, ExpandDirection.RIGHT);

        this.panel = panel;
        this.wrapper = (BackpackWrapper) panel.getWrapper();
        this.settingPanel = settingPanel;
        this.parentTabWidget = parentTabWidget;
        this.currentColorIndex = wrapper.getNoSortColorIndex();

        Row buttonRow = (Row) new Row().leftRel(0.5f)
            .height(20)
            .coverChildrenWidth()
            .childPadding(0);

        ButtonWidget<?> selectAllButton = new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.ALL_FOUR_SLOT_ICON)
            .onMousePressed(button -> {
                if (button == 0) {
                    for (int i = 0; i < wrapper.getSlots(); i++) {
                        wrapper.setSlotLocked(i, true);
                    }

                    for (BackpackSlotSH syncHandler : (BackpackSlotSH[]) panel.getStorageSlotSH()) {
                        syncHandler.syncToServer(BackpackSlotSH.getId(BackpackSlotSHRegisters.UPDATE_SET_SLOT_LOCK));
                    }

                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.backpack.select_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        ButtonWidget<?> unselectAllButton = new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.NONE_FOUR_SLOT_ICON)
            .onMousePressed(button -> {
                if (button == 0) {
                    for (int i = 0; i < wrapper.getSlots(); i++) {
                        wrapper.setSlotLocked(i, false);
                    }

                    for (BackpackSlotSH syncHandler : (BackpackSlotSH[]) panel.getStorageSlotSH()) {
                        syncHandler.syncToServer(BackpackSlotSH.getId(BackpackSlotSHRegisters.UPDATE_UNSET_SLOT_LOCK));
                    }

                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.backpack.unselect_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        ButtonWidget<?> colorToggleButton = new ButtonWidget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                super.draw(context, widgetTheme);
                int color = 0xFF000000 | getColorForIndex(currentColorIndex);
                GlStateManager.disableDepth();
                GuiDraw.drawRect(5, 5, 10, 10, color);
                GuiDraw.drawRect(4, 4, 12, 1, 0xFF202020);
                GuiDraw.drawRect(4, 15, 12, 1, 0xFF202020);
                GuiDraw.drawRect(4, 4, 1, 12, 0xFF202020);
                GuiDraw.drawRect(15, 4, 1, 12, 0xFF202020);
                GlStateManager.enableDepth();
            }

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                cycleColor(scrollDirection == UpOrDown.UP ? -1 : 1);
                return true;
            }
        };
        colorToggleButton.size(20);
        colorToggleButton.onMousePressed(button -> {
            if (button == 0) {
                cycleColor(1);
                return true;
            } else if (button == 1) {
                cycleColor(-1);
                return true;
            }
            return false;
        });
        colorToggleButton.tooltipStatic(
            t -> t.addLine(IKey.lang("gui.backpack.toggle_color"))
                .addLine(
                    IKey.lang("gui.backpack.toggle_color_detail")
                        .style(IKey.GRAY))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        buttonRow.top(28)
            .child(selectAllButton)
            .child(unselectAllButton)
            .child(colorToggleButton);

        child(buttonRow);

        phantomTabWidget.getTabIcon()
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                tooltip.addLine(IKey.lang("gui.backpack.sorting_settings.tooltip_open_detail"));
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });
        phantomTabWidget.getTabIcon()
            .tooltipAutoUpdate(true);
    }

    public int getCurrentColor() {
        return getColorForIndex(currentColorIndex);
    }

    public static int getColorForIndex(int index) {
        return SLOT_COLORS[Math.floorMod(index, SLOT_COLORS.length)];
    }

    @Override
    public void onInit() {
        getContext().getUISettings()
            .getRecipeViewerSettings()
            .addExclusionArea(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        currentColorIndex = wrapper.getNoSortColorIndex();
    }

    @Override
    public void updateTabState() {
        parentTabWidget.setShowExpanded(!parentTabWidget.isShowExpanded());
        panel.setSortingSettingTabOpened(parentTabWidget.isShowExpanded());
        settingPanel.updateTabState(1);
    }

    private void cycleColor(int delta) {
        currentColorIndex = Math.floorMod(currentColorIndex + delta, SLOT_COLORS.length);
        wrapper.setNoSortColorIndex(currentColorIndex);
        panel.getStorageSH()
            .syncToServer(
                BackpackSH.getId(BackpackSHRegisters.UPDATE_SET_NO_SORT_COLOR),
                buffer -> buffer.writeInt(currentColorIndex));
    }
}
