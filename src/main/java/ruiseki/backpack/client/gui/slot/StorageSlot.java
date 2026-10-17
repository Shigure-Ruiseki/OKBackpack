package ruiseki.backpack.client.gui.slot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.slot.UpgradeSlot;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.sync.StorageSlotSH;
import ruiseki.storagecore.client.gui.sync.StorageSlotSHRegistries;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.client.gui.widget.StorageSettingPanel;
import ruiseki.storagecore.settings.memory.MemorySettingsCategory;

public class StorageSlot extends ItemSlot {

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;
    private final int slotIndex;

    private StorageSlotSH syncHandler;

    public StorageSlot(IStorageWrapper storageWrapper, StoragePanel storagePanel, int slotIndex) {
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.slotIndex = slotIndex;
    }

    private boolean isInSettingMode() {
        return this.getStoragePanel()
            .getSettingPanelHandler()
            .isPanelOpen();
    }

    private boolean isInMemorySettingMode() {
        return this.getStoragePanel()
            .getSettingPanel()
            .map(StorageSettingPanel::isMemorySettingTabOpened)
            .orElse(false);
    }

    @Override
    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        if (stack == null) {
            super.buildTooltip(stack, tooltip);
            return;
        }

        super.buildTooltip(stack, tooltip);

        NumberFormat.Params params = NumberFormat.DEFAULT.copyToBuilder()
            .build();
        params.format.setMaximumFractionDigits(2);
        params.format.setMinimumFractionDigits(0);

        String count = NumberFormat.format(stack.stackSize, params);
        String limit = NumberFormat.format(
            this.getStorageWrapper()
                .getInventoryHandler()
                .getStackLimit(getSlot().getSlotIndex(), stack),
            params);

        tooltip.addLine(
            IKey.lang(
                "gui.storage.stack_size_extra",
                new ChatComponentText(count).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                    .getFormattedText(),
                new ChatComponentText(limit).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                    .getFormattedText()));
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

    private Result handleSettingSelection(int mouseButton) {
        if (!this.isInSettingMode()) return null;
        if (this.isInMemorySettingMode()) {
            boolean isMemorySet = this.getMemorySetting()
                .isSlotSelected(this.getSlotIndex());
            if (isMemorySet && mouseButton == 1) {
                MemorySettingsCategory memory = getMemorySetting();
                memory.unselectSlot(getSlotIndex());
                getSyncHandler().syncToServer(StorageSlotSHRegistries.UNSET_MEMORY_SLOT);
                return Result.SUCCESS;
            } else if (!isMemorySet && mouseButton == 0) {
                MemorySettingsCategory memory = this.getMemorySetting();
                memory.selectSlot(getSlotIndex());
                getSyncHandler().syncToServer(StorageSlotSHRegistries.SET_MEMORY_SLOT);
                return Result.SUCCESS;
            }
            return Result.STOP;
        }

        return null;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);

        this.drawMemoryStack();

        if (shouldHighlightConflict()) {
            GlStateManager.disableDepth();
            GuiDraw.drawRect(1, 1, 16, 16, UpgradeSlot.ERROR_SLOT_COLOR);
            GlStateManager.enableDepth();
        }
    }

    public boolean shouldHighlightConflict() {
        return this.getStoragePanel()
            .getErrorUpgradeSlotChangeResult()
            .map(
                result -> result.getErrorInventorySlots()
                    .contains(this.getSlotIndex()))
            .orElse(false);
    }

    public IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }

    public MemorySettingsCategory getMemorySetting() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }

    private void drawMemoryStack() {
        if (!this.getSlot()
            .getHasStack()) {
            this.getMemorySetting()
                .getSlotFilterStack(slotIndex, true)
                .ifPresent(this::drawMemoryStack);
        }
    }

    private void drawMemoryStack(ItemStack memoryStack) {
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
}
