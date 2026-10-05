package ruiseki.okbackpack.client.gui.widget;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ColorPickerDialog;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.block.BackpackSettingPanel;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;

public class BackpackSettingWidget extends ExpandedTabWidget {

    private final IStoragePanel<?> panel;
    private final IStorageWrapper wrapper;
    private final BackpackSettingPanel settingPanel;
    private final TabWidget parentTabWidget;

    private final ButtonWidget<?> modeButton;
    private final CyclicVariantButtonWidget shiftClickButton;
    private final CyclicVariantButtonWidget tabButton;
    private final CyclicVariantButtonWidget searchButton;
    private final CyclicVariantButtonWidget lockButton;

    private final ButtonWidget<?> mainColorButton;
    private final ButtonWidget<?> accentColorButton;
    private final Rectangle mainColorPreview;
    private final Rectangle accentColorPreview;

    private static final List<CyclicVariantButtonWidget.Variant> SHIFT_CLICK_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.shift_click_inventory"),
            OKBGuiTextures.SHIFT_CLICK_INVENTORY_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.shift_click_open_tab"),
            OKBGuiTextures.SHIFT_CLICK_OPEN_TAB_ICON));

    private static final List<CyclicVariantButtonWidget.Variant> KEEP_TAB_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.keep_tab"), OKBGuiTextures.KEEP_TAB_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.not_keep_tab"),
            OKBGuiTextures.NOT_KEEP_TAB_ICON));

    private static final List<CyclicVariantButtonWidget.Variant> KEEP_SEARCH_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.unlock_search"),
            OKBGuiTextures.UNLOCK_SEARCH_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.lock_search"), OKBGuiTextures.LOCK_SEARCH_ICON));

    private static final List<CyclicVariantButtonWidget.Variant> LOCK_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.unlock_backpack"),
            OKBGuiTextures.UNLOCK_BACKPACK_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.lock_backpack"),
            OKBGuiTextures.LOCK_BACKPACK_ICON));

    public BackpackSettingWidget(IStoragePanel<?> panel, BackpackSettingPanel settingPanel, TabWidget parentTabWidget) {
        super(4, OKBGuiTextures.BACKPACK_ICON, "gui.backpack.backpack_settings", 100, TabWidget.ExpandDirection.RIGHT);

        this.panel = panel;
        this.wrapper = panel.getWrapper();
        this.settingPanel = settingPanel;
        this.parentTabWidget = parentTabWidget;

        modeButton = new ButtonWidget<>().pos(6, 28)
            .size(60, 18)
            .onMousePressed(mouseButton -> {
                if (mouseButton == 0) {
                    wrapper.setUsePlayerSettings(!wrapper.isUsePlayerSettings());
                    if (wrapper.isUsePlayerSettings()) {
                        BackpackProperty playerSettings = getPlayerSettings();
                        if (playerSettings != null) {
                            playerSettings.applySettingsToWrapper(wrapper);
                        }
                    }
                    syncButtonStates();
                    updateWrapper();
                    return true;
                }
                return false;
            })
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                if (isUsingPlayerSettings()) {
                    tooltip.addLine(IKey.lang("gui.backpack.settings_context_player"))
                        .addLine(
                            IKey.lang("gui.backpack.settings_context_player.tooltip")
                                .style(IKey.GRAY));
                } else {
                    tooltip.addLine(IKey.lang("gui.backpack.settings_context_backpack"))
                        .addLine(
                            IKey.lang("gui.backpack.settings_context_backpack.tooltip")
                                .style(IKey.GRAY));
                }
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });

        Flow buttonRow = Flow.row()
            .pos(6, 48)
            .height(20)
            .coverChildrenWidth()
            .childPadding(0);

        shiftClickButton = new CyclicVariantButtonWidget(
            SHIFT_CLICK_VARIANTS,
            getShiftClickIntoOpenTabValue() ? 1 : 0,
            (index) -> {
                if (isUsingPlayerSettings()) {
                    BackpackProperty playerSettings = getPlayerSettings();
                    if (playerSettings != null) {
                        playerSettings.setShiftClickIntoOpenTab(index == 1);
                        playerSettings.applySettingsToWrapper(wrapper);
                    }
                } else {
                    wrapper.setShiftClickIntoOpenTab(index == 1);
                }
                syncButtonStates();
                updateWrapper();
            });
        shiftClickButton.tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (getShiftClickIntoOpenTabValue()) {
                tooltip.addLine(IKey.lang("gui.backpack.shift_click_open_tab"))
                    .addLine(
                        IKey.lang("gui.backpack.shift_click_open_tab.tooltip")
                            .style(IKey.GRAY));
            } else {
                tooltip.addLine(IKey.lang("gui.backpack.shift_click_inventory"))
                    .addLine(
                        IKey.lang("gui.backpack.shift_click_inventory.tooltip")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        })
            .tooltipAutoUpdate(true);

        tabButton = new CyclicVariantButtonWidget(KEEP_TAB_VARIANTS, getKeepTabValue() ? 0 : 1, (index) -> {
            if (isUsingPlayerSettings()) {
                BackpackProperty playerSettings = getPlayerSettings();
                if (playerSettings != null) {
                    playerSettings.setKeepTab(index == 0);
                    playerSettings.applySettingsToWrapper(wrapper);
                }
            } else {
                wrapper.setKeepTab(index == 0);
            }
            syncButtonStates();
            updateWrapper();
        });
        tabButton.tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (getKeepTabValue()) {
                tooltip.addLine(IKey.lang("gui.backpack.keep_tab"))
                    .addLine(
                        IKey.lang("gui.backpack.keep_tab.tooltip")
                            .style(IKey.GRAY));
            } else {
                tooltip.addLine(IKey.lang("gui.backpack.not_keep_tab"))
                    .addLine(
                        IKey.lang("gui.backpack.not_keep_tab.tooltip")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        })
            .tooltipAutoUpdate(true);

        searchButton = new CyclicVariantButtonWidget(KEEP_SEARCH_VARIANTS, getKeepSearchValue() ? 1 : 0, (index) -> {
            if (isUsingPlayerSettings()) {
                BackpackProperty playerSettings = getPlayerSettings();
                if (playerSettings != null) {
                    playerSettings.setKeepSearchPhrase(index == 1);
                    playerSettings.applySettingsToWrapper(wrapper);
                }
            } else {
                wrapper.setKeepSearchPhrase(index == 1);
            }
            if (!getKeepSearchValue() && panel instanceof BackpackPanel backpackPanel) {
                backpackPanel.clearSearchPhrase();
            }
            syncButtonStates();
            updateWrapper();
        });
        searchButton.tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (getKeepSearchValue()) {
                tooltip.addLine(IKey.lang("gui.backpack.lock_search"))
                    .addLine(
                        IKey.lang("gui.backpack.lock_search.tooltip")
                            .style(IKey.GRAY));
            } else {
                tooltip.addLine(IKey.lang("gui.backpack.unlock_search"))
                    .addLine(
                        IKey.lang("gui.backpack.unlock_search.tooltip")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        })
            .tooltipAutoUpdate(true);

        lockButton = new CyclicVariantButtonWidget(LOCK_VARIANTS, getLockStorageValue() ? 1 : 0, (index) -> {
            if (isUsingPlayerSettings()) {
                BackpackProperty playerSettings = getPlayerSettings();
                if (playerSettings != null) {
                    playerSettings.setLockBackpack(index == 1);
                    playerSettings.applySettingsToWrapper(wrapper);
                }
            } else {
                wrapper.setLockStorage(index == 1);
            }
            syncButtonStates();
            updateWrapper();
        });
        lockButton.tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (getLockStorageValue()) {
                tooltip.addLine(IKey.lang("gui.backpack.lock_backpack"))
                    .addLine(
                        IKey.lang("gui.backpack.lock_backpack.tooltip")
                            .style(IKey.GRAY));
            } else {
                tooltip.addLine(IKey.lang("gui.backpack.unlock_backpack"))
                    .addLine(
                        IKey.lang("gui.backpack.unlock_backpack.tooltip")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        })
            .tooltipAutoUpdate(true);

        buttonRow.child(shiftClickButton)
            .child(tabButton)
            .child(searchButton)
            .child(lockButton);

        mainColorPreview = new Rectangle().color(wrapper.getMainColor());
        IPanelHandler colorPicker1 = IPanelHandler
            .simple(settingPanel, (mainPanel, player) -> new ColorPickerDialog("color_picker1", newColor -> {
                mainColorPreview.color(newColor);
                wrapper.setColors(newColor, wrapper.getAccentColor());
                syncButtonStates();
                updateWrapper();
            }, mainColorPreview.getColor(), true).setDraggable(true)
                .relative(settingPanel)
                .top(0)
                .rightRel(1f), true);

        accentColorPreview = new Rectangle().color(wrapper.getAccentColor());
        IPanelHandler colorPicker2 = IPanelHandler
            .simple(settingPanel, (mainPanel, player) -> new ColorPickerDialog("color_picker2", newColor -> {
                accentColorPreview.color(newColor);
                wrapper.setColors(wrapper.getMainColor(), newColor);
                syncButtonStates();
                updateWrapper();
            }, accentColorPreview.getColor(), true).setDraggable(true)
                .relative(settingPanel)
                .top(0)
                .leftRel(1f), true);

        mainColorButton = new ButtonWidget<>().name("color picker button 1")
            .size(18, 18)
            .background(mainColorPreview)
            .disableHoverBackground()
            .onMousePressed(mouseButton -> {
                if (mouseButton == 0) {
                    colorPicker1.openPanel();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                tooltip -> tooltip.addLine(IKey.lang("gui.backpack.main_color"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        accentColorButton = new ButtonWidget<>().name("color picker button 2")
            .size(18, 18)
            .background(accentColorPreview)
            .disableHoverBackground()
            .onMousePressed(mouseButton -> {
                if (mouseButton == 0) {
                    colorPicker2.openPanel();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                tooltip -> tooltip.addLine(IKey.lang("gui.backpack.accent_color"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        Flow colorRow = Flow.row()
            .pos(6, 70)
            .height(20)
            .coverChildrenWidth()
            .childPadding(4)
            .child(mainColorButton)
            .child(accentColorButton);

        child(modeButton);
        child(buttonRow);
        child(colorRow);
        syncButtonStates();

        phantomTabWidget.getTabIcon()
            .tooltipStatic(
                tooltip -> tooltip.addLine(IKey.lang("gui.backpack.backpack_settings"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
    }

    @Override
    public void onInit() {
        getContext().getUISettings()
            .getRecipeViewerSettings()
            .addExclusionArea(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        syncButtonStates();
    }

    @Override
    public void updateTabState() {
        parentTabWidget.setShowExpanded(!parentTabWidget.isShowExpanded());
        settingPanel.updateTabState(0);
    }

    private BackpackProperty getPlayerSettings() {
        return BackpackProperty.get(panel.getPlayer());
    }

    private boolean isUsingPlayerSettings() {
        return wrapper.isUsePlayerSettings();
    }

    private boolean getKeepTabValue() {
        BackpackProperty playerSettings = getPlayerSettings();
        return isUsingPlayerSettings() && playerSettings != null ? playerSettings.isKeepTab() : wrapper.isKeepTab();
    }

    private boolean getShiftClickIntoOpenTabValue() {
        BackpackProperty playerSettings = getPlayerSettings();
        return isUsingPlayerSettings() && playerSettings != null ? playerSettings.isShiftClickIntoOpenTab()
            : wrapper.isShiftClickIntoOpenTab();
    }

    private boolean getKeepSearchValue() {
        BackpackProperty playerSettings = getPlayerSettings();
        return isUsingPlayerSettings() && playerSettings != null ? playerSettings.isKeepSearchPhrase()
            : wrapper.isKeepSearchPhrase();
    }

    private boolean getLockStorageValue() {
        BackpackProperty playerSettings = getPlayerSettings();
        return isUsingPlayerSettings() && playerSettings != null ? playerSettings.isLockBackpack()
            : wrapper.isLockStorage();
    }

    private void syncButtonStates() {
        if (isUsingPlayerSettings()) {
            modeButton.overlay(IKey.lang("gui.backpack.settings_context_player"));
        } else {
            modeButton.overlay(IKey.lang("gui.backpack.settings_context_backpack"));
        }
        shiftClickButton.setIndex(getShiftClickIntoOpenTabValue() ? 1 : 0);
        tabButton.setIndex(getKeepTabValue() ? 0 : 1);
        searchButton.setIndex(getKeepSearchValue() ? 1 : 0);
        lockButton.setIndex(getLockStorageValue() ? 1 : 0);
        mainColorPreview.color(Color.withAlpha(wrapper.getMainColor(), 255));
        accentColorPreview.color(Color.withAlpha(wrapper.getAccentColor(), 255));
    }

    private void updateWrapper() {
        this.panel.getStorageSH()
            .syncToServer(BackpackSH.getId(BackpackSHRegisters.UPDATE_SETTING), buffer -> {
                buffer.writeBoolean(wrapper.isUsePlayerSettings());
                buffer.writeBoolean(getLockStorageValue());
                buffer.writeStringToBuffer(
                    panel.getPlayer()
                        .getUniqueID()
                        .toString());
                buffer.writeBoolean(getKeepTabValue());
                buffer.writeBoolean(getShiftClickIntoOpenTabValue());
                buffer.writeBoolean(getKeepSearchValue());
                buffer.writeInt(wrapper.getMainColor());
                buffer.writeInt(wrapper.getAccentColor());
            });
    }
}
