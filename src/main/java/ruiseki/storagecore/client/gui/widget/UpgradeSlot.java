package ruiseki.storagecore.client.gui.widget;

import static ruiseki.okcore.client.mui.OKCGuiTextures.EMPTY_UPGRADE;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.storagecore.api.IStorageWrapper;
// TODO Show Upgrade Conflict

public class UpgradeSlot extends ItemSlot {

    public static final int ERROR_SLOT_COLOR = 0xAAB02E26;

    private final IStorageWrapper wrapper;
    private final StoragePanel panel;
    private final int index;

    public UpgradeSlot(IStorageWrapper wrapper, StoragePanel panel, int index) {
        this.wrapper = wrapper;
        this.panel = panel;
        this.index = index;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawBackground(context, widgetTheme);
        if (context != null && getSlot().getStack() == null) {
            EMPTY_UPGRADE.draw(context, 1, 1, 16, 16, widgetTheme.getTheme());
        }
    }
}
