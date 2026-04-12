package ruiseki.okbackpack.client.gui.widget;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Row;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.TabWidget.ExpandDirection;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.block.BackpackSettingPanel;

public class MemorySettingWidget extends ExpandedTabWidget {

    private static final List<CyclicVariantButtonWidget.Variant> RESPECT_NBT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.ignore_nbt"), OKBGuiTextures.IGNORE_NBT_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.match_nbt"), OKBGuiTextures.MATCH_NBT_ICON));

    private final IStoragePanel<?> panel;
    private final IStorageWrapper wrapper;
    private final BackpackSettingPanel settingPanel;
    private final TabWidget parentTabWidget;

    private final CyclicVariantButtonWidget respectNBTButton;

    public MemorySettingWidget(IStoragePanel<?> panel, BackpackSettingPanel settingPanel, TabWidget parentTabWidget) {
        super(2, OKBGuiTextures.BRAIN_ICON, "gui.backpack.memory_settings", 80, ExpandDirection.RIGHT);

        this.panel = panel;
        this.wrapper = panel.getWrapper();
        this.settingPanel = settingPanel;
        this.parentTabWidget = parentTabWidget;

        Row buttonRow = (Row) new Row().leftRel(0.5f)
            .height(20)
            .coverChildrenWidth()
            .childPadding(0);

        ButtonWidget<?> selectAllButton = new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.ALL_FOUR_SLOT_ICON)
            .onMousePressed(button -> {
                if (button == 0) {
                    for (int i = 0; i < wrapper.getSlots(); i++) {
                        wrapper.setMemoryStack(i, panel.shouldMemorizeRespectNBT());
                    }

                    for (BackpackSlotSH syncHandler : (BackpackSlotSH[]) panel.getStorageSlotSH()) {
                        syncHandler.syncToServer(
                            BackpackSlotSH.getId(BackpackSlotSHRegisters.UPDATE_SET_MEMORY_STACK),
                            buf -> buf.writeBoolean(panel.shouldMemorizeRespectNBT()));
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
                        wrapper.unsetMemoryStack(i);
                    }

                    for (BackpackSlotSH syncHandler : (BackpackSlotSH[]) panel.getStorageSlotSH()) {
                        syncHandler
                            .syncToServer(BackpackSlotSH.getId(BackpackSlotSHRegisters.UPDATE_UNSET_MEMORY_STACK));
                    }

                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.backpack.unselect_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        respectNBTButton = new CyclicVariantButtonWidget(
            RESPECT_NBT_VARIANTS,
            index -> this.panel.setShouldMemorizeRespectNBT(index != 0));

        buttonRow.top(28)
            .child(selectAllButton)
            .child(unselectAllButton)
            .child(respectNBTButton);

        child(buttonRow);

        phantomTabWidget.getTabIcon()
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                tooltip.addLine(IKey.lang("gui.backpack.memory_settings.tooltip_open_detail"));
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });

        phantomTabWidget.getTabIcon()
            .tooltipAutoUpdate(true);
    }

    public boolean isRespectNBT() {
        return respectNBTButton.getIndex() != 0;
    }

    @Override
    public void onInit() {
        getContext().getUISettings()
            .getRecipeViewerSettings()
            .addExclusionArea(this);
    }

    @Override
    public void updateTabState() {
        parentTabWidget.setShowExpanded(!parentTabWidget.isShowExpanded());
        panel.setMemorySettingTabOpened(parentTabWidget.isShowExpanded());
        settingPanel.updateTabState(2);
    }

}
