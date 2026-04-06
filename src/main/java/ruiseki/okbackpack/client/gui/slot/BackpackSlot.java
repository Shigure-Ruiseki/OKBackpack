package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;
import lombok.Setter;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSHRegisters;

public class BackpackSlot extends ItemSlot {

    private final IStoragePanel<?> panel;
    private final IStorageWrapper wrapper;

    @Getter
    @Setter
    private boolean focus;

    public BackpackSlot(IStoragePanel<?> panel, IStorageWrapper wrapper) {
        this.panel = panel;
        this.wrapper = wrapper;
        this.focus = true;
    }

    private boolean isInSettingMode() {
        return panel.getSettingPanel()
            .isPanelOpen();
    }

    private boolean isInMemorySettingMode() {
        return panel.isMemorySettingTabOpened();
    }

    private boolean isInSortSettingMode() {
        return panel.isSortingSettingTabOpened();
    }

    @Override
    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        ItemStack memorizedStack = wrapper.getMemoryStack(getSlot().slotNumber);

        if (stack == null && memorizedStack == null) return;

        String formattedCount;
        String formattedStackLimit;

        NumberFormat.Params p = NumberFormat.DEFAULT.copyToBuilder()
            .build();
        p.format.setMaximumFractionDigits(2);
        p.format.setMinimumFractionDigits(0);

        if (stack != null) {
            super.buildTooltip(stack, tooltip);
            formattedCount = NumberFormat.format(stack.stackSize, p);
            formattedStackLimit = NumberFormat.format(getSlot().getItemStackLimit(stack), p);
        } else {
            super.buildTooltip(memorizedStack, tooltip);
            formattedCount = "0";
            formattedStackLimit = NumberFormat.format(getSlot().getItemStackLimit(memorizedStack), p);
        }

        tooltip.addLine(
            IKey.lang(
                "gui.backpack.stack_size_extra",
                new ChatComponentText(formattedCount).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                    .getFormattedText(),
                new ChatComponentText(formattedStackLimit)
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                    .getFormattedText()));

        int index = getSlot().slotNumber;

        if (wrapper.isSlotMemorized(index)) {
            tooltip.addLine(
                IKey.lang("gui.backpack.memorized_slot")
                    .style(IKey.LIGHT_PURPLE));

            if (wrapper.isMemoryStackRespectNBT(index)) {
                tooltip.addLine(
                    IKey.comp(IKey.str("- "), IKey.lang("gui.backpack.match_nbt"))
                        .style(EnumChatFormatting.YELLOW));
            } else {
                tooltip.addLine(
                    IKey.comp(IKey.str("- "), IKey.lang("gui.backpack.ignore_nbt"))
                        .style(EnumChatFormatting.GRAY));
            }
        }

        if (wrapper.isSlotLocked(index)) {
            tooltip.addLine(
                IKey.lang("gui.backpack.no_sorting_slot")
                    .style(EnumChatFormatting.DARK_RED));
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        int index = getSlot().slotNumber;

        if (isInMemorySettingMode()) {

            boolean isMemorySet = wrapper.isSlotMemorized(index);

            if (isMemorySet && mouseButton == 1) {
                wrapper.unsetMemoryStack(index);
                getSyncHandler().syncToServer(BackpackSlotSH.getId(BackpackSlotSHRegisters.UPDATE_UNSET_MEMORY_STACK));
                return Result.SUCCESS;

            } else if (!isMemorySet && mouseButton == 0) {
                wrapper.setMemoryStack(index, panel.shouldMemorizeRespectNBT());
                getSyncHandler().syncToServer(
                    BackpackSlotSH.getId(BackpackSlotSHRegisters.UPDATE_SET_MEMORY_STACK),
                    buf -> buf.writeBoolean(panel.shouldMemorizeRespectNBT()));
                return Result.SUCCESS;

            } else return Result.STOP;
        }

        else if (isInSortSettingMode()) {
            boolean locked = wrapper.isSlotLocked(index);

            if (locked && mouseButton == 1) {
                wrapper.setSlotLocked(index, false);
                getSyncHandler().syncToServer(BackpackSlotSH.getId(BackpackSlotSHRegisters.UPDATE_UNSET_SLOT_LOCK));
                return Result.SUCCESS;
            } else if (!locked && mouseButton == 0) {
                wrapper.setSlotLocked(index, true);
                getSyncHandler().syncToServer(BackpackSlotSH.getId(BackpackSlotSHRegisters.UPDATE_SET_SLOT_LOCK));
                return Result.SUCCESS;
            } else return Result.STOP;
        }

        else if (isInSettingMode()) {
            return Result.STOP;
        }

        return super.onMousePressed(mouseButton);
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        if (isInSettingMode()) return true;
        return super.onMouseRelease(mouseButton);
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        if (isInSettingMode()) return;
        super.onMouseDrag(mouseButton, timeSinceClick);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetThemeEntry) {
        WidgetTheme widgetTheme = widgetThemeEntry.getTheme() != null ? widgetThemeEntry.getTheme()
            : WidgetTheme.getDefault()
                .getTheme();
        int index = getSlot().slotNumber;

        if (wrapper.isSlotLocked(index)) drawLockedSlot(context, widgetTheme);

        if (isInSettingMode()) drawSettingStack(context, widgetTheme);
        else drawNormalStack(context, widgetTheme);

        if (!focus && !isInSettingMode()) {
            drawDimmedSlot(context);
        }
    }

