package ruiseki.storagecore.client.gui.widget;

import static ruiseki.okbackpack.common.block.BackpackPanel.LAYERED_TAB_TEXTURE;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import ruiseki.storagecore.api.IStorageWrapper;

public class StorageSettingPanel extends ModularPanel {

    protected final IStorageWrapper storageWrapper;
    protected final StoragePanel storagePanel;

    protected boolean isMemorySettingTabOpened = true;
    protected boolean isSortingSettingTabOpened = false;

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

    public IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public boolean isMemorySettingTabOpened() {
        return this.isMemorySettingTabOpened;
    }

    public boolean isSortingSettingTabOpened() {
        return isSortingSettingTabOpened;
    }
}
