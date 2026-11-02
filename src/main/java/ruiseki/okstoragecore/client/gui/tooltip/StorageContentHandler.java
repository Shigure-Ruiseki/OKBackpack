package ruiseki.okstoragecore.client.gui.tooltip;

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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okcore.client.gui.tooltip.ITooltipLine;
import ruiseki.okcore.client.gui.tooltip.TooltipLineHandler;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

@SideOnly(Side.CLIENT)
public class StorageContentHandler {

    public static final StorageContentHandler INSTANCE = new StorageContentHandler();

    public static final int DEFAULT_STACK_WIDTH = 18;
    public static final int COUNT_PADDING = 2;
    public static final int STACK_ROW_HEIGHT = 20;
    public static final int UPGRADE_ROW_HEIGHT = 18;
    public static final int SECTION_SPACING = 2;

    public static List<UpgradeInfo> upgradeInfos = new ArrayList<>();
    public static List<ItemStack> sortedContents = new ArrayList<>();
    public static List<String> upgradeTooltipLines = new ArrayList<>();

    public record UpgradeInfo(ItemStack stack, boolean canBeDisabled, boolean enabled) {}

    public static final String RIGHT_CLICK_ADD_KEY = "tooltip.backpack.inventory_interaction.right_click_add";

    public static List<String> prependInteractionLine(List<String> lines) {
        List<String> result = new ArrayList<>();
        result.add(LangHelpers.localize(RIGHT_CLICK_ADD_KEY));
        if (lines != null) result.addAll(lines);
        return result;
    }

    /**
     * Prepares storage render lines and returns the accompanying text lines.
     * Call during addInformation() while Shift is held.
     */
    public static List<String> buildExpandedTooltipLines(IStorageWrapper wrapper) {
        List<String> lines = new ArrayList<>();

        reset();

        if (wrapper == null) return lines;

        prepareContents(wrapper);

        final List<UpgradeInfo> upgrades = new ArrayList<>(upgradeInfos);
        final List<ItemStack> contents = new ArrayList<>(sortedContents);

        boolean hasUpgrades = !upgrades.isEmpty();
        boolean hasContents = !contents.isEmpty();

        if (!hasUpgrades && !hasContents) {
            lines.add("§e" + LangHelpers.localize("tooltip.backpack.contents.empty"));
        } else {
            if (hasUpgrades) {
                lines.addAll(upgradeTooltipLines);
                lines.add("§e" + LangHelpers.localize("tooltip.backpack.contents.upgrades"));

                lines.add(TooltipLineHandler.register(new ITooltipLine() {

                    @Override
                    public int getWidth(FontRenderer font) {
                        return getUpgradeWidth(upgrades);
                    }

                    @Override
                    public int getHeight(FontRenderer font, int width) {
                        return getUpgradeHeight(upgrades, width);
                    }

                    @Override
                    public void draw(FontRenderer font, int x, int y, int mouseX, int mouseY, int width, int height) {
                        renderUpgrades(upgrades, font, x, y, width, height);
                    }
                }));
            }

            if (hasContents) {
                lines.add("§e" + LangHelpers.localize("tooltip.backpack.contents.inventory"));

                lines.add(TooltipLineHandler.register(new ITooltipLine() {

                    @Override
                    public int getWidth(FontRenderer font) {
                        return getContentsWidth(contents, font);
                    }

                    @Override
                    public int getHeight(FontRenderer font, int width) {
                        return getContentsHeight(contents, font, width);
                    }

                    @Override
                    public void draw(FontRenderer font, int x, int y, int mouseX, int mouseY, int width, int height) {
                        renderContents(contents, font, x, y, width, height);
                    }
                }));
            }
        }

        lines.add(
            LangHelpers.localize(
                "tooltip.backpack.inventory_size",
                wrapper.getInventoryHandler()
                    .getSlots()));

        lines.add(
            LangHelpers.localize(
                "tooltip.backpack.upgrade_slots_size",
                wrapper.getUpgradeHandler()
                    .getSlots()));

        return lines;
    }

    public static List<String> buildInteractionTooltipLines(IStorageWrapper wrapper) {
        return prependInteractionLine(buildExpandedTooltipLines(wrapper));
    }

