package ruiseki.storagecore.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
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
import com.cleanroommc.neverenoughanimations.NEAConfig;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.client.gui.slot.UpgradeSlot;
import ruiseki.okbackpack.compat.bogosorter.BackpackBogoSorterClientCompat;
import ruiseki.okbackpack.mixins.early.modularui2.ItemSlotInvoker;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.sync.StorageSHRegisters;
import ruiseki.storagecore.client.gui.sync.StorageSlotSH;
import ruiseki.storagecore.settings.memory.MemorySettingsCategory;
import ruiseki.storagecore.settings.nosort.NoSortSettingsCategory;

public class StorageSlot extends ItemSlot {

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;
    private final int slotIndex;

    protected StorageSlotSH syncHandler;

    public StorageSlot(IStorageWrapper storageWrapper, StoragePanel storagePanel, int slotIndex) {
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.slotIndex = slotIndex;
    }

    @Override
    public @NotNull StorageSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return this.syncHandler;
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castOrThrow(StorageSlotSH.class);
    }

    protected boolean isInSettingMode() {
        return this.getStoragePanel()
            .getSettingPanelHandler()
            .isPanelOpen();
    }

    protected boolean isInMemoryMode() {
        return this.getStoragePanel()
            .getSettingPanel()
            .map(
                settingPanel -> settingPanel.getMemoryTab()
                    .isShowExpanded())
            .orElse(false);
    }

    protected boolean isInSortingMode() {
        return this.getStoragePanel()
            .getSettingPanel()
            .map(
                settingPanel -> settingPanel.getSortingTab()
                    .isShowExpanded())
            .orElse(false);
    }

    @Override
    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        ItemStack memorizedStack = this.getMemoryStack();

        NumberFormat.Params p = NumberFormat.DEFAULT.copyToBuilder()
            .build();
        p.format.setMaximumFractionDigits(2);
        p.format.setMinimumFractionDigits(0);

        String count = "0";
        String limit = String.valueOf(
            this.getSlot()
                .getSlotStackLimit());
        if (!ItemHelpers.isEmpty(stack)) {
            super.buildTooltip(stack, tooltip);
            count = NumberFormat.format(stack.stackSize, p);
            limit = NumberFormat.format(
                this.getSlot()
                    .getItemStackLimit(stack),
                p);
        } else if (!ItemHelpers.isEmpty(memorizedStack)) {
            super.buildTooltip(memorizedStack, tooltip);
            count = "0";
            limit = NumberFormat.format(
                this.getSlot()
                    .getItemStackLimit(memorizedStack),
                p);
        }

        tooltip.addLine(
            IKey.lang(
                "gui.storage.stack_size_extra",
                new ChatComponentText(count).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                    .getFormattedText(),
                new ChatComponentText(limit).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                    .getFormattedText()));

        if (this.isMemorySlot()) {
            tooltip.addLine(
                IKey.lang("gui.storage.memorized_slot")
                    .style(IKey.LIGHT_PURPLE));

            if (this.getMemorySetting()
                .ignoresNbt()) {
                tooltip.addLine(
                    IKey.comp(IKey.str("- "), IKey.lang("gui.storage.ignore_nbt"))
                        .style(EnumChatFormatting.GRAY));
            } else {
                tooltip.addLine(
                    IKey.comp(IKey.str("- "), IKey.lang("gui.storage.match_nbt"))
                        .style(EnumChatFormatting.YELLOW));
            }
        }

        if (this.isNoSortingSlot()) {
            tooltip.addLine(
                IKey.lang("gui.storage.no_sorting_slot")
                    .style(EnumChatFormatting.DARK_RED));
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        Result settingResult = this.handleSettingSelection(mouseButton);
        if (settingResult != null) return settingResult;
        if (this.isInSettingMode()) return Result.STOP;
        return super.onMousePressed(mouseButton);
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        if (this.isInSettingMode()) return true;
        return super.onMouseRelease(mouseButton);
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        Result settingResult = this.handleSettingSelection(mouseButton);
        if (settingResult != null) return;
        if (this.isInSettingMode()) return;
        super.onMouseDrag(mouseButton, timeSinceClick);
    }

    protected Result handleSettingSelection(int mouseButton) {
        if (!this.isInSettingMode()) return null;
        if (this.isInMemoryMode()) {
            boolean isMemorySet = this.isMemorySlot();
            if (isMemorySet && mouseButton == 1) {
                MemorySettingsCategory settings = this.getMemorySetting();
                settings.unselectSlot(getSlotIndex());
                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.UNSET_MEMORY_SLOT, buffer -> buffer.writeInt(this.getSlotIndex()));
                // refreshBogoSortingContext();
                return Result.SUCCESS;
            } else if (!isMemorySet && mouseButton == 0) {
                MemorySettingsCategory settings = this.getMemorySetting();
                settings.selectSlot(getSlotIndex());
                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.SET_MEMORY_SLOT, buffer -> buffer.writeInt(this.getSlotIndex()));
                // refreshBogoSortingContext();
                return Result.SUCCESS;
            }
            return Result.STOP;
        }

        if (this.isInSortingMode()) {
            boolean noSorting = this.isNoSortingSlot();
            if (noSorting && mouseButton == 1) {
                NoSortSettingsCategory settings = this.getNoSortingSettings();
                settings.unselectSlot(this.getSlotIndex());
                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(
                        StorageSHRegisters.UNSET_NO_SORT_SLOT,
                        buffer -> buffer.writeInt(this.getSlotIndex()));
                // refreshBogoSortingContext();
                return Result.SUCCESS;
            } else if (!noSorting && mouseButton == 0) {
                NoSortSettingsCategory settings = this.getNoSortingSettings();
                settings.selectSlot(this.getSlotIndex());
                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.SET_NO_SORT_SLOT, buffer -> buffer.writeInt(this.getSlotIndex()));
                // refreshBogoSortingContext();
                return Result.SUCCESS;
            }
            return Result.STOP;
        }

        return null;
    }

    @Optional.Method(modid = "bogosorter")
    private static void refreshBogoSortingContext() {
        BackpackBogoSorterClientCompat.invalidateSortingContext();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetThemeEntry) {
        WidgetTheme widgetTheme = widgetThemeEntry.getTheme() != null ? widgetThemeEntry.getTheme()
            : WidgetTheme.getDefault()
                .getTheme();
        super.draw(context, widgetThemeEntry);

        if (this.isNoSortingSlot()) drawLockedSlot(context, widgetTheme);

        if (this.isInSettingMode()) drawSettingStack(context, widgetTheme);
        else drawNormalStack(context, widgetTheme);

        if (this.shouldHighlightConflict()) {
            GlStateManager.disableDepth();
            GuiDraw.drawRect(1, 1, 16, 16, UpgradeSlot.ERROR_SLOT_COLOR);
            GlStateManager.enableDepth();
        }
    }

    protected boolean shouldHighlightConflict() {
        return this.getStoragePanel()
            .getErrorUpgradeSlotChangeResult()
            .map(
                result -> result.getErrorInventorySlots()
                    .contains(this.getSlotIndex()))
            .orElse(false);
    }

    protected IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    protected StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    protected int getSlotIndex() {
        return this.slotIndex;
    }

    protected MemorySettingsCategory getMemorySetting() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }

    protected ItemStack getMemoryStack() {
        return this.getMemorySetting()
            .getSlotFilterStack(slotIndex, true)
            .orElse(ItemHelpers.EMPTY);
    }

    protected boolean isMemorySlot() {
        return this.getMemorySetting()
            .isSlotSelected(slotIndex);
    }

    protected NoSortSettingsCategory getNoSortingSettings() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getTypeCategory(NoSortSettingsCategory.class);
    }

    protected boolean isNoSortingSlot() {
        return this.getNoSortingSettings()
            .isSlotSelected(this.getSlotIndex());
    }

    protected void drawLockedSlot(ModularGuiContext context, WidgetTheme widgetTheme) {
        GlStateManager.disableDepth();
        int color = this.getNoSortingSettings()
            .getColor()
            .getColor();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        r = (r + 255) / 2;
        g = (g + 255) / 2;
        b = (b + 255) / 2;
        int lightColor = 0xFF000000 | (r << 16) | (g << 8) | b;
        GuiDraw.drawRect(1, 1, 17, 17, lightColor);
        GlStateManager.enableDepth();
    }

    protected void drawSettingStack(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack slotStack = this.getSlot()
            .getStack();
        ItemStack memoryStack = this.getMemoryStack();
        ItemStack toRender = !ItemHelpers.isEmpty(slotStack) ? slotStack : memoryStack;

        if (ItemHelpers.isEmpty(toRender)) return;

        GuiScreen guiScreen = this.getScreen()
            .getScreenWrapper()
            .getGuiScreen();
        if (!(guiScreen instanceof GuiContainer guiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");

        RenderItem renderItem = GuiScreenAccessor.getItemRender();
        float z = this.getContext()
            .getCurrentDrawingZ() + 100;
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

        boolean isMarked = this.isMemorySlot() || (ItemHelpers.isEmpty(slotStack) && !ItemHelpers.isEmpty(memoryStack));
        if (isMarked) {
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

    protected void drawNormalStack(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack memoryStack = this.getMemoryStack();
        this.superDraw();
        if (!this.getSlot()
            .getHasStack() && !ItemHelpers.isEmpty(memoryStack)) this.drawMemoryStack(memoryStack);
    }

    protected void drawMemoryStack(ItemStack memoryStack) {
        GuiScreen guiScreen = getScreen().getScreenWrapper()
            .getGuiScreen();

        if (!(guiScreen instanceof GuiContainer guiContainer)) {
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        }

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

    protected void superDraw() {
        RenderHelper.enableGUIStandardItemLighting();
        this.drawSlot(getSlot());
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        this.drawOverlay();
    }

    @Override
    protected void drawOverlay() {
        if (this.isHovering() && (!ModularUI.Mods.NEA.isLoaded() || NEAConfig.itemHoverOverlay)) {
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, 16, 16, getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
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
        boolean flag1 = slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null && !acc.getIsRightMouseClick();
        ItemStack itemstack1 = guiScreen.mc.thePlayer.inventory.getItemStack();
        int amount = -1;
        String format = null;

        if (!this.getSyncHandler()
            .isPhantom()) {
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
        float z = this.getContext()
            .getCurrentDrawingZ() + 100;
        ((GuiAccessor) guiScreen).setZLevel(z);
        renderItem.zLevel = z;

        if (!flag1) {
            if (isDragPreview) {
                GuiDraw.drawRect(1, 1, 16, 16, -2130706433);
            }

            ((ItemSlotInvoker) this).invokeRenderSlotUnderlayNEI(guiContainer, slotIn);

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
                if (amount >= Integer.MAX_VALUE) {
                    FontRenderer font = Minecraft.getMinecraft().fontRenderer;
                    String infinityText = "\u221E";
                    int x = this.getArea().width - font.getStringWidth(infinityText) - 1;
                    int y = this.getArea().height - font.FONT_HEIGHT;
                    font.drawStringWithShadow(infinityText, x, y, 0xFFFFFF);
                } else {
                    GuiDraw.drawStandardSlotAmountText(amount, format, getArea());
                }

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

            ((ItemSlotInvoker) this).invokeRenderSlotOverlayNEI(guiContainer, slotIn);
        }

        ((GuiAccessor) guiScreen).setZLevel(0f);
        renderItem.zLevel = 0f;
    }

}
