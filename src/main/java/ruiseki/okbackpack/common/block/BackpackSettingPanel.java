package ruiseki.okbackpack.common.block;

import static ruiseki.okbackpack.common.block.BackpackPanel.LAYERED_TAB_TEXTURE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;
import ruiseki.okbackpack.client.gui.widget.BackpackSettingWidget;
import ruiseki.okbackpack.client.gui.widget.MemorySettingWidget;
import ruiseki.okbackpack.client.gui.widget.TabWidget;
import ruiseki.okbackpack.client.gui.widget.TabWidget.ExpandDirection;
import ruiseki.okbackpack.client.gui.widget.upgrade.SortingSettingWidget;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.helpers.BackpackJsonReader;
import ruiseki.okbackpack.common.helpers.BackpackJsonWriter;
import ruiseki.okbackpack.common.helpers.BackpackMaterial;
import ruiseki.okbackpack.common.helpers.BackpackSettingsTemplate;
import ruiseki.okcore.helper.LangHelpers;

public class BackpackSettingPanel extends ModularPanel {

    private static final File SETTINGS_TEMPLATE_DIR = new File("config/" + Reference.MOD_ID + "/dump");

    private final IStoragePanel<?> parent;

    private final List<TabWidget> tabs = new ArrayList<>();
    private final TabWidget backpackTab;
    private final TabWidget sortTab;
    private final TabWidget memoryTab;

    private final List<String> availableSettingsFiles = new ArrayList<>();
    private final StringValue settingsInputValue = new StringValue("");
    private SettingsInputMode activeSettingsInput = SettingsInputMode.NONE;
    private TextFieldWidget settingsInputField;
    private ButtonWidget<?> saveButton;
    private ButtonWidget<?> exportButton;
    private ParentWidget<?> settingsButtonContainer;
    private int selectedSettingsPresetIndex = 0;
    private int selectedImportFileIndex = 0;
    private int settingsTemplateRefreshTicks = 0;

    private enum SettingsInputMode {
        NONE,
        SAVE_PRESET,
        EXPORT_FILE
    }

