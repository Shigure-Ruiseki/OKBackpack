
package ruiseki.backpack.helpers;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.backpack.helpers.InteractionHelpers.InteractionResult;

@SideOnly(Side.CLIENT)
public final class InteractionRenderHelpers {

    private static final int GREEN = 0xFF55FF55;
    private static final int YELLOW = 0xFFFFFF55;

    private InteractionRenderHelpers() {}

    public static InteractionResult getRenderableResult(EntityPlayer player, Slot slot, ItemStack cursorStack) {

        InteractionResult result = InteractionHelpers.analyze(player, slot, cursorStack);

        return result.canRenderOverlay() ? result : InteractionResult.NONE;
    }

    public static void renderOverlay(FontRenderer font, EntityPlayer player, ItemStack cursorStack, Slot slot, int x,
        int y) {

        renderOverlayAt(font, getRenderableResult(player, slot, cursorStack), x, y);
    }

    public static void renderOverlayAt(FontRenderer font, InteractionResult result, int x, int y) {

        if (font == null || result == null || !result.canRenderOverlay()) {
            return;
        }

        boolean minus = result.symbol == InteractionResult.Symbol.MINUS;
        String symbol = minus ? "-" : "+";

        int color = result.state == InteractionResult.State.GREEN ? GREEN : YELLOW;

        int drawX = minus ? x + 1 : x + 15 - font.getStringWidth(symbol);
        int drawY = minus ? y : y + 16 - font.FONT_HEIGHT;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        GL11.glTranslatef(0, 0, 200);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        font.drawStringWithShadow(symbol, drawX, drawY, color);

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
