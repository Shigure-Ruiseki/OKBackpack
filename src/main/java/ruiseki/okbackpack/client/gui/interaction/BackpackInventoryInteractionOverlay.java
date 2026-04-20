package ruiseki.okbackpack.client.gui.interaction;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;

public final class BackpackInventoryInteractionOverlay {

    private static final int GREEN_COLOR = 0xFF55FF55;
    private static final int YELLOW_COLOR = 0xFFFFFF55;

    private BackpackInventoryInteractionOverlay() {}

    public static BackpackInventoryInteractionResult getRenderableResult(EntityPlayer player, Slot slot,
        ItemStack cursorStack) {
        if (!ModConfig.enableBackpackInventoryInteraction) {
            return BackpackInventoryInteractionResult.NONE;
        }

        BackpackInventoryInteractionResult result = BackpackInventoryInteractionAnalyzer
            .analyze(player, slot, cursorStack);
        return result.canRenderOverlay() ? result : BackpackInventoryInteractionResult.NONE;
    }

    public static void renderOverlay(FontRenderer fontRenderer, EntityPlayer player, Slot slot, ItemStack cursorStack) {
        BackpackInventoryInteractionResult result = getRenderableResult(player, slot, cursorStack);
        renderOverlayAt(
            fontRenderer,
            result,
            slot == null ? 0 : slot.xDisplayPosition,
            slot == null ? 0 : slot.yDisplayPosition);
    }

    public static void renderOverlayAt(FontRenderer fontRenderer, BackpackInventoryInteractionResult result, int slotX,
        int slotY) {
        if (fontRenderer == null || result == null || !result.canRenderOverlay()) {
            return;
        }

        String symbol = result.getOverlaySymbol() == BackpackInventoryInteractionResult.OverlaySymbol.PLUS ? "+" : "-";
        int color = result.getOverlayState() == BackpackInventoryInteractionResult.OverlayState.GREEN ? GREEN_COLOR
            : YELLOW_COLOR;
        int x = result.getOverlaySymbol() == BackpackInventoryInteractionResult.OverlaySymbol.MINUS ? slotX + 1
            : slotX + 16 - fontRenderer.getStringWidth(symbol) - 1;
        int y = result.getOverlaySymbol() == BackpackInventoryInteractionResult.OverlaySymbol.MINUS ? slotY
            : slotY + 16 - fontRenderer.FONT_HEIGHT;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, 0.0f, 200.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        fontRenderer.drawStringWithShadow(symbol, x, y, color);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static List<String> buildTooltipLines(EntityPlayer player, Slot slot, ItemStack cursorStack) {
        if (!ModConfig.enableBackpackInventoryInteraction) {
            return Collections.emptyList();
        }

        BackpackInventoryInteractionResult result = getRenderableResult(player, slot, cursorStack);
        if (!result.canRenderOverlay()) {
            return Collections.emptyList();
        }

        BackpackWrapper wrapper = BackpackEntityHelpers.getInteractionWrapper(player, result.getBackpackStack());
        if (wrapper == null || player == null || !wrapper.canPlayerAccess(player.getUniqueID())) {
            return Collections.emptyList();
        }

        return BackpackInventoryInteractionTooltipHelpers
            .buildInteractionTooltipLines(result.getBackpackStack(), wrapper);
    }

    public static void renderTooltip(FontRenderer fontRenderer, List<String> lines, int mouseX, int mouseY,
        TooltipFallback fallback) {
        if (fontRenderer == null || lines == null || lines.isEmpty()) {
            return;
        }

        if (tryRenderCodeChickenTooltip(fontRenderer, mouseX, mouseY, lines)) {
            return;
        }

        fallback.render(lines, mouseX, mouseY);
    }

    private static boolean tryRenderCodeChickenTooltip(FontRenderer fontRenderer, int mouseX, int mouseY,
        List<String> lines) {
        if (!Mods.CodeChickenCore.isModLoaded()) {
            return false;
        }

        try {
            Class<?> guiDrawClass = Class.forName("codechicken.lib.gui.GuiDraw");
            Method drawMultilineTip = guiDrawClass
                .getMethod("drawMultilineTip", FontRenderer.class, int.class, int.class, List.class);
            drawMultilineTip.invoke(null, fontRenderer, mouseX + 12, mouseY - 12, lines);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @FunctionalInterface
    public interface TooltipFallback {

        void render(List<String> lines, int x, int y);
    }
}
