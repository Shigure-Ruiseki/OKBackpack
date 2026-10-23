package ruiseki.okstoragecore.client.gui.widget;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.TabTexture;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.OSCTextures;

public class SettingTab extends Widget<SettingTab> implements Interactable {

    public static final TabTexture TAB_TEXTURE = GuiTextures.TAB_RIGHT;

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;

    public SettingTab(IStorageWrapper storageWrapper, StoragePanel storagePanel) {
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.size(TAB_TEXTURE.getWidth(), TAB_TEXTURE.getHeight())
            .right(-TAB_TEXTURE.getWidth() + 4)
            .top(0)
            .background(TAB_TEXTURE.get(-1, false))
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                if (this.getStoragePanel()
                    .getSettingPanelHandler()
                    .isPanelOpen()) {
                    tooltip.addLine(IKey.lang("gui.okstoragecore.settings.storage"))
                        .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                } else {
                    tooltip.addLine(IKey.lang("gui.okstoragecore.settings.storage.tooltip"))
                        .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                }
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });
    }

    @Override
    public void onInit() {
        getContext().getUISettings()
            .getRecipeViewerSettings()
            .addExclusionArea(this);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton == 0) {
            StoragePanel panel = this.getStoragePanel();

            Interactable.playButtonClickSound();
            if (panel.getSettingPanelHandler()
                .isPanelOpen()) {
                panel.getSettingPanelHandler()
                    .closePanel();
                panel.updateUpgradeTabs();
            } else {
                panel.getSettingPanelHandler()
                    .openPanel();
                panel.disableAllTabWidgets();
            }
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        if (this.getStoragePanel()
            .getSettingPanelHandler()
            .isPanelOpen()) {
            OSCTextures.BACK.draw(context, 8, 6, 16, 16, widgetTheme.getTheme());
        } else {
            OSCTextures.SETTING.draw(context, 8, 6, 16, 16, widgetTheme.getTheme());
        }
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }
}
