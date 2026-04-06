package ruiseki.okbackpack.client.gui.widget;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Row;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.block.BackpackSettingPanel;
import ruiseki.okbackpack.common.block.BackpackWrapper;

public class BackpackSettingWidget extends ExpandedTabWidget {

    private final BackpackPanel panel;
    private final BackpackWrapper wrapper;
    private final BackpackSettingPanel settingPanel;
    private final TabWidget parentTabWidget;

    private static final List<CyclicVariantButtonWidget.Variant> KEEP_TAB_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.keep_tab"), OKBGuiTextures.KEEP_TAB_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.not_keep_tab"),
            OKBGuiTextures.NOT_KEEP_TAB_ICON));

    private static final List<CyclicVariantButtonWidget.Variant> LOCK_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.lock_backpack"),
            OKBGuiTextures.LOCK_BACKPACK_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.unlock_backpack"),
            OKBGuiTextures.UNLOCK_BACKPACK_ICON));

    public BackpackSettingWidget(BackpackPanel panel, BackpackSettingPanel settingPanel, TabWidget parentTabWidget) {
        super(2, OKBGuiTextures.BACKPACK_ICON, "gui.backpack.backpack_settings", 80, TabWidget.ExpandDirection.RIGHT);

        this.panel = panel;
        this.wrapper = panel.wrapper;
        this.settingPanel = settingPanel;
        this.parentTabWidget = parentTabWidget;

        Row buttonRow = (Row) new Row().leftRel(0.5f)
            .height(20)
            .coverChildrenWidth()
            .childPadding(2);

        CyclicVariantButtonWidget tabButton = new CyclicVariantButtonWidget(
            KEEP_TAB_VARIANTS,
            wrapper.keepTab ? 0 : 1,
            (index) -> {
                wrapper.keepTab = index == 0;
                updateWrapper();
            });

        CyclicVariantButtonWidget lockButton = new CyclicVariantButtonWidget(
            LOCK_VARIANTS,
            wrapper.lockBackpack ? 0 : 1,
            (index) -> {
                wrapper.lockBackpack = index == 0;
                updateWrapper();
            });

        buttonRow.top(28)
            .child(tabButton)
            .child(lockButton);

        child(buttonRow);
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
        settingPanel.updateTabState(0);
    }

    private void updateWrapper() {
        this.panel.backpackSyncHandler.syncToServer(BackpackSH.getId(BackpackSHRegisters.UPDATE_SETTING), buffer -> {
            buffer.writeBoolean(wrapper.lockBackpack);
            buffer.writeStringToBuffer(
                panel.player.getUniqueID()
                    .toString());
            buffer.writeBoolean(wrapper.keepTab);
        });
    }
}
