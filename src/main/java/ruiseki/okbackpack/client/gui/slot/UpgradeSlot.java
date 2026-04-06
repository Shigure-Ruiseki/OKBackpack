package ruiseki.okbackpack.client.gui.slot;

import static ruiseki.okcore.client.OKCGuiTextures.EMPTY_UPGRADE;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.common.block.BackpackPanel;

public class UpgradeSlot extends ItemSlot {

    public static final int ERROR_SLOT_COLOR = 0xAAB02E26;

    private final BackpackPanel panel;
    private final int slotIndex;

    public UpgradeSlot(BackpackPanel panel, int slotIndex) {
        this.panel = panel;
        this.slotIndex = slotIndex;
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
        if (shouldHighlightConflict()) {
            GlStateManager.disableDepth();
            GuiDraw.drawRect(1, 1, 16, 16, ERROR_SLOT_COLOR);
            GlStateManager.enableDepth();
        }
    }

    private boolean shouldHighlightConflict() {
        return panel.isSlotInConflict(slotIndex);
    }
}