    private static int getUpgradeItemWidth(UpgradeInfo info) {
        return DEFAULT_STACK_WIDTH + (info.canBeDisabled() ? 4 : 0);
    }

    private static int getUpgradeWidth(List<UpgradeInfo> upgrades) {
        int width = 0;

        for (UpgradeInfo info : upgrades) {
            width += getUpgradeItemWidth(info);
        }

        return width;
    }

    private static int getUpgradeRowCount(List<UpgradeInfo> upgrades, int width) {
        if (upgrades.isEmpty()) {
            return 0;
        }

        int availableWidth = Math.max(1, width);
        int rows = 1;
        int rowWidth = 0;

        for (UpgradeInfo info : upgrades) {
            int itemWidth = getUpgradeItemWidth(info);

            if (rowWidth > 0 && rowWidth + itemWidth > availableWidth) {
                rows++;
                rowWidth = 0;
            }

            rowWidth += itemWidth;
        }

        return rows;
    }

    private static int getUpgradeHeight(List<UpgradeInfo> upgrades, int width) {
        return getUpgradeRowCount(upgrades, width) * UPGRADE_ROW_HEIGHT;
    }

    private static int getContentsWidth(List<ItemStack> contents, FontRenderer font) {
        if (contents.isEmpty()) {
            return 0;
        }

        int maxPerRow = Math.max(1, GeneralConfig.tooltipMaxItemsPerRow);

        int maxWidth = 0;
        int rowWidth = 0;
        int itemsInRow = 0;

        for (ItemStack stack : contents) {
            int stackWidth = getContentStackWidth(stack, font);

            if (stackWidth <= 0) {
                continue;
            }

            if (itemsInRow >= maxPerRow) {
                maxWidth = Math.max(maxWidth, rowWidth);
                rowWidth = 0;
                itemsInRow = 0;
            }

            rowWidth += stackWidth;
            itemsInRow++;
        }

        return Math.max(maxWidth, rowWidth);
    }

    private static int getContentsHeight(List<ItemStack> contents, FontRenderer font, int width) {
        return getContentsRowCount(contents, font, width) * STACK_ROW_HEIGHT;
    }

    private static int getContentsRowCount(List<ItemStack> contents, FontRenderer font, int width) {
        if (contents.isEmpty()) {
            return 0;
        }

        int maxPerRow = Math.max(1, GeneralConfig.tooltipMaxItemsPerRow);
        int availableWidth = Math.max(1, width);

        int rows = 1;
        int rowWidth = 0;
        int itemsInRow = 0;

        for (ItemStack stack : contents) {
            int stackWidth = getContentStackWidth(stack, font);

            if (stackWidth <= 0) {
                continue;
            }

            if (itemsInRow > 0 && (itemsInRow >= maxPerRow || rowWidth + stackWidth > availableWidth)) {
                rows++;
                rowWidth = 0;
                itemsInRow = 0;
            }

            rowWidth += stackWidth;
            itemsInRow++;
        }

        return rows;
    }

    private static int getContentStackWidth(ItemStack stack, FontRenderer font) {
        if (ItemHelpers.isEmpty(stack)) {
            return 0;
        }

        int countWidth = font.getStringWidth(abbreviateCount(stack.stackSize)) + COUNT_PADDING;

        return Math.max(countWidth, DEFAULT_STACK_WIDTH);
    }