    private void drawDimmedSlot(ModularGuiContext context) {
        GuiDraw.drawRect(1, 1, 17, 17, 0x88000000);
    }

    private void drawSettingStack(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack slotStack = getSlot().getStack();
        ItemStack memoryStack = wrapper.getMemoryStack(getSlot().slotNumber);
        ItemStack toRender = slotStack != null ? slotStack : memoryStack;

        if (toRender == null) return;

        GuiScreen guiScreen = getScreen().getScreenWrapper()
            .getGuiScreen();
        if (!(guiScreen instanceof GuiContainer guiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");

        RenderItem renderItem = GuiScreenAccessor.getItemRender();
        float z = getContext().getCurrentDrawingZ() + 100;
        ((GuiAccessor) guiScreen).setZLevel(z);
        renderItem.zLevel = z;

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();

        Platform.setupDrawItem();
        float itemScale = NEAAnimationHandler.injectHoverScale(guiContainer, getSlot());
        renderItem.renderItemAndEffectIntoGUI(
            Minecraft.getMinecraft().fontRenderer,
            Minecraft.getMinecraft()
                .getTextureManager(),
            toRender,
            1,
            1);

        if (slotStack == null && memoryStack != null) {
            GlStateManager.disableDepth();
            GuiDraw.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128));
            GlStateManager.enableAlpha();
        }

        GuiDraw.afterRenderItemAndEffectIntoGUI(toRender);
        NEAAnimationHandler.endHoverScale();
        Platform.endDrawItem();

        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();

        ((GuiAccessor) guiScreen).setZLevel(0f);
        renderItem.zLevel = 0f;
    }

    private void drawNormalStack(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (!(getSlot() instanceof ModularBackpackSlot bslot)) return;

        ItemStack memoryStack = bslot.getMemoryStack();

        superDraw();

        if (bslot.getStack() == null && memoryStack != null) drawMemoryStack(memoryStack);
    }

    private void drawMemoryStack(ItemStack memoryStack) {
        GuiScreen guiScreen = getScreen().getScreenWrapper()
            .getGuiScreen();
        if (!(guiScreen instanceof GuiContainer guiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        RenderItem renderItem = GuiScreenAccessor.getItemRender();

        float z = getContext().getCurrentDrawingZ() + 100;
        ((GuiAccessor) guiScreen).setZLevel(z);
        renderItem.zLevel = z;

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();

        Platform.setupDrawItem();
        float itemScale = NEAAnimationHandler.injectHoverScale(guiContainer, getSlot());
        ItemStack stackToRender = NEAAnimationHandler.injectVirtualStack(memoryStack, guiContainer, getSlot());
        if (stackToRender != null) {
            renderItem.renderItemAndEffectIntoGUI(
                Minecraft.getMinecraft().fontRenderer,
                Minecraft.getMinecraft()
                    .getTextureManager(),
                stackToRender,
                1,
                1);
            GuiDraw.afterRenderItemAndEffectIntoGUI(stackToRender);
            NEAAnimationHandler.endHoverScale();
        }
        Platform.endDrawItem();

        GlStateManager.disableDepth();
        GuiDraw.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128));
        GlStateManager.enableAlpha();

        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();

        ((GuiAccessor) guiScreen).setZLevel(0f);
        renderItem.zLevel = 0f;
    }

