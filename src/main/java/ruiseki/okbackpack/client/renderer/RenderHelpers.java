package ruiseki.okbackpack.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RenderHelpers {

    public static enum RenderType {

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
}
