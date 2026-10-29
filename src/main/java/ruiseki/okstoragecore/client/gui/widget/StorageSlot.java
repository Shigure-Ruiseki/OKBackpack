package ruiseki.okstoragecore.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiAccessor;
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
import com.cleanroommc.neverenoughanimations.NEAConfig;

import cpw.mods.fml.common.Optional;
import ruiseki.okbackpack.client.gui.slot.ResizableItemSlot;
import ruiseki.okbackpack.client.gui.slot.UpgradeSlot;
import ruiseki.okbackpack.compat.bogosorter.BackpackBogoSorterClientCompat;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegisters;
import ruiseki.okstoragecore.client.gui.sync.StorageSlotSH;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.settings.nosort.NoSortSettingsCategory;

public class StorageSlot extends ResizableItemSlot {

    private final StoragePanel storagePanel;
    private final int slotIndex;

    protected StorageSlotSH syncHandler;

    public StorageSlot(StoragePanel storagePanel, int slotIndex) {
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
                "gui.okstoragecore.stack_size_extra",
                new ChatComponentText(count).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                    .getFormattedText(),
                new ChatComponentText(limit).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                    .getFormattedText()));

        if (this.isMemorySlot()) {
            tooltip.addLine(
                IKey.lang("gui.okstoragecore.memorized_slot")
                    .style(IKey.LIGHT_PURPLE));

            if (this.getMemorySetting()
                .ignoresNbt()) {
                tooltip.addLine(
                    IKey.comp(IKey.str("- "), IKey.lang("gui.okstoragecore.ignore_nbt"))
                        .style(EnumChatFormatting.GRAY));
            } else {
                tooltip.addLine(
                    IKey.comp(IKey.str("- "), IKey.lang("gui.okstoragecore.match_nbt"))
                        .style(EnumChatFormatting.YELLOW));
            }
        }

        if (this.isNoSortingSlot()) {
            tooltip.addLine(
                IKey.lang("gui.okstoragecore.no_sorting_slot")
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
                refreshBogoSortingContext();
                return Result.SUCCESS;
            } else if (!isMemorySet && mouseButton == 0) {
                MemorySettingsCategory settings = this.getMemorySetting();
                settings.selectSlot(getSlotIndex());
                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.SET_MEMORY_SLOT, buffer -> buffer.writeInt(this.getSlotIndex()));
                refreshBogoSortingContext();
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
                refreshBogoSortingContext();
                return Result.SUCCESS;
            } else if (!noSorting && mouseButton == 0) {
                NoSortSettingsCategory settings = this.getNoSortingSettings();
                settings.selectSlot(this.getSlotIndex());
                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.SET_NO_SORT_SLOT, buffer -> buffer.writeInt(this.getSlotIndex()));
                refreshBogoSortingContext();
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
        return this.getStoragePanel()
            .getWrapper();
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
        GuiDraw.drawRect(
            1,
            1,
            16,
            16,
            this.getNoSortingSettings()
                .getColor());
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

    @Override
    protected void drawSlotAmountText(int amount, String format) {
        if (amount >= Integer.MAX_VALUE) {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            String infinityText = "\u221E";
            int x = this.getArea().width - font.getStringWidth(infinityText) - 1;
            int y = this.getArea().height - font.FONT_HEIGHT;
            font.drawStringWithShadow(infinityText, x, y, 0xFFFFFF);
        } else {
            GuiDraw.drawStandardSlotAmountText(amount, format, getArea());
        }
    }
}
