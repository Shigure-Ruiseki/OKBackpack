package ruiseki.backpack.client.gui.widget;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.client.gui.widget.setting.main.BackpackMainSettingTab;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSettingPanel;
import ruiseki.okstoragecore.client.gui.widget.Tab;

public class BackpackSettingPanel extends StorageSettingPanel {

    public BackpackSettingPanel(IStorageWrapper storageWrapper, StoragePanel storagePanel) {
        super(storageWrapper, storagePanel);
    }

    @Override
    protected void addGlobalTab() {
        this.globalTab = new Tab(1).tooltipStatic(
            tooltip -> tooltip.addLine(IKey.lang("gui.okbackpack.setting.backpack.tooltip"))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        // Change to other icon
        this.globalTab.setTabIcon(OKBGuiTextures.BACKPACK_ICON);
        this.globalTab.setExpandedWidget(
            new BackpackMainSettingTab(this.getStorageWrapper(), this.getStoragePanel(), this, this.getGlobalTab()));
        this.tabContainer.addChild(this.globalTab, -1);
    }

    @Override
    public IBackpackWrapper getStorageWrapper() {
        return (IBackpackWrapper) super.getStorageWrapper();
    }

    @Override
    public BackpackPanel getStoragePanel() {
        return (BackpackPanel) super.getStoragePanel();
    }
}
