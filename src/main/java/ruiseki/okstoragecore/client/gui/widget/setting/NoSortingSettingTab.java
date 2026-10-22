package ruiseki.okstoragecore.client.gui.widget.setting;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ColorPickerDialog;
import com.cleanroommc.modularui.widgets.layout.Flow;

import cpw.mods.fml.common.Optional;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.compat.bogosorter.BackpackBogoSorterClientCompat;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSettingPanel;
import ruiseki.okstoragecore.client.gui.widget.Tab;
import ruiseki.okstoragecore.settings.nosort.NoSortSettingsCategory;

public class NoSortingSettingTab extends SettingTab {

    private int currentColor = NoSortSettingsCategory.DEFAULT_COLOR;

    public NoSortingSettingTab(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        StorageSettingPanel settingPanel, Tab parentTab) {
        super(
            2,
            OKBGuiTextures.NO_SORT_ICON,
            "gui.okstoragecore.no_sorting_settings",
            100,
            Tab.ExpandDirection.RIGHT,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);

        ButtonWidget<?> selectAllButton = new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.ALL_FOUR_SLOT_ICON)
            .onMousePressed(button -> {
                if (button == 0) {
                    NoSortSettingsCategory settings = this.getNoSortSettingsCategory();
                    settings.selectSlots(
                        0,
                        this.getStorageWrapper()
                            .getInventoryHandler()
                            .getSlots());
                    this.getStoragePanel()
                        .getStorageSH()
                        .syncToServer(StorageSHRegisters.SET_NO_SORT_ALL_SLOT);
                    refreshBogoSortingContext();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.okstoragecore.select_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        ButtonWidget<?> unselectAllButton = new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.NONE_FOUR_SLOT_ICON)
            .onMousePressed(button -> {
                if (button == 0) {
                    NoSortSettingsCategory settings = this.getNoSortSettingsCategory();
                    settings.unselectAllSlots();
                    this.getStoragePanel()
                        .getStorageSH()
                        .syncToServer(StorageSHRegisters.UNSET_NO_SORT_ALL_SLOT);
                    refreshBogoSortingContext();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.okstoragecore.unselect_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        ButtonWidget<?> colorToggleButton = new ButtonWidget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                super.draw(context, widgetTheme);

                GlStateManager.disableDepth();

                // Preview select color
                GuiDraw.drawRect(5, 5, 10, 10, currentColor);

                // line preview
                GuiDraw.drawRect(4, 4, 12, 1, 0xFF202020);
                GuiDraw.drawRect(4, 15, 12, 1, 0xFF202020);
                GuiDraw.drawRect(4, 4, 1, 12, 0xFF202020);
                GuiDraw.drawRect(15, 4, 1, 12, 0xFF202020);

                GlStateManager.enableDepth();
            }
        };
        IPanelHandler colorPicker = IPanelHandler.simple(
            this.getSettingPanel(),
            (mainPanel,
                player) -> new ColorPickerDialog("no_sort_color_picker", this::updateColor, this.currentColor, true)
                    .setDraggable(true)
                    .relative(this.getSettingPanel())
                    .top(0)
                    .rightRel(1f),
            true);
        colorToggleButton.size(20)
            .onMousePressed(button -> {
                if (button == 0) {
                    colorPicker.openPanel();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.okstoragecore.toggle_color"))
                    .addLine(
                        IKey.lang("gui.okstoragecore.toggle_color_detail")
                            .style(IKey.GRAY))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        this.currentColor = this.getNoSortSettingsCategory()
            .getColor();

        this.child(
            Flow.row()
                .leftRel(0.5f)
                .height(20)
                .top(28)
                .coverChildrenWidth()
                .childPadding(0)
                .child(selectAllButton)
                .child(unselectAllButton)
                .child(colorToggleButton));

        this.getPhantomTab()
            .getTabIcon()
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                tooltip.addLine(IKey.lang("gui.okstoragecore.no_sorting_settings.tooltip_open_detail"));
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            })
            .tooltipAutoUpdate(true);
    }

    public int getCurrentColor() {
        return this.currentColor;
    }

    private void updateColor(int color) {
        this.currentColor = color;
        this.getNoSortSettingsCategory()
            .setColor(this.getCurrentColor());
        this.getStoragePanel()
            .getStorageSH()
            .syncToServer(StorageSHRegisters.SET_NO_SORT_SLOT_COLOR, buffer -> buffer.writeInt(this.getCurrentColor()));
    }

    public NoSortSettingsCategory getNoSortSettingsCategory() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getTypeCategory(NoSortSettingsCategory.class);
    }

    @Optional.Method(modid = "bogosorter")
    private static void refreshBogoSortingContext() {
        BackpackBogoSorterClientCompat.invalidateSortingContext();
    }
}
