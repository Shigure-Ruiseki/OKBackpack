package ruiseki.okbackpack.client.tooltip;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.github.bsideup.jabel.Desugar;
import com.gtnewhorizon.gtnhlib.client.event.RenderTooltipEvent;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;

@SideOnly(Side.CLIENT)
public class BackpackTooltipRenderer {

    private static final int MAX_STACKS_ON_LINE = 9;
    private static final int DEFAULT_STACK_WIDTH = 18;
    private static final int COUNT_PADDING = 2;

    // Static data set by addInformation, read by DrawScreenEvent.Post
    public static boolean shouldRenderItems = false;
    public static List<String> tooltipTextSnapshot = new ArrayList<>();
    public static List<UpgradeInfo> upgradeInfos = new ArrayList<>();
    public static List<ItemStack> sortedContents = new ArrayList<>();
    public static int upgradeSpacerStartIndex = -1;
    public static int inventorySpacerStartIndex = -1;

    // Tooltip position captured from RenderTooltipEvent (matches actual drawHoveringText)
    private static int capturedTooltipX = 0;
    private static int capturedTooltipY = 0;
    private static boolean positionCaptured = false;

    public static void reset() {
        shouldRenderItems = false;
        upgradeInfos.clear();
        sortedContents.clear();
        upgradeSpacerStartIndex = -1;
        inventorySpacerStartIndex = -1;
        tooltipTextSnapshot.clear();
        positionCaptured = false;
    }

    /**
     * Prepares the expanded tooltip data. Called from addInformation() when shift is held.
     */
    public static void prepareContents(BackpackWrapper wrapper) {
        shouldRenderItems = true;
        upgradeInfos.clear();
        sortedContents.clear();

        // Gather upgrades
        Map<Integer, IUpgradeWrapper> wrappers = wrapper.getUpgradeHandler()
            .getSlotWrappers();
        for (IUpgradeWrapper uw : wrappers.values()) {
            boolean canBeDisabled = uw instanceof IToggleable;
            boolean enabled = !canBeDisabled || ((IToggleable) uw).isEnabled();
            upgradeInfos.add(new UpgradeInfo(uw.getUpgradeStack(), canBeDisabled, enabled));
        }

        // Gather inventory contents - compact and sort by count descending
        sortedContents = getCompactedStacksSortedByCount(wrapper.backpackHandler);
    }

