package ruiseki.storagecore.client.gui.widget.setting;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
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
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.sync.StorageSHRegisters;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.client.gui.widget.StorageSettingPanel;
import ruiseki.storagecore.client.gui.widget.Tab;
import ruiseki.storagecore.settings.SettingsManager;
import ruiseki.storagecore.settings.main.MainSettingsCategory;
import ruiseki.storagecore.settings.main.StorageMainSettingCategory;

public class StorageMainSettingTab extends SettingTab {

    protected static final List<CyclicVariantButtonWidget.Variant> SHIFT_CLICK_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.storage.shift_click_inventory"),
            OKBGuiTextures.SHIFT_CLICK_INVENTORY_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.storage.shift_click_open_tab"),
            OKBGuiTextures.SHIFT_CLICK_OPEN_TAB_ICON));

    protected static final List<CyclicVariantButtonWidget.Variant> KEEP_TAB_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.keep_tab"), OKBGuiTextures.KEEP_TAB_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.not_keep_tab"), OKBGuiTextures.NOT_KEEP_TAB_ICON));

    protected static final List<CyclicVariantButtonWidget.Variant> KEEP_SEARCH_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.storage.unlock_search"),
            OKBGuiTextures.UNLOCK_SEARCH_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.lock_search"), OKBGuiTextures.LOCK_SEARCH_ICON));

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
            3,
            OKBGuiTextures.BACKPACK_ICON,
            "gui.storage.storage_settings",
            120,
            Tab.ExpandDirection.RIGHT,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);
    }

    public StorageMainSettingTab(int coveredTabSize, IDrawable delegatedIcon, String titleKey, int width,
        Tab.ExpandDirection expandDirection, IStorageWrapper storageWrapper, StoragePanel storagePanel,
        StorageSettingPanel settingPanel, Tab parentTab) {
        super(
            coveredTabSize,
            delegatedIcon,
            titleKey,
            width,
            expandDirection,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);

        this.usingPlayerSettings = this.getPlayerSettingsMode();
        this.shiftClickIntoOpenTab = this.getShiftClickValue();
        this.keepTabOpen = this.getKeepTabValue();
        this.keepSearch = this.getKeepSearchValue();
        this.mainColor = this.getStorageWrapper()
            .getMainColor();
        this.accentColor = this.getStorageWrapper()
            .getAccentColor();

        this.child(this.addModeButton());
        this.child(this.addButtonRow());
        this.child(this.addColorRow());
    }

    protected ToggleButton addModeButton() {
        return new ToggleButton().pos(6, 26)
            .size(60, 16)
            .value(new BoolValue.Dynamic(this::isUsingPlayerSettings, this::setUsingPlayerMode))
            .overlay(false, IKey.lang("gui.storage.settings_context_storage"))
            .overlay(true, IKey.lang("gui.storage.settings_context_player"))
            .background(GuiTextures.MC_BUTTON)
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();

                if (this.isUsingPlayerSettings()) {
                    tooltip.addLine(IKey.lang("gui.storage.settings_context_player"))
                        .addLine(
                            IKey.lang("gui.storage.settings_context_player.tooltip")
                                .style(IKey.GRAY));
                } else {
                    tooltip.addLine(IKey.lang("gui.storage.settings_context_storage"))
                        .addLine(
                            IKey.lang("gui.storage.settings_context_storage.tooltip")
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

    private void reloadValues() {
        this.shiftClickIntoOpenTab = this.getShiftClickValue();
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

    public Flow addButtonRow() {
        return Flow.row()
            .pos(6, 44)
            .height(20)
            .coverChildrenWidth()
            .childPadding(0)
            .child(this.addShiftClickButton())
            .child(this.addKeepTabButton())
            .child(this.addKeepSearchButton());
    }

    protected ButtonWidget<?> addShiftClickButton() {
        this.shiftClickButton = new CyclicVariantButtonWidget(
            SHIFT_CLICK_VARIANTS,
            this.isShiftClickIntoOpenTabValue() ? 1 : 0,
            index -> {
                boolean newValue = (index == 1);
                this.shiftClickIntoOpenTab = newValue;

                SettingsManager.setSettingByMode(
                    this.getStoragePanel()
                        .getPlayer(),
                    StorageMainSettingCategory.OK_STORAGE_SETTINGS_PLAYER_TAG,
                    this.getGlobalSetting(),
                    SettingsManager.SHIFT_CLICK_INTO_OPEN_TAB_FIRST,
                    newValue,
                    this.isUsingPlayerSettings());

                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.SET_SHIFT_CLICK_SETTING, buffer -> {
                        buffer.writeBoolean(newValue);
                        buffer.writeBoolean(this.isUsingPlayerSettings());
                    });
            }).setIndex(this.isShiftClickIntoOpenTabValue() ? 1 : 0);

        return this.shiftClickButton;
    }

    protected ButtonWidget<?> addKeepTabButton() {
        this.keepTabButton = new CyclicVariantButtonWidget(KEEP_TAB_VARIANTS, this.isKeepTabOpen() ? 1 : 0, index -> {
            boolean newValue = (index == 1);
            this.keepTabOpen = newValue;

            SettingsManager.setSettingByMode(
                this.getStoragePanel()
                    .getPlayer(),
                StorageMainSettingCategory.OK_STORAGE_SETTINGS_PLAYER_TAG,
                this.getGlobalSetting(),
                SettingsManager.KEEP_TAB_OPEN,
                newValue,
                this.isUsingPlayerSettings());

            this.getStoragePanel()
                .getStorageSH()
                .syncToServer(StorageSHRegisters.SET_KEEP_TAB_SETTING, buffer -> {
                    buffer.writeBoolean(newValue);
                    buffer.writeBoolean(this.isUsingPlayerSettings());
                });
        }).setIndex(this.isKeepTabOpen() ? 1 : 0);

        return this.keepTabButton;
    }

    protected ButtonWidget<?> addKeepSearchButton() {
        this.keepSearchButton = new CyclicVariantButtonWidget(
            KEEP_SEARCH_VARIANTS,
            this.isKeepSearch() ? 1 : 0,
            index -> {
                boolean newValue = (index == 1);
                this.keepSearch = newValue;

                SettingsManager.setSettingByMode(
                    this.getStoragePanel()
                        .getPlayer(),
                    StorageMainSettingCategory.OK_STORAGE_SETTINGS_PLAYER_TAG,
                    this.getGlobalSetting(),
                    SettingsManager.KEEP_SEARCH,
                    newValue,
                    this.isUsingPlayerSettings());

                this.getStoragePanel()
                    .getStorageSH()
                    .syncToServer(StorageSHRegisters.SET_KEEP_SEARCH_SETTING, buffer -> {
                        buffer.writeBoolean(newValue);
                        buffer.writeBoolean(this.isUsingPlayerSettings());
                    });
            }).setIndex(this.isKeepSearch() ? 1 : 0);

        return this.keepSearchButton;
    }

    protected Flow addColorRow() {
        return Flow.row()
            .pos(6, 66)
            .height(20)
            .coverChildrenWidth()
            .childPadding(0)
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
                t -> t.addLine(IKey.lang("gui.storage.main_color"))
                    .addLine(
                        IKey.lang("gui.storage.main_color_detail")
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
                t -> t.addLine(IKey.lang("gui.storage.accent_color"))
                    .addLine(
                        IKey.lang("gui.storage.accent_color_detail")
                            .style(IKey.GRAY))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        return accentToggleButton;
    }

    private void updateColor(int mainColor, int accentColor) {
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

    protected boolean isShiftClickIntoOpenTabValue() {
        return this.shiftClickIntoOpenTab;
    }

    protected boolean isKeepTabOpen() {
        return keepTabOpen;
    }

    protected boolean isKeepSearch() {
        return keepSearch;
    }

    private boolean getPlayerSettingsMode() {
        return this.getGlobalSetting()
            .getSettingValue(SettingsManager.PLAYER_SETTINGS)
            .orElse(false);
    }

    private boolean getShiftClickValue() {
        return SettingsManager.getSettingByMode(
            this.getStoragePanel()
                .getPlayer(),
            StorageMainSettingCategory.OK_STORAGE_SETTINGS_PLAYER_TAG,
            this.getGlobalSetting(),
            SettingsManager.SHIFT_CLICK_INTO_OPEN_TAB_FIRST,
            this.isUsingPlayerSettings());
    }

    private boolean getKeepTabValue() {
        return SettingsManager.getSettingByMode(
            this.getStoragePanel()
                .getPlayer(),
            StorageMainSettingCategory.OK_STORAGE_SETTINGS_PLAYER_TAG,
            this.getGlobalSetting(),
            SettingsManager.KEEP_TAB_OPEN,
            this.isUsingPlayerSettings());
    }

    private boolean getKeepSearchValue() {
        return SettingsManager.getSettingByMode(
            this.getStoragePanel()
                .getPlayer(),
            StorageMainSettingCategory.OK_STORAGE_SETTINGS_PLAYER_TAG,
            this.getGlobalSetting(),
            SettingsManager.KEEP_SEARCH,
            this.isUsingPlayerSettings());
    }

    protected MainSettingsCategory<?> getGlobalSetting() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getGlobalSettingsCategory();
    }
}
