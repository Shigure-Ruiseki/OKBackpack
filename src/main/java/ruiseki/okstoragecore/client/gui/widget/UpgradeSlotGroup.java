package ruiseki.okstoragecore.client.gui.widget;

import static ruiseki.okbackpack.client.gui.OKBGuiTextures.TOGGLE_DISABLE_ICON;
import static ruiseki.okbackpack.client.gui.OKBGuiTextures.TOGGLE_ENABLE_ICON;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class UpgradeSlotGroup extends ParentWidget<UpgradeSlotGroup> {

    private static final UITexture UPPER_TAB_TEXTURE = UITexture.builder()
        .location(new ResourceLocation(Reference.MOD_ID, "gui/gui_controls.png"))
        .imageSize(256, 256)
        .xy(0, 0, 25, 5)
        .build();

    private static final UITexture SLOT_SURROUNDING_TEXTURE = UITexture.builder()
        .location(new ResourceLocation(Reference.MOD_ID, "gui/gui_controls.png"))
        .imageSize(256, 256)
        .xy(0, 6, 25, 18)
        .build();

    private static final UITexture LOWER_TAB_TEXTURE = UITexture.builder()
        .location(new ResourceLocation(Reference.MOD_ID, "gui/gui_controls.png"))
        .imageSize(256, 256)
        .xy(0, 199, 25, 5)
        .build();

    private final StoragePanel storagePanel;

    private final List<UpgradeToggleWidget> toggleWidgets;

    public UpgradeSlotGroup(StoragePanel storagePanel) {
        this.storagePanel = storagePanel;

        this.toggleWidgets = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            UpgradeToggleWidget toggleWidget = new UpgradeToggleWidget(
                this.getStorageWrapper(),
                this.getStoragePanel(),
                i).syncHandler(
                    this.getStoragePanel()
                        .getUpgradeSlotSH(i))
                    .name("upgrade_toggle_" + i);
            this.toggleWidgets.add(toggleWidget);
            this.child(toggleWidget);
        }
    }

    public List<UpgradeToggleWidget> getToggleWidgets() {
        return this.toggleWidgets;
    }

    public UpgradeToggleWidget getToggleWidget(int index) {
        return this.toggleWidgets.get(index);
    }

    @Override
    public void onInit() {
        this.getContext()
            .getUISettings()
            .getRecipeViewerSettings()
            .addExclusionArea(this);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);

        int y = 5;

        UPPER_TAB_TEXTURE.draw(context, 0, 0, 25, 5, widgetTheme.getTheme());

        for (int i = 0; i < getSize(); i++) {
            SLOT_SURROUNDING_TEXTURE.draw(context, 0, y, 25, 18, widgetTheme.getTheme());
            y += 18;
        }

        LOWER_TAB_TEXTURE.draw(context, 0, y, 25, 5, widgetTheme.getTheme());
    }

    public int getSize() {
        return this.getStorageWrapper()
            .getUpgradeHandler()
            .getSlots();
    }

    public IStorageWrapper getStorageWrapper() {
        return this.getStoragePanel()
            .getWrapper();
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public static class UpgradeToggleWidget extends Widget<UpgradeToggleWidget> implements Interactable {

        private static final int WIDTH = 9;
        private static final int HEIGHT = 18;

        private static final UITexture BACKGROUND_TAB_TEXTURE = UITexture.builder()
            .location(new ResourceLocation(Reference.MOD_ID, "gui/gui_controls.png"))
            .imageSize(256, 256)
            .xy(0, 204, WIDTH, HEIGHT)
            .build();

        private boolean isToggleEnabled = false;
        private UpgradeSlotSH syncHandler;
        private final IStorageWrapper storageWrapper;
        private final StoragePanel storagePanel;
        private final int slotIndex;

        public UpgradeToggleWidget(IStorageWrapper storageWrapper, StoragePanel storagePanel, int slotIndex) {
            this.storageWrapper = storageWrapper;
            this.storagePanel = storagePanel;
            this.slotIndex = slotIndex;

            this.size(WIDTH, HEIGHT);
            this.left(-4);
            this.top(slotIndex * 18 + 4);
            this.setEnabled(false);

            IUpgradeWrapper upgradeWrapper = this.getUpgradeWrapper();
            if (upgradeWrapper != null) {
                this.isToggleEnabled = upgradeWrapper.isEnabled();
                this.setEnabled(upgradeWrapper.canBeDisabled());
            }
        }

        public void setToggleEnabled(boolean toggleEnabled) {
            this.isToggleEnabled = toggleEnabled;
        }

        public IUpgradeWrapper getUpgradeWrapper() {
            return this.getStorageWrapper()
                .getUpgradeHandler()
                .getSlotWrapper(this.getSlotIndex());
        }

        public StoragePanel getStoragePanel() {
            return storagePanel;
        }

        public IStorageWrapper getStorageWrapper() {
            return storageWrapper;
        }

        public int getSlotIndex() {
            return slotIndex;
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            IUpgradeWrapper upgradeWrapper = this.getUpgradeWrapper();
            if (upgradeWrapper == null || !upgradeWrapper.canBeDisabled()) return Result.IGNORE;
            boolean enabled = !upgradeWrapper.isEnabled();
            upgradeWrapper.setEnabled(enabled);
            this.isToggleEnabled = enabled;
            this.getSyncHandler()
                .syncToServer(UpgradeSlotSHRegistries.ENABLE, buffer -> { buffer.writeBoolean(enabled); });
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }

        public UpgradeToggleWidget syncHandler(UpgradeSlotSH syncHandler) {
            this.setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
            return this;
        }

        @Override
        protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
            super.setSyncOrValue(syncOrValue);
            this.syncHandler = syncOrValue.castOrThrow(UpgradeSlotSH.class);
        }

        @Override
        public @NotNull UpgradeSlotSH getSyncHandler() {
            if (this.syncHandler == null) {
                throw new IllegalStateException("Widget is not initialised!");
            }
            return this.syncHandler;
        }

        @Override
        public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
            // disallow null
            return syncOrValue instanceof UpgradeSlotSH;
        }

        @Override
        public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            super.drawOverlay(context, widgetTheme);
            if (this.isToggleEnabled) {
                TOGGLE_ENABLE_ICON.draw(context, 4, 4, 4, 10, widgetTheme.getTheme());
            } else {
                TOGGLE_DISABLE_ICON.draw(context, 4, 4, 4, 10, widgetTheme.getTheme());
            }
        }

        @Override
        public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            super.drawBackground(context, widgetTheme);
            BACKGROUND_TAB_TEXTURE.draw(context, 0, 0, WIDTH, HEIGHT, widgetTheme.getTheme());
        }

    }
}