    private static void renderUpgrades(List<UpgradeInfo> upgrades, FontRenderer font, int x, int y, int width,
        int height) {
        if (upgrades.isEmpty() || width <= 0 || height <= 0) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = RenderItem.getInstance();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        float oldZ = itemRender.zLevel;

        try {
            RenderHelper.enableGUIStandardItemLighting();

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            itemRender.zLevel = 300.0F;

            int availableWidth = Math.max(1, width);
            int row = 0;
            int rowWidth = 0;
            int drawX = x;

            for (UpgradeInfo info : upgrades) {
                int itemWidth = getUpgradeItemWidth(info);

                if (rowWidth > 0 && rowWidth + itemWidth > availableWidth) {
                    row++;
                    rowWidth = 0;
                    drawX = x;
                }

                int drawY = y + row * UPGRADE_ROW_HEIGHT;

                if ((row + 1) * UPGRADE_ROW_HEIGHT > height) {
                    break;
                }

                if (info.canBeDisabled()) {
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);

                    if (info.enabled()) {
                        OSCTextures.TOGGLE_ENABLE.draw(drawX, drawY + 3, 4, 10);
                    } else {
                        OSCTextures.TOGGLE_DISABLE.draw(drawX, drawY + 3, 4, 10);
                    }

                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_LIGHTING);

                    drawX += 4;
                }

                ItemStack stack = info.stack();

                if (stack != null) {
                    itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, drawX, drawY);
                }

                drawX += DEFAULT_STACK_WIDTH;
                rowWidth += itemWidth;
            }
        } finally {
            itemRender.zLevel = oldZ;

            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void renderContents(List<ItemStack> contents, FontRenderer font, int x, int y, int width,
        int height) {
        if (contents.isEmpty() || width <= 0 || height <= 0) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = RenderItem.getInstance();

        int maxPerRow = Math.max(1, GeneralConfig.tooltipMaxItemsPerRow);
        int availableWidth = Math.max(1, width);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        float oldZ = itemRender.zLevel;

        try {
            RenderHelper.enableGUIStandardItemLighting();

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            itemRender.zLevel = 300.0F;

            int row = 0;
            int rowWidth = 0;
            int itemsInRow = 0;
            int drawX = x;

            for (ItemStack stack : contents) {
                int stackWidth = getContentStackWidth(stack, font);

                if (stackWidth <= 0) {
                    continue;
                }

                if (itemsInRow > 0 && (itemsInRow >= maxPerRow || rowWidth + stackWidth > availableWidth)) {
                    row++;
                    rowWidth = 0;
                    itemsInRow = 0;
                    drawX = x;
                }

                int drawY = y + row * STACK_ROW_HEIGHT;

                if (drawY + STACK_ROW_HEIGHT > y + height) {
                    break;
                }

                String countText = abbreviateCount(stack.stackSize);

                int renderX = drawX + stackWidth - DEFAULT_STACK_WIDTH;

                itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, renderX, drawY);

                itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), stack, renderX, drawY, countText);

                drawX += stackWidth;
                rowWidth += stackWidth;
                itemsInRow++;
            }
        } finally {
            itemRender.zLevel = oldZ;

            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    public static void reset() {
        upgradeInfos.clear();
        sortedContents.clear();
        upgradeTooltipLines.clear();
    }

    /**
     * Prepares expanded tooltip data.
     * Called from addInformation() when shift is held.
     */
    public static void prepareContents(IStorageWrapper wrapper) {
        reset();
        Map<Integer, IUpgradeWrapper> wrappers = wrapper.getUpgradeHandler()
            .getSlotWrappers();
        for (IUpgradeWrapper uw : wrappers.values()) {
            boolean canBeDisabled = uw.canBeDisabled();
            boolean enabled = !canBeDisabled || uw.isEnabled();
            upgradeInfos.add(new UpgradeInfo(uw.getUpgradeStack(), canBeDisabled, enabled));
            upgradeTooltipLines.addAll(uw.getTooltipLines());
        }
        sortedContents = getCompactedStacksSortedByCount(wrapper.getInventoryHandler());
    }

    public static List<ItemStack> getCompactedStacksSortedByCount(IItemHandler handler) {
        Map<String, ItemStack> merged = new LinkedHashMap<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ItemHelpers.isEmpty(stack)) continue;
            String key = stack.getItem()
                .getUnlocalizedName() + ":"
                + stack.getItemDamage();
            if (merged.containsKey(key)) {
                merged.get(key).stackSize += stack.stackSize;
            } else {
                merged.put(key, stack.copy());
            }
        }

        List<ItemStack> result = new ArrayList<>(merged.values());
        result.sort(
            Comparator.comparingInt((ItemStack s) -> s.stackSize)
                .reversed());
        return result;
    }

    public static String abbreviateCount(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        }

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

}
