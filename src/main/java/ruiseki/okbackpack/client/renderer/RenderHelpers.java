package ruiseki.okbackpack.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.utils.GlStateManager;

import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okcore.helper.LangHelpers;

public class RenderHelpers {

    public static final int ERROR_BACKGROUND_COLOR = 0xF0100010;
    public static final int ERROR_BORDER_COLOR = 0xFFB02E26;
    public static final int ERROR_TEXT_COLOR = 0xB02E26;

    public enum RenderType {

        BODY,

        HEAD;
    }

    public static void rotateIfSneaking(EntityPlayer player) {
        if (player.isSneaking()) {
            applySneakingRotation();
        }
    }

    public static void applySneakingRotation() {
        GL11.glRotatef(28.65F, 1F, 0F, 0F);
    }

    public static void translateToChest() {
        GL11.glTranslatef(0F, 0.9F, 0F);
    }

    public static void translateToHead(EntityPlayer player) {
        GL11.glTranslated(
            0,
            (player != Minecraft.getMinecraft().thePlayer ? 1.62F : 0F) - player.getDefaultEyeHeight()
                + (player.isSneaking() ? 0.0625 : 0),
            0);
    }

    public static void translateToLeftArm() {
        GL11.glTranslatef(0.35F, 1.2F, 0F);
    }

    public static void translateToRightArm() {
        GL11.glTranslatef(-0.35F, 1.2F, 0F);
    }

    public static void translateToLegs() {
        GL11.glTranslatef(0F, 0.5F, 0F);
    }

    public static void translateToBoots() {
        GL11.glTranslatef(0F, 0.1F, 0F);
    }

    public static TextureManager engine() {
        return Minecraft.getMinecraft().renderEngine;
    }

    public static void bindTexture(String string) {
        engine().bindTexture(new ResourceLocation(string));
    }

    public static void bindTexture(ResourceLocation tex) {
        engine().bindTexture(tex);
    }

    public static void renderErrorOverlay(BackpackPanel panel, float partialTicks) {
        panel.updateActiveError(partialTicks);
        if (panel.activeError == null || panel.activeError.getErrorLangKey() == null) return;

        String errorText = LangHelpers.localize(panel.activeError.getErrorLangKey(), panel.activeError.getErrorArgs());
        var minecraft = Minecraft.getMinecraft();
        var font = minecraft.fontRenderer;
        int textWidth = font.getStringWidth(errorText);

        int panelWidth = panel.resizer()
            .getArea().width;
        int panelHeight = panel.resizer()
            .getArea().height;

        int padding = 4;
        int boxWidth = textWidth + padding * 2;
        int boxHeight = font.FONT_HEIGHT + padding * 2;
        int boxX = (panelWidth - boxWidth) / 2;
        int boxY = panelHeight - 90;

        GlStateManager.disableDepth();
        GuiDraw.drawRect(boxX, boxY, boxWidth, boxHeight, ERROR_BORDER_COLOR);
        GuiDraw.drawRect(boxX + 1, boxY + 1, boxWidth - 2, boxHeight - 2, ERROR_BACKGROUND_COLOR);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        font.drawStringWithShadow(errorText, boxX + padding, boxY + padding, ERROR_TEXT_COLOR);
        GlStateManager.enableDepth();
    }

    public static float getCurrentTick(float partialTicks) {
        var minecraft = Minecraft.getMinecraft();
        return minecraft.theWorld != null ? minecraft.theWorld.getTotalWorldTime() + partialTicks : 0;
    }
}
