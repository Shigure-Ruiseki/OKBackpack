package ruiseki.backpack.client.gui.widget.setting.main;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.client.gui.sync.BackpackSHRegistries;
import ruiseki.backpack.settings.BackpackMainSettingsCategory;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.widget.StorageSettingPanel;
import ruiseki.okstoragecore.client.gui.widget.Tab;
import ruiseki.okstoragecore.client.gui.widget.setting.StorageMainSettingTab;
import ruiseki.okstoragecore.settings.SettingsManager;

public class BackpackMainSettingTab extends StorageMainSettingTab {

    protected static final List<CyclicVariantButtonWidget.Variant> LOCK_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.settings.buttons.another_player_can_open.on"),
            OKBGuiTextures.LOCK_BACKPACK_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.settings.buttons.another_player_can_open.off"),
            OKBGuiTextures.UNLOCK_BACKPACK_ICON));

    protected boolean anotherPlayerCanOpen;
    protected CyclicVariantButtonWidget anotherPlayerCanOpenButton;

    public BackpackMainSettingTab(IBackpackWrapper storageWrapper, BackpackPanel storagePanel,
        StorageSettingPanel settingPanel, Tab parentTab) {
        super(
            3,
            OKBGuiTextures.BACKPACK_ICON,
            "gui.okbackpack.settings.backpack",
            120,
            Tab.ExpandDirection.RIGHT,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);

        this.anotherPlayerCanOpen = this.getAnotherPlayerCanOpenValue();
    }

    @Override
    protected ToggleButton addModeButton() {
        return super.addModeButton().overlay(false, IKey.lang("gui.okbackpack.settings.buttons.context_backpack="))
            .overlay(true, IKey.lang("gui.okstoragecore.settings.buttons.context_player"))
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                if (this.isUsingPlayerSettings()) {
                    tooltip.addLine(IKey.lang("gui.okstoragecore.settings.buttons.context_player.tooltip"))
                        .addLine(
                            IKey.lang("gui.okstoragecore.settings.buttons.context_player.tooltip_detail")
                                .style(IKey.GRAY));
                } else {
                    tooltip.addLine(IKey.lang("gui.okbackpack.settings.buttons.context_backpack.tooltip"))
                        .addLine(
                            IKey.lang("gui.okbackpack.settings.buttons.context_backpack.tooltip_detail")
                                .style(IKey.GRAY));
                }
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });
    }

    @Override
    public Flow addButtonRow() {
        return super.addButtonRow().child(this.addAnotherPlayerCanOpenButton());
    }

    protected ButtonWidget<?> addAnotherPlayerCanOpenButton() {
        this.anotherPlayerCanOpenButton = new CyclicVariantButtonWidget(
            LOCK_VARIANTS,
            this.isAnotherPlayerCanOpen() ? 1 : 0,
            index -> {
                boolean newValue = (index == 1);
                this.anotherPlayerCanOpen = newValue;

                SettingsManager.setSettingByMode(
                    this.getStoragePanel()
                        .getPlayer(),
                    this.getGlobalSetting()
                        .getPlayerSettingsTagName(),
                    this.getGlobalSetting(),
                    BackpackMainSettingsCategory.ANOTHER_PLAYER_CAN_OPEN,
                    newValue,
                    this.isUsingPlayerSettings());

                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(BackpackSHRegistries.SET_ANOTHER_PLAYER_CAN_OPEN_SETTING, buffer -> {
                        buffer.writeBoolean(newValue);
                        buffer.writeBoolean(this.isUsingPlayerSettings());
                    });
            }).setIndex(this.isAnotherPlayerCanOpen() ? 1 : 0)
                .tooltipDynamic(tooltip -> {
                    if (this.isShiftClickIntoOpenTab()) {
                        tooltip
                            .addLine(IKey.lang("gui.okbackpack.settings.buttons.another_player_can_open.on.tooltip"));
                    } else {
                        tooltip
                            .addLine(IKey.lang("gui.okbackpack.settings.buttons.another_player_can_open.off.tooltip"));
                    }
                });

        return this.anotherPlayerCanOpenButton;
    }

    @Override
    protected void reloadValues() {
        super.reloadValues();
        this.anotherPlayerCanOpen = this.getAnotherPlayerCanOpenValue();
        if (this.anotherPlayerCanOpenButton != null) {
            this.anotherPlayerCanOpenButton.setIndex(this.anotherPlayerCanOpen ? 1 : 0);
        }
    }

    protected boolean isAnotherPlayerCanOpen() {
        return anotherPlayerCanOpen;
    }

    protected boolean getAnotherPlayerCanOpenValue() {
        return SettingsManager.getSettingByMode(
            this.getStoragePanel()
                .getPlayer(),
            this.getGlobalSetting()
                .getPlayerSettingsTagName(),
            this.getGlobalSetting(),
            BackpackMainSettingsCategory.ANOTHER_PLAYER_CAN_OPEN,
            this.isUsingPlayerSettings());
    }

    @Override
    protected IBackpackWrapper getStorageWrapper() {
        return (IBackpackWrapper) super.getStorageWrapper();
    }

    @Override
    protected BackpackPanel getStoragePanel() {
        return (BackpackPanel) super.getStoragePanel();
    }
}
