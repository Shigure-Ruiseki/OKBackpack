package ruiseki.okstoragecore.client.gui.widget.setting;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ColorPickerDialog;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSettingPanel;
import ruiseki.okstoragecore.client.gui.widget.Tab;
import ruiseki.okstoragecore.settings.SettingsManager;
import ruiseki.okstoragecore.settings.main.MainSettingsCategory;

public class StorageMainSettingTab extends SettingTab {

    protected static final List<CyclicVariantButtonWidget.Variant> SHIFT_CLICK_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.settings.buttons.shift_click_open_tab.on"),
            OSCTextures.SHIFT_CLICK_OPEN_TAB),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.settings.buttons.shift_click_open_tab.off"),
            OSCTextures.SHIFT_CLICK_INVENTORY));

    protected static final List<CyclicVariantButtonWidget.Variant> KEEP_TAB_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.settings.buttons.keep_tab_open.on"),
            OKBGuiTextures.KEEP_TAB_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.settings.buttons.keep_tab_open.off"),
            OKBGuiTextures.NOT_KEEP_TAB_ICON));

    protected static final List<CyclicVariantButtonWidget.Variant> KEEP_SEARCH_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.settings.buttons.keep_search_phrase.on"),
            OKBGuiTextures.LOCK_SEARCH_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.settings.buttons.keep_search_phrase.off"),
            OKBGuiTextures.UNLOCK_SEARCH_ICON));

    protected boolean usingPlayerSettings;

    protected boolean shiftClickIntoOpenTab;
    protected CyclicVariantButtonWidget shiftClickButton;
    protected boolean keepTabOpen;
    protected CyclicVariantButtonWidget keepTabButton;
    protected boolean keepSearch;
    protected CyclicVariantButtonWidget keepSearchButton;

    private int mainColor;
    private int accentColor;

    public StorageMainSettingTab(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        StorageSettingPanel settingPanel, Tab parentTab) {
        this(
            new ItemDrawable(
                storagePanel.getTile()
                    .getBlockType()),
            "gui.okstoragecore.storage_settings",
            Tab.ExpandDirection.RIGHT,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);
    }

    public StorageMainSettingTab(IDrawable delegatedIcon, String titleKey, Tab.ExpandDirection expandDirection,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, StorageSettingPanel settingPanel, Tab parentTab) {
        super(delegatedIcon, titleKey, expandDirection, storageWrapper, storagePanel, settingPanel, parentTab);

        this.usingPlayerSettings = this.getPlayerSettingsMode();
        this.shiftClickIntoOpenTab = this.getShiftClickIntoOpenTabValue();
        this.keepTabOpen = this.getKeepTabValue();
        this.keepSearch = this.getKeepSearchValue();
        this.mainColor = this.getStorageWrapper()
            .getMainColor();
        this.accentColor = this.getStorageWrapper()
            .getAccentColor();
    }

    @Override
    protected Flow addSettingsColumn() {
        return super.addSettingsColumn().child(this.addModeButton())
            .child(this.addButtonRow())
            .child(this.addColorRow());
    }

    protected ToggleButton addModeButton() {
        return new ToggleButton().size(60, 16)
            .value(new BoolValue.Dynamic(this::isUsingPlayerSettings, this::setUsingPlayerMode))
            .overlay(false, IKey.lang("gui.okstoragecore.settings.buttons.settings_context_storage"))
            .overlay(true, IKey.lang("gui.okstoragecore.settings.buttons.context_player"))
            .background(GuiTextures.MC_BUTTON)
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                if (this.isUsingPlayerSettings()) {
                    tooltip.addLine(IKey.lang("gui.okstoragecore.settings.buttons.context_player.tooltip"))
                        .addLine(
                            IKey.lang("gui.okstoragecore.settings.buttons.context_player.tooltip_detail")
                                .style(IKey.GRAY));
                } else {
                    tooltip.addLine(IKey.lang("gui.okstoragecore.settings.buttons.context_storage.tooltip"))
                        .addLine(
                            IKey.lang("gui.okstoragecore.settings.buttons.context_storage.tooltip_detail")
                                .style(IKey.GRAY));
                }
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });
    }

    protected void setUsingPlayerMode(boolean playerMode) {
        this.usingPlayerSettings = playerMode;
        reloadValues();
        this.getGlobalSetting()
            .setSettingValue(SettingsManager.PLAYER_SETTINGS, playerMode);
        this.getStoragePanel()
            .getStorageSH()
            .syncToServer(StorageSHRegisters.SET_PLAYER_SETTINGS_MODE, buffer -> buffer.writeBoolean(playerMode));
    }

    protected boolean isUsingPlayerSettings() {
        return this.usingPlayerSettings;
    }

    protected void reloadValues() {
        this.shiftClickIntoOpenTab = this.getShiftClickIntoOpenTabValue();
        if (this.shiftClickButton != null) {
            this.shiftClickButton.setIndex(this.shiftClickIntoOpenTab ? 1 : 0);
        }
        this.keepTabOpen = this.getKeepTabValue();
        if (this.keepTabButton != null) {
            this.keepTabButton.setIndex(this.keepTabOpen ? 1 : 0);
        }
        this.keepSearch = this.getKeepSearchValue();
        if (this.keepSearchButton != null) {
            this.keepSearchButton.setIndex(this.keepSearch ? 1 : 0);
        }
        this.mainColor = this.getStorageWrapper()
            .getMainColor();
        this.accentColor = this.getStorageWrapper()
            .getAccentColor();
    }

    protected Flow addButtonRow() {
        return Flow.row()
            .coverChildren()
            .child(this.addShiftClickButton())
            .child(this.addKeepTabButton())
            .child(this.addKeepSearchButton());
    }

    protected ButtonWidget<?> addShiftClickButton() {
        this.shiftClickButton = new CyclicVariantButtonWidget(
            SHIFT_CLICK_VARIANTS,
            this.isShiftClickIntoOpenTab() ? 1 : 0,
            index -> {
                boolean newValue = (index == 1);
                this.shiftClickIntoOpenTab = newValue;
                SettingsManager.setSettingByMode(
                    this.getStoragePanel()
                        .getPlayer(),
                    this.getGlobalSetting()
                        .getPlayerSettingsTagName(),
                    this.getGlobalSetting(),
                    SettingsManager.SHIFT_CLICK_INTO_OPEN_TAB_FIRST,
                    newValue,
                    this.isUsingPlayerSettings());
                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.SET_SHIFT_CLICK_INTO_TAB_FIRST_SETTING, buffer -> {
                        buffer.writeBoolean(newValue);
                        buffer.writeBoolean(this.isUsingPlayerSettings());
                    });
            }).setIndex(this.isShiftClickIntoOpenTab() ? 1 : 0)
                .tooltipDynamic(tooltip -> {
                    if (this.isShiftClickIntoOpenTab()) {
                        tooltip
                            .addLine(IKey.lang("gui.okstoragecore.settings.buttons.shift_click_open_tab.on.tooltip"));
                    } else {
                        tooltip
                            .addLine(IKey.lang("gui.okstoragecore.settings.buttons.shift_click_open_tab.off.tooltip"));
                    }
                });
        return this.shiftClickButton;
    }

    protected ButtonWidget<?> addKeepTabButton() {
        this.keepTabButton = new CyclicVariantButtonWidget(KEEP_TAB_VARIANTS, this.isKeepTabOpen() ? 1 : 0, index -> {
            boolean newValue = (index == 1);
            this.keepTabOpen = newValue;
            SettingsManager.setSettingByMode(
                this.getStoragePanel()
                    .getPlayer(),
                this.getGlobalSetting()
                    .getPlayerSettingsTagName(),
                this.getGlobalSetting(),
                SettingsManager.KEEP_TAB_OPEN,
                newValue,
                this.isUsingPlayerSettings());
            this.getStoragePanel()
                .getStorageSH()
                .syncToServer(StorageSHRegisters.SET_KEEP_TAB_OPEN_SETTING, buffer -> {
                    buffer.writeBoolean(newValue);
                    buffer.writeBoolean(this.isUsingPlayerSettings());
                });
        }).setIndex(this.isKeepTabOpen() ? 1 : 0)
            .tooltipDynamic(tooltip -> {
                if (this.isKeepTabOpen()) {
                    tooltip.addLine(IKey.lang("gui.okstoragecore.settings.buttons.keep_tab_open.on.tooltip"));
                } else {
                    tooltip.addLine(IKey.lang("gui.okstoragecore.settings.buttons.keep_tab_open.off.tooltip"));
                }
            });
        return this.keepTabButton;
    }

    protected ButtonWidget<?> addKeepSearchButton() {
        this.keepSearchButton = new CyclicVariantButtonWidget(
            KEEP_SEARCH_VARIANTS,
            this.isKeepSearchPhrase() ? 1 : 0,
            index -> {
                boolean newValue = (index == 1);
                this.keepSearch = newValue;
                SettingsManager.setSettingByMode(
                    this.getStoragePanel()
                        .getPlayer(),
                    this.getGlobalSetting()
                        .getPlayerSettingsTagName(),
                    this.getGlobalSetting(),
                    SettingsManager.KEEP_SEARCH_PHRASE,
                    newValue,
                    this.isUsingPlayerSettings());
                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.SET_KEEP_SEARCH_PHRASE_SETTING, buffer -> {
                        buffer.writeBoolean(newValue);
                        buffer.writeBoolean(this.isUsingPlayerSettings());
                    });
            }).setIndex(this.isKeepSearchPhrase() ? 1 : 0)
                .tooltipDynamic(tooltip -> {
                    if (this.isKeepSearchPhrase()) {
                        tooltip.addLine(IKey.lang("gui.okstoragecore.settings.buttons.keep_search_phrase.on.tooltip"));
                    } else {
                        tooltip.addLine(IKey.lang("gui.okstoragecore.settings.buttons.keep_search_phrase.off.tooltip"));
                    }
                });
        return this.keepSearchButton;
    }

    protected Flow addColorRow() {
        return Flow.row()
            .coverChildren()
            .child(this.addMainToggleButton())
            .child(this.addAccentToggleButton());
    }

    protected ButtonWidget<?> addMainToggleButton() {
        ButtonWidget<?> colorToggleButton = new ButtonWidget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                super.draw(context, widgetTheme);
                GlStateManager.disableDepth();
                GuiDraw.drawRect(5, 5, 10, 10, Color.withAlpha(mainColor, 255));
                GuiDraw.drawRect(4, 4, 12, 1, 0xFF202020);
                GuiDraw.drawRect(4, 15, 12, 1, 0xFF202020);
                GuiDraw.drawRect(4, 4, 1, 12, 0xFF202020);
                GuiDraw.drawRect(15, 4, 1, 12, 0xFF202020);
                GlStateManager.enableDepth();
            }
        };
        IPanelHandler colorPicker = IPanelHandler.simple(
            this.getSettingPanel(),
            (mainPanel,
                player) -> new ColorPickerDialog(
                    "storage_main_color_picker",
                    color -> this.updateColor(color, this.accentColor),
                    this.mainColor,
                    true).setDraggable(true)
                        .relative(this.getSettingPanel())
                        .top(0)
                        .rightRel(1f),
            true);
        colorToggleButton.size(20)
            .onMousePressed(button -> {
                if (button == 0) {
                    colorPicker.openPanel();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.okstoragecore.main_color"))
                    .addLine(
                        IKey.lang("gui.okstoragecore.main_color_detail")
                            .style(IKey.GRAY))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        return colorToggleButton;
    }

    protected ButtonWidget<?> addAccentToggleButton() {
        ButtonWidget<?> accentToggleButton = new ButtonWidget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                super.draw(context, widgetTheme);
                GlStateManager.disableDepth();
                GuiDraw.drawRect(5, 5, 10, 10, Color.withAlpha(accentColor, 255));
                GuiDraw.drawRect(4, 4, 12, 1, 0xFF202020);
                GuiDraw.drawRect(4, 15, 12, 1, 0xFF202020);
                GuiDraw.drawRect(4, 4, 1, 12, 0xFF202020);
                GuiDraw.drawRect(15, 4, 1, 12, 0xFF202020);
                GlStateManager.enableDepth();
            }
        };
        IPanelHandler colorPicker = IPanelHandler.simple(
            this.getSettingPanel(),
            (mainPanel,
                player) -> new ColorPickerDialog(
                    "storage_accent_color_picker",
                    color -> this.updateColor(this.mainColor, color),
                    this.accentColor,
                    true).setDraggable(true)
                        .relative(this.getSettingPanel())
                        .top(0)
                        .rightRel(1f),
            true);
        accentToggleButton.size(20)
            .onMousePressed(button -> {
                if (button == 0) {
                    colorPicker.openPanel();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.okstoragecore.accent_color"))
                    .addLine(
                        IKey.lang("gui.okstoragecore.accent_color_detail")
                            .style(IKey.GRAY))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        return accentToggleButton;
    }

    protected void updateColor(int mainColor, int accentColor) {
        this.mainColor = mainColor;
        this.accentColor = accentColor;
        this.getStorageWrapper()
            .setColors(this.mainColor, this.accentColor);
        this.getStoragePanel()
            .getStorageSH()
            .syncToServer(StorageSHRegisters.SET_SET_STORAGE_COLOR, buffer -> {
                buffer.writeInt(this.mainColor);
                buffer.writeInt(this.accentColor);
            });
    }

    protected boolean isShiftClickIntoOpenTab() {
        return this.shiftClickIntoOpenTab;
    }

    protected boolean isKeepTabOpen() {
        return keepTabOpen;
    }

    protected boolean isKeepSearchPhrase() {
        return keepSearch;
    }

    protected boolean getPlayerSettingsMode() {
        return this.getGlobalSetting()
            .getSettingValue(SettingsManager.PLAYER_SETTINGS)
            .orElse(false);
    }

    protected boolean getShiftClickIntoOpenTabValue() {
        return SettingsManager.getSettingByMode(
            this.getStoragePanel()
                .getPlayer(),
            this.getGlobalSetting()
                .getPlayerSettingsTagName(),
            this.getGlobalSetting(),
            SettingsManager.SHIFT_CLICK_INTO_OPEN_TAB_FIRST,
            this.isUsingPlayerSettings());
    }

    protected boolean getKeepTabValue() {
        return SettingsManager.getSettingByMode(
            this.getStoragePanel()
                .getPlayer(),
            this.getGlobalSetting()
                .getPlayerSettingsTagName(),
            this.getGlobalSetting(),
            SettingsManager.KEEP_TAB_OPEN,
            this.isUsingPlayerSettings());
    }

    protected boolean getKeepSearchValue() {
        return SettingsManager.getSettingByMode(
            this.getStoragePanel()
                .getPlayer(),
            this.getGlobalSetting()
                .getPlayerSettingsTagName(),
            this.getGlobalSetting(),
            SettingsManager.KEEP_SEARCH_PHRASE,
            this.isUsingPlayerSettings());
    }

    protected MainSettingsCategory<?> getGlobalSetting() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getGlobalSettingsCategory();
    }
}