    public BackpackSettingPanel(IStoragePanel<?> parent) {
        super("backpack_settings");
        this.parent = parent;

        addSettingsTemplateButtons();

        size(6, parent.getArea().height).relative(parent)
            .top(0)
            .right(0);

        backpackTab = new TabWidget(1, ExpandDirection.RIGHT);
        backpackTab.tooltipStatic(
            tooltip -> tooltip.addLine(IKey.lang("gui.backpack.backpack_settings"))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
        backpackTab.setExpandedWidget(new BackpackSettingWidget(parent, this, backpackTab));
        backpackTab.setTabIcon(OKBGuiTextures.BACKPACK_ICON);

        sortTab = new TabWidget(2, ExpandDirection.RIGHT);
        sortTab.tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (sortTab.isShowExpanded()) {
                tooltip.addLine(IKey.lang("gui.backpack.sorting_settings.tooltip_open_detail"));
            } else {
                tooltip.addLine(IKey.lang("gui.backpack.sorting_settings"))
                    .addLine(
                        IKey.lang("gui.backpack.sorting_settings.tooltip_detail")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        });
        sortTab.tooltipAutoUpdate(true);
        sortTab.setExpandedWidget(new SortingSettingWidget(parent, this, sortTab));
        sortTab.setTabIcon(OKBGuiTextures.NO_SORT_ICON);

        memoryTab = new TabWidget(3, ExpandDirection.RIGHT);
        memoryTab.tooltipDynamic(tooltip -> {
            tooltip.clearText();
            if (memoryTab.isShowExpanded()) {
                tooltip.addLine(IKey.lang("gui.backpack.memory_settings.tooltip_open_detail"));
            } else {
                tooltip.addLine(IKey.lang("gui.backpack.memory_settings"))
                    .addLine(
                        IKey.lang("gui.backpack.memory_settings.tooltip_detail")
                            .style(IKey.GRAY));
            }
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        });
        memoryTab.tooltipAutoUpdate(true);
        memoryTab.setExpandedWidget(new MemorySettingWidget(parent, this, memoryTab));
        memoryTab.setTabIcon(OKBGuiTextures.BRAIN_ICON);

        tabs.add(backpackTab);
        tabs.add(sortTab);
        tabs.add(memoryTab);

        child(backpackTab).child(sortTab)
            .child(memoryTab);
    }

    private BackpackPanel getBackpackPanel() {
        return (BackpackPanel) parent.getStoragePanel();
    }

    private BackpackWrapper getWrapper() {
        return getBackpackPanel().wrapper;
    }

    private BackpackSH getBackpackSyncHandler() {
        return getBackpackPanel().backpackSyncHandler;
    }

    private void addSettingsTemplateButtons() {
        refreshAvailableSettingsFiles();

        settingsButtonContainer = new ParentWidget<>();
        settingsButtonContainer.bottom(11)
            .right(-21)
            .size(18, 90);

        saveButton = new ButtonWidget() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                cycleSettingsPreset(scrollDirection == UpOrDown.UP ? -1 : 1);
                return true;
            }
        };
        saveButton.bottom(72)
            .left(0)
            .size(18);
        saveButton.overlay(OKBGuiTextures.SAVE_TEMPLATE_ICON);
        saveButton.onMousePressed(mouseButton -> {
            if (mouseButton == 0) {
                if (activeSettingsInput == SettingsInputMode.SAVE_PRESET) {
                    submitSettingsInput();
                } else if (selectedSettingsPresetIndex >= getWrapper().getSettingsPresetCount()) {
                    openSettingsInput(SettingsInputMode.SAVE_PRESET);
                    alignInputFieldToButton(saveButton);
                } else {
                    saveCurrentSettingsPreset(getSelectedSettingsPresetEditableName());
                }
                return true;
            }
            return false;
        });
        saveButton.tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                String presetLabel;
                if (selectedSettingsPresetIndex >= getWrapper().getSettingsPresetCount()) {
                    presetLabel = EnumChatFormatting.GREEN
                        + LangHelpers.localize("gui.backpack.settings_preset_save_custom_name")
                        + EnumChatFormatting.RESET;
                } else {
                    presetLabel = EnumChatFormatting.GREEN + getSelectedSettingsPresetLabel()
                        + EnumChatFormatting.RESET;
                }
                tooltip.addLine(IKey.lang("gui.backpack.settings_preset_save", presetLabel))
                    .addLine(
                        IKey.lang("gui.backpack.settings_preset_save_controls")
                            .style(IKey.GRAY, IKey.ITALIC))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });

        ButtonWidget<?> loadButton = new ButtonWidget() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                cycleExistingPreset(scrollDirection == UpOrDown.UP ? -1 : 1);
                return true;
            }
        };
        loadButton.bottom(54)
            .left(0)
            .size(18);
        loadButton.overlay(OKBGuiTextures.LOAD_TEMPLATE_ICON);
        loadButton.onMousePressed(mouseButton -> {
            if (mouseButton == 0) {
                loadSelectedSettingsPreset();
                return true;
            }
            return false;
        });
        loadButton.tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                if (selectedSettingsPresetIndex >= getWrapper().getSettingsPresetCount()) {
                    tooltip.addLine(IKey.lang("gui.backpack.settings_preset_load_no_save"))
                        .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                    return;
                }
                String presetLabel = EnumChatFormatting.GREEN + getSelectedSettingsPresetLabel()
                    + EnumChatFormatting.RESET;
                tooltip.addLine(IKey.lang("gui.backpack.settings_preset_load", presetLabel))
                    .addLine(
                        IKey.lang("gui.backpack.settings_preset_load_controls")
                            .style(IKey.GRAY, IKey.ITALIC))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });

        ButtonWidget<?> deleteButton = new ButtonWidget() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                cycleExistingPreset(scrollDirection == UpOrDown.UP ? -1 : 1);
                return true;
            }
        };
        deleteButton.bottom(36)
            .left(0)
            .size(18);
        deleteButton.overlay(OKBGuiTextures.DELETE_TEMPLATE_ICON);
        deleteButton.onMousePressed(mouseButton -> {
            if (mouseButton == 0) {
                deleteSelectedSettingsPreset();
                return true;
            }
            return false;
        });
        deleteButton.tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                if (selectedSettingsPresetIndex >= getWrapper().getSettingsPresetCount()) {
                    tooltip.addLine(
                        IKey.lang("gui.backpack.settings_preset_delete_controls")
                            .style(IKey.GRAY, IKey.ITALIC))
                        .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                    return;
                }
                String presetLabel = EnumChatFormatting.GREEN + getSelectedSettingsPresetLabel()
                    + EnumChatFormatting.RESET;
                tooltip.addLine(IKey.lang("gui.backpack.settings_preset_delete", presetLabel))
                    .addLine(
                        IKey.lang("gui.backpack.settings_preset_delete_controls")
                            .style(IKey.GRAY, IKey.ITALIC))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });

        exportButton = new ButtonWidget<>().bottom(18)
            .left(0)
            .size(18)
            .overlay(OKBGuiTextures.EXPORT_TEMPLATE_ICON)
            .onMousePressed(mouseButton -> {
                if (mouseButton == 0) {
                    if (activeSettingsInput == SettingsInputMode.EXPORT_FILE) {
                        submitSettingsInput();
                    } else {
                        exportCurrentSettingsToFile(getSuggestedExportName());
                    }
                    return true;
                }
                return false;
            })
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                String exportName = getSuggestedExportName();
                String displayName;
                if (exportName.isEmpty()) {
                    displayName = EnumChatFormatting.GREEN
                        + LangHelpers.localize("gui.backpack.settings_preset_export_enter_name")
                        + EnumChatFormatting.RESET;
                } else {
                    displayName = EnumChatFormatting.GREEN + exportName + EnumChatFormatting.RESET;
                }
                tooltip.addLine(IKey.lang("gui.backpack.settings_preset_export", displayName))
                    .addLine(
                        IKey.lang("gui.backpack.settings_preset_export_info")
                            .style(IKey.GRAY, IKey.ITALIC))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });

        ButtonWidget<?> importButton = new ButtonWidget() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                cycleImportFile(scrollDirection == UpOrDown.UP ? -1 : 1);
                return true;
            }
        };
        importButton.bottom(0)
            .left(0)
            .size(18);
        importButton.overlay(OKBGuiTextures.IMPORT_TEMPLATE_ICON);
        importButton.onMousePressed(mouseButton -> {
            if (mouseButton == 0) {
                importSelectedSettingsFile();
                return true;
            }
            return false;
        });
        importButton.tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                if (availableSettingsFiles.isEmpty()) {
                    tooltip.addLine(IKey.lang("gui.backpack.settings_preset_no_files"));
                } else {
                    String importFile = EnumChatFormatting.GREEN + getSelectedImportFileName()
                        + EnumChatFormatting.RESET;
                    tooltip.addLine(IKey.lang("gui.backpack.settings_preset_import", importFile))
                        .addLine(
                            IKey.lang("gui.backpack.settings_preset_import_controls")
                                .style(IKey.GRAY, IKey.ITALIC));
                }
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });

        settingsInputField = new TextFieldWidget() {

            @Override
            public void setText(@NotNull String text) {
                super.setText(text);
                handler.setCursor(0, text.length(), false);
            }

            @Override
            public @NotNull Interactable.Result onKeyPressed(char character, int keyCode) {
                if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                    submitSettingsInput();
                    return Interactable.Result.SUCCESS;
                }
                if (keyCode == Keyboard.KEY_ESCAPE) {
                    closeSettingsInput();
                    return Interactable.Result.SUCCESS;
                }
                return super.onKeyPressed(character, keyCode);
            }
        }.value(settingsInputValue)
            .setMaxLength(128)
            .background(OKBGuiTextures.ANVIL_TEXT_FIELD_ENABLED)
            .size(72, 16)
            .setEnabledIf(widget -> activeSettingsInput != SettingsInputMode.NONE);

        settingsButtonContainer.child(saveButton)
            .child(loadButton)
            .child(deleteButton)
            .child(exportButton)
            .child(importButton);

        child(settingsButtonContainer);
        child(settingsInputField);
    }

    private void refreshAvailableSettingsFiles() {
        if (!SETTINGS_TEMPLATE_DIR.exists()) {
            SETTINGS_TEMPLATE_DIR.mkdirs();
        }

        availableSettingsFiles.clear();
        File[] files = SETTINGS_TEMPLATE_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                availableSettingsFiles.add(name.substring(0, name.length() - 5));
            }
        }
        Collections.sort(availableSettingsFiles);
        normalizeSelectedIndexes();
    }

    private void normalizeSelectedIndexes() {
        int presetCount = Math.max(1, getWrapper().getSettingsPresetCount() + 1);
        selectedSettingsPresetIndex = Math.max(0, Math.min(selectedSettingsPresetIndex, presetCount - 1));

        if (availableSettingsFiles.isEmpty()) {
            selectedImportFileIndex = 0;
        } else {
            selectedImportFileIndex = Math.floorMod(selectedImportFileIndex, availableSettingsFiles.size());
        }
    }

    private void cycleSettingsPreset(int delta) {
        int presetCount = Math.max(1, getWrapper().getSettingsPresetCount() + 1);
        selectedSettingsPresetIndex = Math.floorMod(selectedSettingsPresetIndex + delta, presetCount);
    }

    private void cycleExistingPreset(int delta) {
        int presetCount = getWrapper().getSettingsPresetCount();
        if (presetCount <= 0) return;
        selectedSettingsPresetIndex = Math.floorMod(selectedSettingsPresetIndex + delta, presetCount);
    }

    private void cycleImportFile(int delta) {
        refreshAvailableSettingsFiles();
        if (!availableSettingsFiles.isEmpty()) {
            selectedImportFileIndex = Math.floorMod(selectedImportFileIndex + delta, availableSettingsFiles.size());
        }
    }

    private void openSettingsInput(SettingsInputMode mode) {
        if (activeSettingsInput == mode) {
            closeSettingsInput();
            return;
        }

        activeSettingsInput = mode;
        String text = mode == SettingsInputMode.SAVE_PRESET ? getSelectedSettingsPresetEditableName()
            : getSuggestedExportName();
        settingsInputValue.setStringValue(text);
        settingsInputField.setText(text);
    }

    private void closeSettingsInput() {
        activeSettingsInput = SettingsInputMode.NONE;
        settingsInputValue.setStringValue("");
        if (settingsInputField != null) {
            settingsInputField.setText("");
        }
    }

    private void alignInputFieldToButton(ButtonWidget<?> button) {
        if (settingsInputField == null || button == null) return;
        int inputWidth = settingsInputField.getArea().width;
        int buttonAreaX = button.getArea().x;
        int leftX = buttonAreaX - inputWidth;
        settingsInputField.pos(leftX - getArea().x, button.getArea().y - getArea().y);
    }

    private void submitSettingsInput() {
        String input = settingsInputField == null ? settingsInputValue.getStringValue() : settingsInputField.getText();
        if (input == null) {
            input = "";
        }

        if (activeSettingsInput == SettingsInputMode.SAVE_PRESET) {
            saveCurrentSettingsPreset(input.trim());
        } else if (activeSettingsInput == SettingsInputMode.EXPORT_FILE) {
            exportCurrentSettingsToFile(input.trim());
        }

        closeSettingsInput();
    }

    private void saveCurrentSettingsPreset(String name) {
        BackpackWrapper wrapper = getWrapper();
        String presetName = name.isEmpty() ? getSelectedSettingsPresetEditableName() : name;
        wrapper.saveSettingsPreset(selectedSettingsPresetIndex, presetName);
        getBackpackSyncHandler()
            .syncToServer(BackpackSH.getId(BackpackSHRegisters.UPDATE_SAVE_SETTINGS_PRESET), buffer -> {
                buffer.writeInt(selectedSettingsPresetIndex);
                buffer.writeStringToBuffer(presetName == null ? "" : presetName);
            });
    }

    private void loadSelectedSettingsPreset() {
        BackpackWrapper wrapper = getWrapper();
        if (selectedSettingsPresetIndex >= wrapper.getSettingsPresetCount()) {
            return;
        }

        if (wrapper.loadSettingsPreset(selectedSettingsPresetIndex)) {
            syncLocalPlayerSettingsFromWrapper();
            getBackpackSyncHandler().syncToServer(
                BackpackSH.getId(BackpackSHRegisters.UPDATE_LOAD_SETTINGS_PRESET),
                buffer -> buffer.writeInt(selectedSettingsPresetIndex));
        }
    }

    private void deleteSelectedSettingsPreset() {
        BackpackWrapper wrapper = getWrapper();
        if (selectedSettingsPresetIndex >= wrapper.getSettingsPresetCount()) {
            return;
        }

        int indexToDelete = selectedSettingsPresetIndex;
        selectedSettingsPresetIndex = wrapper.deleteSettingsPreset(indexToDelete);
        getBackpackSyncHandler().syncToServer(
            BackpackSH.getId(BackpackSHRegisters.UPDATE_DELETE_SETTINGS_PRESET),
            buffer -> buffer.writeInt(indexToDelete));

        normalizeSelectedIndexes();
        loadSelectedSettingsPreset();
    }

    private void exportCurrentSettingsToFile(String input) {
        BackpackWrapper wrapper = getWrapper();
        refreshAvailableSettingsFiles();

        String fileName = input.isEmpty() ? getSuggestedExportName() : input;
        if (fileName.isEmpty()) {
            fileName = "settings";
        }

        BackpackMaterial material = new BackpackMaterial();
        material.setBackpackTier("Base");
        material.setSettingsFromTemplate(BackpackSettingsTemplate.fromWrapper(wrapper));

        File file = new File(SETTINGS_TEMPLATE_DIR, fileName + ".json");
        try {
            new BackpackJsonWriter(file).write(material);
        } catch (IOException e) {
            return;
        }

        refreshAvailableSettingsFiles();
        selectedImportFileIndex = availableSettingsFiles.indexOf(fileName);
        if (selectedImportFileIndex < 0) {
            selectedImportFileIndex = 0;
        }
    }

    private void importSelectedSettingsFile() {
        BackpackWrapper wrapper = getWrapper();
        refreshAvailableSettingsFiles();
        if (availableSettingsFiles.isEmpty()) {
            return;
        }

        String fileName = getSelectedImportFileName();
        if (fileName.isEmpty()) {
            return;
        }

        try {
            BackpackMaterial material = new BackpackJsonReader(new File(SETTINGS_TEMPLATE_DIR, fileName + ".json"))
                .read();
            if (material == null || !material.hasSettings()) {
                return;
            }

            String presetName = getUniqueImportedPresetName(fileName);
            BackpackSettingsTemplate template = material.toSettingsTemplate(wrapper.getSlots());
            selectedSettingsPresetIndex = wrapper.addSettingsPreset(presetName, template);
            getBackpackSyncHandler()
                .syncToServer(BackpackSH.getId(BackpackSHRegisters.UPDATE_IMPORT_SETTINGS_PRESET), buffer -> {
                    buffer.writeStringToBuffer(presetName);
                    buffer.writeNBTTagCompoundToBuffer(template.serializeNBT());
                });
        } catch (IOException e) {
            // ignore invalid files and keep current state unchanged
        }
    }

    private void syncLocalPlayerSettingsFromWrapper() {
        BackpackWrapper wrapper = getWrapper();
        if (!wrapper.isUsePlayerSettings()) {
            return;
        }

        BackpackProperty property = BackpackProperty.get(parent.getPlayer());
        if (property == null) {
            return;
        }

        property.setLockBackpack(wrapper.isLockStorage());
        property.setKeepTab(wrapper.isKeepTab());
        property.setShiftClickIntoOpenTab(wrapper.isShiftClickIntoOpenTab());
        property.setKeepSearchPhrase(wrapper.isKeepSearchPhrase());
    }

    private String getSelectedSettingsPresetEditableName() {
        BackpackWrapper wrapper = getWrapper();
        return selectedSettingsPresetIndex < wrapper.getSettingsPresetCount()
            ? wrapper.getSettingsPresetName(selectedSettingsPresetIndex)
            : "";
    }

    private String getSelectedSettingsPresetLabel() {
        String name = getSelectedSettingsPresetEditableName();
        if (selectedSettingsPresetIndex >= getWrapper().getSettingsPresetCount()) {
            return LangHelpers.localize("gui.backpack.settings_preset_new_slot");
        }
        return name == null || name.isEmpty() ? LangHelpers.localize("gui.backpack.settings_preset_unnamed") : name;
    }

    private String getSuggestedExportName() {
        String name = getSelectedSettingsPresetEditableName();
        return name == null || name.trim()
            .isEmpty() ? "" : name.trim();
    }

    private String getSelectedImportFileName() {
        if (availableSettingsFiles.isEmpty()) {
            return "";
        }
        return availableSettingsFiles.get(selectedImportFileIndex);
    }

    private String getUniqueImportedPresetName(String baseName) {
        BackpackWrapper wrapper = getWrapper();
        String candidate = (baseName == null || baseName.trim()
            .isEmpty()) ? "imported" : baseName.trim();
        List<String> existingNames = new ArrayList<>();
        for (int i = 0; i < wrapper.getSettingsPresetCount(); i++) {
            existingNames.add(wrapper.getSettingsPresetName(i));
        }

        if (!existingNames.contains(candidate)) {
            return candidate;
        }

        int suffix = 2;
        while (existingNames.contains(candidate + " (" + suffix + ")")) {
            suffix++;
        }
        return candidate + " (" + suffix + ")";
    }

    public void updateTabState(int openIndex) {
        TabWidget[] tabs = { backpackTab, sortTab, memoryTab };

        for (TabWidget tab : tabs) {
            tab.setEnabled(true);
        }

        for (int i = 0; i < tabs.length; i++) {
            if (i != openIndex) {
                tabs[i].setShowExpanded(false);
            }
        }

        if (tabs[openIndex].isShowExpanded()) {
            for (int i = openIndex + 1; i < tabs.length; i++) {
                tabs[i].setEnabled(false);
            }
        }
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public void onInit() {
        super.onInit();
        if (settingsButtonContainer != null) {
            getContext().getUISettings()
                .getRecipeViewerSettings()
                .addExclusionArea(settingsButtonContainer);
        }
    }

    @Override
    public void onOpen(ModularScreen screen) {
        super.onOpen(screen);
        parent.setMemorySettingTabOpened(memoryTab.isShowExpanded());
        parent.setShouldMemorizeRespectNBT(((MemorySettingWidget) memoryTab.getExpandedWidget()).isRespectNBT());
        parent.setSortingSettingTabOpened(sortTab.isShowExpanded());
    }

    @Override
    public void onClose() {
        super.onClose();
        closeSettingsInput();
        parent.setMemorySettingTabOpened(false);
        parent.setShouldMemorizeRespectNBT(false);
        parent.setSortingSettingTabOpened(false);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (saveButton != null && exportButton != null) {
            if (saveButton.isHovering() && selectedSettingsPresetIndex >= getWrapper().getSettingsPresetCount()) {
                if (activeSettingsInput != SettingsInputMode.SAVE_PRESET) {
                    openSettingsInput(SettingsInputMode.SAVE_PRESET);
                    alignInputFieldToButton(saveButton);
                }
            } else if (exportButton.isHovering()) {
                if (activeSettingsInput != SettingsInputMode.EXPORT_FILE) {
                    openSettingsInput(SettingsInputMode.EXPORT_FILE);
                    alignInputFieldToButton(exportButton);
                }
            } else if (activeSettingsInput != SettingsInputMode.NONE && (settingsInputField == null
                || (!settingsInputField.isHovering() && !settingsInputField.isFocused()))) {
                    closeSettingsInput();
                }
        }

        if (++settingsTemplateRefreshTicks >= 20) {
            settingsTemplateRefreshTicks = 0;
            refreshAvailableSettingsFiles();
        }
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        super.postDraw(context, transformed);
        LAYERED_TAB_TEXTURE.draw(
            context,
            0,
            0,
            6,
            getArea().height,
            WidgetTheme.getDefault()
                .getTheme());
    }

}
