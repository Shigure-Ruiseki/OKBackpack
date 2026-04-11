package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.text.NumberFormat;
import java.util.function.IntSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.slot.UpgradeSlot;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.item.tank.TankUpgradeWrapper;
import ruiseki.okbackpack.common.network.PacketTankClick;
import ruiseki.okcore.helper.LangHelpers;

public class TankSlotWidget extends Widget<TankSlotWidget> implements Interactable {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private static final int BAR_WIDTH = 18;
    private static final int OVERLAY_HEIGHT = 18;
    private static final ResourceLocation BLOCK_TEXTURE = new ResourceLocation("textures/atlas/blocks.png");

    private final int slotIndex;
    private final int tankCount;
    private final BackpackPanel panel;

    private IntSupplier fluidAmountSupplier;
    private IntSupplier tankCapacitySupplier;
    private IntSupplier fluidIdSupplier;

    public TankSlotWidget(int slotIndex, TankUpgradeWrapper wrapper, int tankCount, BackpackPanel panel) {
        this.slotIndex = slotIndex;
        this.tankCount = tankCount;
        this.panel = panel;
        this.fluidAmountSupplier = () -> wrapper.getContents() != null ? wrapper.getContents().amount : 0;
        this.tankCapacitySupplier = wrapper::getTankCapacity;
        this.fluidIdSupplier = () -> wrapper.getContents() != null ? wrapper.getContents()
            .getFluidID() : -1;
        width(BAR_WIDTH);
        heightRel(1f);
        tooltipAutoUpdate(true);
        tooltipDynamic(this::buildTooltip);
    }

    public void setTankSuppliers(IntSupplier fluidAmount, IntSupplier tankCapacity, IntSupplier fluidId) {
        this.fluidAmountSupplier = fluidAmount;
        this.tankCapacitySupplier = tankCapacity;
        this.fluidIdSupplier = fluidId;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        int visibleHeight = getArea().height;

        // 1. Background - top section
        OKBGuiTextures.TANK_BAR_TOP
            .draw(context, 0, 0, BAR_WIDTH, Math.min(OVERLAY_HEIGHT, visibleHeight), widgetTheme.getTheme());

        // 2. Background - middle sections (repeated to fill between top and bottom)
        int yOffset = OVERLAY_HEIGHT;
        int bottomStart = visibleHeight - OVERLAY_HEIGHT;
        while (yOffset < bottomStart) {
            int drawHeight = Math.min(OVERLAY_HEIGHT, bottomStart - yOffset);
            OKBGuiTextures.TANK_BAR_MIDDLE.draw(context, 0, yOffset, BAR_WIDTH, drawHeight, widgetTheme.getTheme());
            yOffset += drawHeight;
        }

        // 3. Background - bottom section
        if (visibleHeight > OVERLAY_HEIGHT) {
            OKBGuiTextures.TANK_BAR_BOTTOM
                .draw(context, 0, visibleHeight - OVERLAY_HEIGHT, BAR_WIDTH, OVERLAY_HEIGHT, widgetTheme.getTheme());
        }

        // 4. Render fluid fill
        renderFluid(context, visibleHeight);

        // 5. Overlay tiled vertically (drawn over fluid for grid effect)
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 100);
        int overlayTiles = visibleHeight / OVERLAY_HEIGHT;
        for (int i = 0; i < overlayTiles; i++) {
            OKBGuiTextures.TANK_OVERLAY
                .draw(context, 1, i * OVERLAY_HEIGHT, 16, OVERLAY_HEIGHT, widgetTheme.getTheme());
        }
        GL11.glPopMatrix();

        // 6. Error highlight overlay (when stack upgrade removal would overflow tank)
        if (panel != null && panel.isSlotInConflict(slotIndex)) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0, 0, 200);
            com.cleanroommc.modularui.drawable.GuiDraw
                .drawRect(1, 1, BAR_WIDTH - 2, visibleHeight - 2, UpgradeSlot.ERROR_SLOT_COLOR);
            GL11.glPopMatrix();
        }
    }

    private void renderFluid(ModularGuiContext context, int visibleHeight) {
        int fluidAmount = fluidAmountSupplier.getAsInt();
        int capacity = tankCapacitySupplier.getAsInt();
        int fluidId = fluidIdSupplier.getAsInt();

        if (fluidAmount <= 0 || capacity <= 0 || fluidId < 0) return;

        Fluid fluid = FluidRegistry.getFluid(fluidId);
        if (fluid == null) return;

        IIcon icon = fluid.getStillIcon();
        if (icon == null) return;

        int displayLevel = (int) ((visibleHeight - 2) * ((float) fluidAmount / capacity));
        if (displayLevel <= 0) return;

        int color = fluid.getColor();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(BLOCK_TEXTURE);
        GL11.glColor4f(r, g, b, 1f);

        int startY = 1 + visibleHeight - 2 - displayLevel;
        int endY = 1 + visibleHeight - 2;
        int x = 1;
        int width = 16;

        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();

        int iconHeight = icon.getIconHeight();

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        int currentY = startY;
        while (currentY < endY) {
            int drawHeight = Math.min(iconHeight, endY - currentY);
            float vEnd = minV + (maxV - minV) * ((float) drawHeight / iconHeight);

            tessellator.addVertexWithUV(x, currentY + drawHeight, 0, minU, vEnd);
            tessellator.addVertexWithUV(x + width, currentY + drawHeight, 0, maxU, vEnd);
            tessellator.addVertexWithUV(x + width, currentY, 0, maxU, minV);
            tessellator.addVertexWithUV(x, currentY, 0, minU, minV);

            currentY += drawHeight;
        }

        tessellator.draw();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        // Handle clicking the tank with a fluid container in hand
        if (mouseButton == 0) {
            // This will be handled by the sync handler trigger
            return handleTankClick();
        }
        return Result.IGNORE;
    }

    private Result handleTankClick() {
        // Send packet to server for tank cursor interaction
        OKBackpack.instance.getPacketHandler()
            .sendToServer(new PacketTankClick(slotIndex));
        return Result.SUCCESS;
    }

    private void buildTooltip(RichTooltip tooltip) {
        int fluidAmount = fluidAmountSupplier.getAsInt();
        int capacity = tankCapacitySupplier.getAsInt();
        int fluidId = fluidIdSupplier.getAsInt();

        tooltip.clearText();

        if (fluidId >= 0 && fluidAmount > 0) {
            Fluid fluid = FluidRegistry.getFluid(fluidId);
            if (fluid != null) {
                FluidStack fs = new FluidStack(fluid, fluidAmount);
                tooltip.addLine(IKey.str(fs.getLocalizedName()));
            }
        }

        String amount = NUMBER_FORMAT.format(fluidAmount);
        String max = NUMBER_FORMAT.format(capacity);
        tooltip.addLine(LangHelpers.localize("tooltip.backpack.tank_contents", amount, max));
        tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
    }
}
