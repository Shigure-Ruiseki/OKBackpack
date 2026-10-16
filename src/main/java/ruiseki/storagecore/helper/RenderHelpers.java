package ruiseki.storagecore.helper;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.utils.GlStateManager;

import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.upgrades.UpgradeSlotChangeResult;

public class RenderHelpers {

    public static final int ERROR_BACKGROUND_COLOR = 0xF0100010;
    public static final int ERROR_BORDER_COLOR = 0xFFB02E26;
    public static final int ERROR_TEXT_COLOR = 0xFFB02E26;

    public enum RenderType {
        BODY,
        HEAD
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
                + (player.isSneaking() ? 0.0625D : 0D),
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

    public static void bindTexture(String texture) {
        engine().bindTexture(new ResourceLocation(texture));
    }

    public static void bindTexture(ResourceLocation texture) {
        engine().bindTexture(texture);
    }

    public static void renderErrorOverlay(StoragePanel panel) {
        Optional<UpgradeSlotChangeResult> optional = panel.getErrorUpgradeSlotChangeResult();
        if (optional.isEmpty()) return;

        UpgradeSlotChangeResult result = optional.get();

        Optional<String> message = result.getErrorMessage();
        if (message.isEmpty()) return;

        String errorText = message.get();

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc.fontRenderer;

        int padding = 4;
        int boxWidth = font.getStringWidth(errorText) + padding * 2;
        int boxHeight = font.FONT_HEIGHT + padding * 2;

        int panelWidth = panel.resizer()
            .getArea().width;
        int panelHeight = panel.resizer()
            .getArea().height;

        int boxX = (panelWidth - boxWidth) / 2;
        int boxY = panelHeight - 90;

        GlStateManager.disableDepth();

        GuiDraw.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, ERROR_BORDER_COLOR);

        GuiDraw.drawRect(boxX + 1, boxY + 1, boxX + boxWidth - 1, boxY + boxHeight - 1, ERROR_BACKGROUND_COLOR);

        GlStateManager.enableTexture2D();
        GlStateManager.color(1F, 1F, 1F, 1F);

        font.drawStringWithShadow(errorText, boxX + padding, boxY + padding, ERROR_TEXT_COLOR);

        GlStateManager.enableDepth();
    }

    public static float getCurrentTick(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();

        return minecraft.theWorld != null ? minecraft.theWorld.getTotalWorldTime() + partialTicks : 0F;
    }
}
