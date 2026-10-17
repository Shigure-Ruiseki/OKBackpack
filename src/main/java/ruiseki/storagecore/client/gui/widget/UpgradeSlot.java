package ruiseki.storagecore.client.gui.widget;

import static ruiseki.okcore.client.mui.OKCGuiTextures.EMPTY_UPGRADE;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.storagecore.api.IStorageWrapper;

public class UpgradeSlot extends ItemSlot {

    public static final int ERROR_SLOT_COLOR = 0xAAB02E26;

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;
    private final int slotIndex;

    public UpgradeSlot(IStorageWrapper storageWrapper, StoragePanel storagePanel, int slotIndex) {
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.slotIndex = slotIndex;
    }

    public int getIndex() {
        return this.slotIndex;
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawBackground(context, widgetTheme);
        if (context != null && getSlot().getStack() == null) {
            EMPTY_UPGRADE.draw(context, 1, 1, 16, 16, widgetTheme.getTheme());
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        if (this.shouldHighlightConflict()) {
            GlStateManager.disableDepth();
            GuiDraw.drawRect(1, 1, 16, 16, ERROR_SLOT_COLOR);
            GlStateManager.enableDepth();
        }
    }

    public boolean shouldHighlightConflict() {
        return this.getStoragePanel()
            .getErrorUpgradeSlotChangeResult()
            .map(
                result -> result.getErrorUpgradeSlots()
                    .contains(this.getIndex()))
            .orElse(false);
    }
}
