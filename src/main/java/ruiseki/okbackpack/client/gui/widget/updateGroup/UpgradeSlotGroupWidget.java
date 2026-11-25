package ruiseki.okbackpack.client.gui.widget.updateGroup;

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

import lombok.Setter;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;

public class UpgradeSlotGroupWidget extends ParentWidget<UpgradeSlotGroupWidget> {

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

    private final List<UpgradeToggleWidget> toggleWidgets;
    private final int slotSize;

    public UpgradeSlotGroupWidget(IStoragePanel<?> panel, int slotSize) {
        super();
        this.slotSize = slotSize;

        this.toggleWidgets = new ArrayList<>();

        for (int i = 0; i < slotSize; i++) {
            UpgradeToggleWidget toggleWidget = new UpgradeToggleWidget(panel, i).syncHandler("upgrades", i)
                .name("upgrade_toggle_" + i);
            toggleWidgets.add(toggleWidget);
            child(toggleWidget);
        }
    }

    @Override
    public void onInit() {
        getContext().getUISettings()
            .getRecipeViewerSettings()
            .addExclusionArea(this);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);

        int y = 5;

        UPPER_TAB_TEXTURE.draw(context, 0, 0, 25, 5, widgetTheme.getTheme());

        for (int i = 0; i < slotSize; i++) {
            SLOT_SURROUNDING_TEXTURE.draw(context, 0, y, 25, 18, widgetTheme.getTheme());
            y += 18;
        }

        LOWER_TAB_TEXTURE.draw(context, 0, y, 25, 5, widgetTheme.getTheme());
    }

    public UpgradeToggleWidget getToggleWidget(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= toggleWidgets.size()) {
            return null;
        }
        return toggleWidgets.get(slotIndex);
    }

    public static class UpgradeToggleWidget extends Widget<UpgradeToggleWidget> implements Interactable {

        private static final int WIDTH = 9;
        private static final int HEIGHT = 18;

        private static final UITexture BACKGROUND_TAB_TEXTURE = UITexture.builder()
            .location(new ResourceLocation(Reference.MOD_ID, "gui/gui_controls.png"))
            .imageSize(256, 256)
            .xy(0, 204, WIDTH, HEIGHT)
            .build();

        @Setter
        private boolean isToggleEnabled = false;
        private UpgradeSlotSH slotSyncHandler = null;
        private final IStoragePanel panel;
        private final int slotIndex;

        public UpgradeToggleWidget(IStoragePanel panel, int slotIndex) {
            this.panel = panel;
            this.slotIndex = slotIndex;

            this.size(WIDTH, HEIGHT);
            this.left(-4);
            this.top(slotIndex * 18 + 4);

            this.setEnabled(false);

            IToggleable wrapper = getWrapper();
            if (wrapper != null) {
                isToggleEnabled = wrapper.isEnabled();
                setEnabled(true);
            }
        }

        public IToggleable getWrapper() {
            IUpgradeWrapper wrapper = panel.getWrapper()
                .getUpgradeHandler()
                .getWrapperInSlot(slotIndex);
            if (wrapper instanceof IToggleable toggleableWrapper) {
                return toggleableWrapper;
            } else {
                return null;
            }
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            isToggleEnabled = !isToggleEnabled;
            IToggleable wrapper = getWrapper();
            if (wrapper != null) {
                wrapper.toggle();
            }
            if (slotSyncHandler != null) {
                slotSyncHandler.syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_UPGRADE_TOGGLE));
            }

            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }

        @Override
        public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
            if (syncOrValue instanceof UpgradeSlotSH) {
                slotSyncHandler = (UpgradeSlotSH) syncOrValue;
            }
            return slotSyncHandler != null;
        }

        @Override
        public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            super.drawOverlay(context, widgetTheme);
            if (isToggleEnabled) {
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
