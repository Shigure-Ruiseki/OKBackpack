package ruiseki.okbackpack.client.gui.widget;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Row;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSH;
import ruiseki.okbackpack.client.gui.widget.TabWidget.ExpandDirection;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.block.BackpackSettingPanel;

public class MemorySettingWidget extends ExpandedTabWidget {

    private static final List<CyclicVariantButtonWidget.Variant> RESPECT_NBT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.ignore_nbt"), OKBGuiTextures.IGNORE_NBT_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.match_nbt"), OKBGuiTextures.MATCH_NBT_ICON));

    private final BackpackPanel panel;
    private final BackpackSettingPanel settingPanel;
    private final TabWidget parentTabWidget;

    private final CyclicVariantButtonWidget respectNBTButton;

    public MemorySettingWidget(BackpackPanel panel, BackpackSettingPanel settingPanel, TabWidget parentTabWidget) {
        super(2, OKBGuiTextures.BRAIN_ICON, "gui.backpack.memory_settings", 80, ExpandDirection.RIGHT);

        this.panel = panel;
        this.settingPanel = settingPanel;
        this.parentTabWidget = parentTabWidget;

        Row buttonRow = (Row) new Row().leftRel(0.5f)
            .height(20)
            .coverChildrenWidth()
            .childPadding(2);

        ButtonWidget<?> memorizeAllButton = new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.ALL_FOUR_SLOT_ICON)
            .onMousePressed(button -> {
                if (button == 0) {
                    var wrapper = panel.wrapper;

                    for (int i = 0; i < wrapper.getSlots(); i++) {
                        wrapper.setMemoryStack(i, panel.shouldMemorizeRespectNBT);
                    }

                    for (BackpackSlotSH syncHandler : panel.backpackSlotSyncHandlers) {
                        syncHandler.syncToServer(
                            BackpackSlotSH.UPDATE_SET_MEMORY_STACK,
                            buf -> buf.writeBoolean(panel.isMemorySettingTabOpened));
                    }

                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.backpack.memorize_all"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        ButtonWidget<?> unmemorizeAllButton = new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.NONE_FOUR_SLOT_ICON)
            .onMousePressed(button -> {
                if (button == 0) {
                    var wrapper = panel.wrapper;

                    for (int i = 0; i < wrapper.getSlots(); i++) {
                        wrapper.unsetMemoryStack(i);
                    }

                    for (BackpackSlotSH syncHandler : panel.backpackSlotSyncHandlers) {
                        syncHandler.syncToServer(BackpackSlotSH.UPDATE_UNSET_MEMORY_STACK);
                    }

                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.backpack.unmemorize_all"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        respectNBTButton = new CyclicVariantButtonWidget(
            RESPECT_NBT_VARIANTS,
            index -> this.panel.shouldMemorizeRespectNBT = (index != 0));

        buttonRow.top(28)
            .child(memorizeAllButton)
            .child(unmemorizeAllButton)
            .child(respectNBTButton);

        child(buttonRow);
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
        panel.isMemorySettingTabOpened = parentTabWidget.isShowExpanded();
        settingPanel.updateTabState(1);
    }

}
