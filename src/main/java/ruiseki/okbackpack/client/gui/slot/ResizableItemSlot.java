package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.mixins.early.modularui2.ItemSlotInvoker;

public class ResizableItemSlot extends ItemSlot {

    public ResizableItemSlot() {
        this(18);
    }

    public ResizableItemSlot(int size) {
        this.size(size);
    }

    @Override
    public void onInit() {
        if (getScreen().isOverlay()) {
            throw new IllegalStateException("Overlays can't have slots!");
        }
    }

    protected int getItemX() {
        return (getArea().width - 16) / 2;
    }

    protected int getItemY() {
        return (getArea().height - 16) / 2;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(getSlot());
        RenderHelper.disableStandardItemLighting();
        drawOverlay();
    }

    @Override
    protected void drawOverlay() {
        if (isHovering()) {
            GuiDraw.drawRect(this.getItemX(), this.getItemY(), 16, 16, getSlotHoverColor());
        }
    }

    @SideOnly(Side.CLIENT)
    protected void drawSlot(ModularSlot slotIn) {
        GuiScreen guiScreen = getScreen().getScreenWrapper()
            .getGuiScreen();
        if (!(guiScreen instanceof GuiContainer guiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        GuiContainerAccessor acc = (GuiContainerAccessor) guiScreen;
        RenderItem renderItem = GuiScreenAccessor.getItemRender();
        ItemStack itemstack = slotIn.getStack();
        boolean isDragPreview = false;
        boolean doDrawItem = slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null
            && !acc.getIsRightMouseClick();
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

        if (!doDrawItem) {
            if (isDragPreview) {
                GuiDraw.drawRect(this.getItemX(), this.getItemY(), 16, 16, 0x80FFFFFF);
            }

            ((ItemSlotInvoker) this).invokeRenderSlotUnderlayNEI(guiContainer, slotIn);

            itemstack = getItemStackForRendering(itemstack, isDragPreview);

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
                    this.getItemX(),
                    this.getItemY());

                GuiDraw.afterRenderItemAndEffectIntoGUI(itemstack);
                GlStateManager.disableRescaleNormal();
                Platform.endDrawItem();

                if (amount < 0) {
                    amount = itemstack.stackSize;
                }
                drawSlotAmountText(amount, format);

                int cachedCount = itemstack.stackSize;
                itemstack.stackSize = 1; // required to not render the amount overlay
                // render other overlays like durability bar

                renderItem.renderItemOverlayIntoGUI(
                    ((GuiScreenAccessor) guiScreen).getFontRenderer(),
                    Minecraft.getMinecraft()
                        .getTextureManager(),
                    itemstack,
                    this.getItemX(),
                    this.getItemY(),
                    null);
                NEAAnimationHandler.endHoverScale();
                itemstack.stackSize = cachedCount;
                GlStateManager.disableDepth();
                GlStateManager.disableLighting();
            }

            ((ItemSlotInvoker) this).invokeRenderSlotOverlayNEI(guiContainer, slotIn);
        }

        ((GuiAccessor) guiScreen).setZLevel(0f);
        renderItem.zLevel = 0f;
    }
}