    /**
     * Captures the final tooltip text after all mods have added their lines.
     * This runs at LOWEST priority so we get the complete list.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (shouldRenderItems) {
            tooltipTextSnapshot = new ArrayList<>(event.toolTip);
        }
    }

    /**
     * Captures the actual tooltip position from GTNHLib's RenderTooltipEvent.
     * This uses the exact same parameters (event.x/y/font/gui.width/height) that
     * the overwritten drawHoveringText will use, ensuring perfect alignment.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderTooltip(RenderTooltipEvent event) {
        if (!shouldRenderItems || tooltipTextSnapshot.isEmpty()) return;

        FontRenderer font = event.font;
        int mouseX = event.x;
        int mouseY = event.y;
        int screenWidth = event.gui.width;
        int screenHeight = event.gui.height;

        // Compute maxWidth using the same font drawHoveringText will use
        int maxWidth = 0;
        for (String s : tooltipTextSnapshot) {
            int w = font.getStringWidth(s);
            if (w > maxWidth) maxWidth = w;
        }

        // Replicate the exact position algorithm from GTNHLib's drawHoveringText overwrite
        capturedTooltipX = mouseX + 16;
        capturedTooltipY = mouseY - 12;
        int tooltipHeight = 8;
        if (tooltipTextSnapshot.size() > 1) {
            tooltipHeight += 2 + (tooltipTextSnapshot.size() - 1) * 10;
        }

        if (capturedTooltipX + maxWidth > screenWidth) {
            capturedTooltipX -= 28 + maxWidth;
        }
        if (capturedTooltipY + tooltipHeight + 6 > screenHeight) {
            capturedTooltipY = screenHeight - tooltipHeight - 6;
        }

        positionCaptured = true;
    }

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!shouldRenderItems || (upgradeInfos.isEmpty() && sortedContents.isEmpty())) {
            shouldRenderItems = false;
            return;
        }

        GuiScreen gui = event.gui;
        FontRenderer font = gui.mc.fontRenderer;

        List<String> textLines = tooltipTextSnapshot;
        if (textLines.isEmpty() || !positionCaptured) {
            shouldRenderItems = false;
            return;
        }

        int tooltipX = capturedTooltipX;
        int tooltipY = capturedTooltipY;

        // Set up GL state so items render ON TOP of the tooltip background (z=300)
        RenderItem itemRender = RenderItem.getInstance();
        float savedZLevel = itemRender.zLevel;
        itemRender.zLevel = 300.0F;

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 300.0F);

        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_LIGHTING);

        // Walk through lines to find Y positions for spacer areas
        int currentY = tooltipY;
        for (int i = 0; i < textLines.size(); i++) {
            if (i == upgradeSpacerStartIndex) {
                renderUpgrades(gui, tooltipX, currentY - 10);
            }
            if (i == inventorySpacerStartIndex) {
                renderContents(gui, font, tooltipX, currentY - 10);
            }
            if (i == 0) {
                currentY += 2; // extra gap after title line
            }
            currentY += 10;
        }

        // Restore GL state
        GL11.glDisable(GL11.GL_LIGHTING);
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
        itemRender.zLevel = savedZLevel;

        shouldRenderItems = false;
    }

    private void renderUpgrades(GuiScreen gui, int leftX, int topY) {
        if (upgradeInfos.isEmpty()) return;

        Minecraft mc = gui.mc;
        RenderItem itemRender = RenderItem.getInstance();

        int x = leftX;
        for (UpgradeInfo info : upgradeInfos) {
            if (info.canBeDisabled) {
                // Disable depth test for the on/off indicator bar
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                drawRect(x, topY + 3, x + 4, topY + 13, info.enabled ? 0xFF00AA00 : 0xFFAA0000);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LIGHTING);
                x += 4;
            }

            itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), info.stack, x, topY);
            x += DEFAULT_STACK_WIDTH;
        }
    }

    private void renderContents(GuiScreen gui, FontRenderer font, int leftX, int topY) {
        if (sortedContents.isEmpty()) return;

        Minecraft mc = gui.mc;
        RenderItem itemRender = RenderItem.getInstance();

        int x = leftX;
        for (int i = 0; i < sortedContents.size(); i++) {
            int row = i / MAX_STACKS_ON_LINE;
            int y = topY + row * 20;
            if (i % MAX_STACKS_ON_LINE == 0 && i > 0) {
                x = leftX;
            }

            ItemStack stack = sortedContents.get(i);
            String countText = abbreviateCount(stack.stackSize);
            int countWidth = font.getStringWidth(countText) + COUNT_PADDING;
            int stackWidth = Math.max(countWidth, DEFAULT_STACK_WIDTH);
            int xOffset = stackWidth - DEFAULT_STACK_WIDTH;

            itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, x + xOffset, y);
            itemRender
                .renderItemOverlayIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, x + xOffset, y, countText);

            x += stackWidth;
        }
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

    public static int getUpgradeSpacerLines() {
        return upgradeInfos.isEmpty() ? 0 : 2;
    }

    public static int getInventorySpacerLines() {
        if (sortedContents.isEmpty()) return 0;
        int rows = 1 + (sortedContents.size() - 1) / MAX_STACKS_ON_LINE;
        return rows * 2;
    }

    public static String createSpacerLine(FontRenderer font, int targetWidth) {
        StringBuilder sb = new StringBuilder("\u00a7r");
        int spaceWidth = font.getCharWidth(' ');
        if (spaceWidth <= 0) spaceWidth = 4;
        int numSpaces = (targetWidth / spaceWidth) + 1;
        for (int i = 0; i < numSpaces; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static int getRequiredWidth(FontRenderer font) {
        int upgradeWidth = 0;
        for (UpgradeInfo info : upgradeInfos) {
            upgradeWidth += (info.canBeDisabled ? 4 : 0) + DEFAULT_STACK_WIDTH;
        }
        int contentsWidth = 0;
        int itemsOnLine = Math.min(sortedContents.size(), MAX_STACKS_ON_LINE);
        for (int i = 0; i < itemsOnLine; i++) {
            int countWidth = font.getStringWidth(abbreviateCount(sortedContents.get(i).stackSize)) + COUNT_PADDING;
            contentsWidth += Math.max(countWidth, DEFAULT_STACK_WIDTH);
        }
        return Math.max(upgradeWidth, contentsWidth);
    }

    private static void drawRect(int left, int top, int right, int bottom, int color) {
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
