package ruiseki.okbackpack.client.renderer;

import static codechicken.lib.gui.GuiDraw.TOOLTIP_HANDLER;
import static codechicken.lib.gui.GuiDraw.getTipLineId;
import static ruiseki.okbackpack.client.gui.OKBGuiTextures.TOGGLE_DISABLE_ICON;
import static ruiseki.okbackpack.client.gui.OKBGuiTextures.TOGGLE_ENABLE_ICON;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.screen.RichTooltipEvent;
import com.github.bsideup.jabel.Desugar;

import codechicken.lib.gui.GuiDraw;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.config.ModConfig;

@SideOnly(Side.CLIENT)
public class BackpackContentHandler {

    public static final int DEFAULT_STACK_WIDTH = 18;
    public static final int COUNT_PADDING = 2;
    public static final int STACK_ROW_HEIGHT = 20;

    public static List<UpgradeInfo> upgradeInfos = new ArrayList<>();
    public static List<ItemStack> sortedContents = new ArrayList<>();
    public static List<String> upgradeTooltipLines = new ArrayList<>();

    public static final GuiDraw.ITooltipLineHandler upgradeHandler = new GuiDraw.ITooltipLineHandler() {

        @Override
        public Dimension getSize() {
            if (upgradeInfos.isEmpty()) return new Dimension(0, 0);
            int width = 0;
            for (UpgradeInfo info : upgradeInfos) {
                width += (info.canBeDisabled ? 4 : 0) + DEFAULT_STACK_WIDTH;
            }
            return new Dimension(width, DEFAULT_STACK_WIDTH);
        }

        @Override
        public void draw(int x, int y) {
            if (upgradeInfos.isEmpty()) return;

            Minecraft mc = Minecraft.getMinecraft();
            RenderItem itemRender = RenderItem.getInstance();

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();

            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            float oldZ = itemRender.zLevel;
            itemRender.zLevel = 200.0F;

            int drawX = x;

            for (UpgradeInfo info : upgradeInfos) {

                if (info.canBeDisabled) {
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);

                    if (info.enabled) {
                        TOGGLE_ENABLE_ICON.draw(drawX, y + 3, 4, 10);
                    } else {
                        TOGGLE_DISABLE_ICON.draw(drawX, y + 3, 4, 10);
                    }

                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_LIGHTING);

                    drawX += 4;
                }

                itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), info.stack, drawX, y);

                drawX += DEFAULT_STACK_WIDTH;
            }

            itemRender.zLevel = oldZ;

            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    };

    public static final GuiDraw.ITooltipLineHandler contentsHandler = new GuiDraw.ITooltipLineHandler() {

        @Override
        public Dimension getSize() {
            if (sortedContents.isEmpty()) return new Dimension(0, 0);
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            int maxPerRow = ModConfig.tooltipMaxItemsPerRow;
            int rows = 1 + (sortedContents.size() - 1) / maxPerRow;

            int itemsOnLine = Math.min(sortedContents.size(), maxPerRow);
            int width = 0;
            for (int i = 0; i < itemsOnLine; i++) {
                int countWidth = font.getStringWidth(abbreviateCount(sortedContents.get(i).stackSize)) + COUNT_PADDING;
                width += Math.max(countWidth, DEFAULT_STACK_WIDTH);
            }
            return new Dimension(width, rows * STACK_ROW_HEIGHT);
        }

        @Override
        public void draw(int x, int y) {
            if (sortedContents.isEmpty()) return;

            Minecraft mc = Minecraft.getMinecraft();
            FontRenderer font = mc.fontRenderer;
            RenderItem itemRender = RenderItem.getInstance();
            int maxPerRow = ModConfig.tooltipMaxItemsPerRow;

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();

            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            float oldZ = itemRender.zLevel;
            itemRender.zLevel = 200.0F;

            int drawX = x;
            for (int i = 0; i < sortedContents.size(); i++) {
                int row = i / maxPerRow;
                int drawY = y + row * STACK_ROW_HEIGHT;

                if (i % maxPerRow == 0 && i > 0) {
                    drawX = x;
                }

                ItemStack stack = sortedContents.get(i);
                String countText = abbreviateCount(stack.stackSize);

                int countWidth = font.getStringWidth(countText) + COUNT_PADDING;
                int stackWidth = Math.max(countWidth, DEFAULT_STACK_WIDTH);
                int xOffset = stackWidth - DEFAULT_STACK_WIDTH;

                int renderX = drawX + xOffset;

                itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, renderX, drawY);

                itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), stack, renderX, drawY, countText);

                drawX += stackWidth;
            }

            itemRender.zLevel = oldZ;

            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    };

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRichTooltipPre(RichTooltipEvent.Pre event) {
        if (upgradeInfos.isEmpty() && sortedContents.isEmpty()) return;

        if (!(event.getTooltip() instanceof RichText richText)) return;
        List<String> lines = richText.getAsStrings();
        boolean hasHandler = false;
        for (String line : lines) {
            if (line != null && line.startsWith(TOOLTIP_HANDLER)) {
                hasHandler = true;
                break;
            }
        }
        if (!hasHandler) return;

        event.setCanceled(true);

        List<String> freshLines = new ArrayList<>();
        int handlerCount = 0;
        for (String line : lines) {
            if (line != null && line.startsWith(TOOLTIP_HANDLER)) {
                if (handlerCount == 0 && !upgradeInfos.isEmpty()) {
                    freshLines.add(TOOLTIP_HANDLER + getTipLineId(upgradeHandler));
                } else {
                    freshLines.add(TOOLTIP_HANDLER + getTipLineId(contentsHandler));
                }
                handlerCount++;
            } else {
                freshLines.add(line);
            }
        }

        GuiDraw
            .drawMultilineTip(Minecraft.getMinecraft().fontRenderer, event.getX() + 12, event.getY() - 12, freshLines);
    }

    public static void reset() {
        upgradeInfos.clear();
        sortedContents.clear();
        upgradeTooltipLines.clear();
    }

    /**
     * Prepares the expanded tooltip data. Called from addInformation() when shift is held.
     */
    public static void prepareContents(BackpackWrapper wrapper) {
        upgradeInfos.clear();
        sortedContents.clear();
        upgradeTooltipLines.clear();

        // Gather upgrades
        Map<Integer, IUpgradeWrapper> wrappers = wrapper.getUpgradeHandler()
            .getSlotWrappers();
        for (IUpgradeWrapper uw : wrappers.values()) {
            boolean canBeDisabled = uw instanceof IToggleable;
            boolean enabled = !canBeDisabled || ((IToggleable) uw).isEnabled();
            upgradeInfos.add(new UpgradeInfo(uw.getUpgradeStack(), canBeDisabled, enabled));
            upgradeTooltipLines.addAll(uw.getTooltipLines());
        }

        // Gather inventory contents - compact and sort by count descending
        sortedContents = getCompactedStacksSortedByCount(wrapper.backpackHandler);
    }

    public static String getUpgradeHandlerLine() {
        return TOOLTIP_HANDLER + getTipLineId(upgradeHandler);
    }

    public static String getContentsHandlerLine() {
        return TOOLTIP_HANDLER + getTipLineId(contentsHandler);
    }

    public static List<ItemStack> getCompactedStacksSortedByCount(BackpackItemStackHandler handler) {
        Map<String, ItemStack> merged = new LinkedHashMap<>();

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack == null) continue;

            String key = stack.getItem()
                .getUnlocalizedName() + ":"
                + stack.getItemDamage();
            if (merged.containsKey(key)) {
                merged.get(key).stackSize += stack.stackSize;
            } else {
                ItemStack copy = stack.copy();
                merged.put(key, copy);
            }
        }

        List<ItemStack> result = new ArrayList<>(merged.values());
        result.sort(
            Comparator.comparingInt((ItemStack s) -> s.stackSize)
                .reversed());
        return result;
    }

    public static String abbreviateCount(int count) {
        if (count < 1000) return String.valueOf(count);

        DecimalFormat df = new DecimalFormat("0.#");
        if (count < 1_000_000) {
            return df.format(count / 1000.0) + "K";
        }
        return df.format(count / 1_000_000.0) + "M";
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Desugar
    public record UpgradeInfo(ItemStack stack, boolean canBeDisabled, boolean enabled) {

    }
}