    private void drawLockedSlot(ModularGuiContext context, WidgetTheme widgetTheme) {
        OKBGuiTextures.NO_SORT_ICON.draw(context, 1, 1, 16, 16, widgetTheme);
        GlStateManager.disableDepth();
        GuiDraw.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128));
        GlStateManager.enableDepth();
    }

    private void superDraw() {
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(getSlot());
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();

        if (isHovering()) {
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, 16, 16, getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawSlot(ModularSlot slotIn) {
        GuiScreen guiScreen = getScreen().getScreenWrapper()
            .getGuiScreen();
        if (!(guiScreen instanceof GuiContainer guiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        GuiContainerAccessor acc = (GuiContainerAccessor) guiScreen;
        RenderItem renderItem = GuiScreenAccessor.getItemRender();
        ItemStack itemstack = slotIn.getStack();
        boolean isDragPreview = false;
        boolean flag1 = slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null && !acc.getIsRightMouseClick();
        ItemStack itemstack1 = guiScreen.mc.thePlayer.inventory.getItemStack();
        int amount = -1;
        String format = null;

        if (!getSyncHandler().isPhantom()) {
            if (slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null
                && acc.getIsRightMouseClick()
                && itemstack != null) {
                itemstack = itemstack.copy();
                itemstack.stackSize /= 2;
            } else if (acc.getDragSplitting() && acc.getDragSplittingSlots()
                .contains(slotIn) && itemstack1 != null) {
                    if (acc.getDragSplittingSlots()
                        .size() == 1) {
                        return;
                    }

                    // canAddItemToSlot
                    if (Container.func_94527_a(slotIn, itemstack1, true) && getScreen().getContainer()
                        .canDragIntoSlot(slotIn)) {
                        itemstack = itemstack1.copy();
                        isDragPreview = true;
                        // computeStackSize
                        Container.func_94525_a(
                            acc.getDragSplittingSlots(),
                            acc.getDragSplittingLimit(),
                            itemstack,
                            slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);
                        int k = Math.min(itemstack.getMaxStackSize(), slotIn.getSlotStackLimit());

                        if (itemstack.stackSize > k) {
                            amount = k;
                            format = EnumChatFormatting.YELLOW.toString();
                            itemstack.stackSize = k;
                        }
                    } else {
                        acc.getDragSplittingSlots()
                            .remove(slotIn);
                        acc.invokeUpdateDragSplitting();
                    }
                }
        }

        // makes sure items of different layers don't interfere with each other visually
        float z = getContext().getCurrentDrawingZ() + 100;
        ((GuiAccessor) guiScreen).setZLevel(z);
        renderItem.zLevel = z;

        if (!flag1) {
            if (isDragPreview) {
                GuiDraw.drawRect(1, 1, 16, 16, -2130706433);
            }

            itemstack = NEAAnimationHandler.injectVirtualStack(itemstack, guiContainer, slotIn);

            if (itemstack != null) {
                Platform.setupDrawItem();
                float itemScale = NEAAnimationHandler.injectHoverScale(guiContainer, slotIn);
                // render the item itself
                renderItem.renderItemAndEffectIntoGUI(
                    Minecraft.getMinecraft().fontRenderer,
                    Minecraft.getMinecraft()
                        .getTextureManager(),
                    itemstack,
                    1,
                    1);
                GuiDraw.afterRenderItemAndEffectIntoGUI(itemstack);
                GlStateManager.disableRescaleNormal();
                Platform.endDrawItem();

                if (amount < 0) {
                    amount = itemstack.stackSize;
                }
                GuiDraw.drawStandardSlotAmountText(amount, format, getArea());

                int cachedCount = itemstack.stackSize;
                itemstack.stackSize = 1; // required to not render the amount overlay
                // render other overlays like durability bar
                renderItem.renderItemOverlayIntoGUI(
                    ((GuiScreenAccessor) guiScreen).getFontRenderer(),
                    Minecraft.getMinecraft()
                        .getTextureManager(),
                    itemstack,
                    1,
                    1,
                    null);
                NEAAnimationHandler.endHoverScale();
                itemstack.stackSize = cachedCount;
                GlStateManager.disableDepth();
                GlStateManager.disableLighting();
            }
        }

        ((GuiAccessor) guiScreen).setZLevel(0f);
        renderItem.zLevel = 0f;
    }

}
