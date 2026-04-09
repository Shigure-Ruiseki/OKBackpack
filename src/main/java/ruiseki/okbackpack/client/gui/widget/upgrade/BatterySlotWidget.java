package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.text.NumberFormat;
import java.util.function.IntSupplier;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.common.item.battery.BatteryUpgradeWrapper;

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
    private IntSupplier energyStoredSupplier;
    private IntSupplier maxEnergySupplier;

    public BatterySlotWidget(int slotIndex, BatteryUpgradeWrapper wrapper) {
        this.slotIndex = slotIndex;
        this.energyStoredSupplier = wrapper::getEnergyStored;
        this.maxEnergySupplier = wrapper::getMaxEnergyStored;
        width(BAR_WIDTH);
        heightRel(1f);
        tooltipAutoUpdate(true);
        tooltipDynamic(this::buildTooltip);
    }

    public void setEnergySuppliers(IntSupplier energyStored, IntSupplier maxEnergy) {
        this.energyStoredSupplier = energyStored;
        this.maxEnergySupplier = maxEnergy;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        int visibleHeight = getArea().height;

        // 1. Background
        OKBGuiTextures.BATTERY_BAR_BACKGROUND.draw(context, 0, 0, BAR_WIDTH, visibleHeight, widgetTheme.getTheme());

        // 2. Overlay tiled vertically (drawn over background, under charge)
        int overlayTiles = visibleHeight / OVERLAY_HEIGHT;
        for (int i = 0; i < overlayTiles; i++) {
            OKBGuiTextures.BATTERY_OVERLAY
                .draw(context, 1, i * OVERLAY_HEIGHT, 16, OVERLAY_HEIGHT, widgetTheme.getTheme());
        }

        // 3. Charge segments with color gradient (bottom-to-top)
        renderCharge(context, widgetTheme, visibleHeight);

        // 4. Connection pieces (top and bottom, drawn last)
        OKBGuiTextures.BATTERY_CONNECTION_TOP.draw(context, 1, 0, 16, 4, widgetTheme.getTheme());
        OKBGuiTextures.BATTERY_CONNECTION_BOTTOM.draw(context, 1, visibleHeight - 4, 16, 4, widgetTheme.getTheme());
    }

    private void renderCharge(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme, int visibleHeight) {
        int energyStored = energyStoredSupplier.getAsInt();
        int maxEnergy = maxEnergySupplier.getAsInt();
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
            OKBGuiTextures.BATTERY_CHARGE_SEGMENT.draw(context, 1, segY, 16, SEGMENT_HEIGHT, widgetTheme.getTheme());
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void buildTooltip(RichTooltip tooltip) {
        String stored = NUMBER_FORMAT.format(energyStoredSupplier.getAsInt());
        String max = NUMBER_FORMAT.format(maxEnergySupplier.getAsInt());
        tooltip.clearText()
            .addLine(IKey.str(stored + " RF / " + max + " RF"))
            .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
    }
}
