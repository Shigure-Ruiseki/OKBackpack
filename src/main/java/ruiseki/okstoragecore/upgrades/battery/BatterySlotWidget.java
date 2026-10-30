package ruiseki.okstoragecore.upgrades.battery;

import java.text.NumberFormat;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okbackpack.client.gui.slot.UpgradeSlot;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class BatterySlotWidget extends Widget<BatterySlotWidget> {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private static final int BAR_WIDTH = 18;
    private static final int BAR_HEIGHT = 54;
    private static final int SEGMENT_HEIGHT = 6;
    private static final int OVERLAY_HEIGHT = 18;

    // Color gradient: bottom (yellow) → top (red), matching SophisticatedCore
    private static final int TOP_BAR_COLOR = 0xff1a1a;
    private static final int BOTTOM_BAR_COLOR = 0xffff40;

    private final int slotIndex;
    private final StoragePanel panel;
    private final DelegatedIntSH energySH;
    private final DelegatedIntSH energyMaxSH;

    public BatterySlotWidget(int slotIndex, BatteryUpgradeWrapper wrapper, StoragePanel panel) {
        this.slotIndex = slotIndex;
        this.panel = panel;
        this.energySH = (DelegatedIntSH) panel.getSyncManager()
            .findSyncHandlerNullable("battery_energy_stored_" + slotIndex, 0);
        this.energyMaxSH = (DelegatedIntSH) panel.getSyncManager()
            .findSyncHandlerNullable("battery_max_energy_" + slotIndex, 0);
        this.width(BAR_WIDTH);
        this.heightRel(1f);
        this.tooltipAutoUpdate(true);
        this.tooltipDynamic(this::buildTooltip);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        int visibleHeight = getArea().height;

        // 1. Background
        OSCTextures.BATTERY_BAR_BACKGROUND.draw(context, 0, 0, BAR_WIDTH, visibleHeight, widgetTheme.getTheme());

        // 2. Overlay tiled vertically (drawn over background, under charge)
        int overlayTiles = visibleHeight / OVERLAY_HEIGHT;
        for (int i = 0; i < overlayTiles; i++) {
            OSCTextures.BATTERY_OVERLAY
                .draw(context, 1, i * OVERLAY_HEIGHT, 16, OVERLAY_HEIGHT, widgetTheme.getTheme());
        }

        // 3. Charge segments with color gradient (bottom-to-top)
        renderCharge(context, widgetTheme, visibleHeight);

        // 4. Connection pieces (top and bottom, drawn last)
        OSCTextures.BATTERY_CONNECTION_TOP.draw(context, 1, 0, 16, 4, widgetTheme.getTheme());
        OSCTextures.BATTERY_CONNECTION_BOTTOM.draw(context, 1, visibleHeight - 4, 16, 4, widgetTheme.getTheme());

        // 5. Error highlight overlay (when stack upgrade removal would overflow battery)
        panel.getErrorUpgradeSlotChangeResult()
            .filter(
                result -> result.getErrorInventoryParts()
                    .contains(this.slotIndex))
            .ifPresent(result -> {
                GL11.glPushMatrix();
                GL11.glTranslatef(0, 0, 200);
                com.cleanroommc.modularui.drawable.GuiDraw
                    .drawRect(1, 1, BAR_WIDTH - 2, visibleHeight - 2, UpgradeSlot.ERROR_SLOT_COLOR);
                GL11.glPopMatrix();
            });
    }

    private void renderCharge(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme, int visibleHeight) {
        int energyStored = energySH.getIntValue();
        int maxEnergy = energyMaxSH.getIntValue();
        if (maxEnergy <= 0 || energyStored <= 0) return;

        int numSegments = visibleHeight / SEGMENT_HEIGHT;
        int displayLevel = (int) (numSegments * ((float) energyStored / maxEnergy));

        int topR = (TOP_BAR_COLOR >> 16) & 0xFF;
        int topG = (TOP_BAR_COLOR >> 8) & 0xFF;
        int topB = TOP_BAR_COLOR & 0xFF;

        int botR = (BOTTOM_BAR_COLOR >> 16) & 0xFF;
        int botG = (BOTTOM_BAR_COLOR >> 8) & 0xFF;
        int botB = BOTTOM_BAR_COLOR & 0xFF;

        for (int i = 0; i < displayLevel; i++) {
            float pct = numSegments > 1 ? (float) i / (numSegments - 1) : 0;
            int r = (int) (botR * (1 - pct) + topR * pct);
            int g = (int) (botG * (1 - pct) + topG * pct);
            int b = (int) (botB * (1 - pct) + topB * pct);

            GL11.glColor4f(r / 255f, g / 255f, b / 255f, 1f);
            int segY = visibleHeight - (i + 1) * SEGMENT_HEIGHT;
            OSCTextures.BATTERY_CHARGE_SEGMENT.draw(context, 1, segY, 16, SEGMENT_HEIGHT, widgetTheme.getTheme());
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void buildTooltip(RichTooltip tooltip) {
        String stored = NUMBER_FORMAT.format(energySH.getIntValue());
        String max = NUMBER_FORMAT.format(energyMaxSH.getIntValue());
        tooltip.clearText()
            .addLine(LangHelpers.localize("gui.okstoragecore.upgrades.battery.tooltip"))
            .addLine(LangHelpers.localize("gui.okstoragecore.upgrades.battery.contents_tooltip", stored, max))
            .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
    }
}
