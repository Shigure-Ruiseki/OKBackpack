package ruiseki.okbackpack.common.block;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.item.PlayerInvWrapper;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageContainer;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.SortType;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.api.wrapper.IDirtable;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.client.gui.container.BackpackGuiContainer;
import ruiseki.okbackpack.client.gui.slot.BackpackSlot;
import ruiseki.okbackpack.client.gui.slot.CraftingSlotInfo;
import ruiseki.okbackpack.client.gui.slot.LockedPlayerSlot;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeSlot;
import ruiseki.okbackpack.client.gui.slot.UpgradeSlot;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.BackpackList;
import ruiseki.okbackpack.client.gui.widget.BackpackSearchBarWidget;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.SettingTabWidget;
import ruiseki.okbackpack.client.gui.widget.ShiftButtonWidget;
import ruiseki.okbackpack.client.gui.widget.TabWidget;
import ruiseki.okbackpack.client.gui.widget.TileWidget;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotGroupWidget;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.helpers.BackpackJsonReader;
import ruiseki.okbackpack.common.helpers.BackpackJsonWriter;
import ruiseki.okbackpack.common.helpers.BackpackMaterial;
import ruiseki.okbackpack.common.helpers.BackpackSettingsTemplate;
import ruiseki.okbackpack.common.item.crafting.CraftingUpgradeWrapper;
import ruiseki.okcore.helper.ItemStackHelpers;
import ruiseki.okcore.helper.LangHelpers;

public class BackpackPanel extends ModularPanel implements IStoragePanel<BackpackPanel> {

    public static final AdaptableUITexture LAYERED_TAB_TEXTURE = (AdaptableUITexture) UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls")
        .imageSize(256, 256)
        .xy(132, 0, 124, 256)
        .adaptable(4)
        .tiled()
        .build();

    public static final int ERROR_BACKGROUND_COLOR = 0xF0100010;
    public static final int ERROR_BORDER_COLOR = 0xFFB02E26;
    public static final int ERROR_TEXT_COLOR = 0xB02E26;
    public static final int ERROR_DISPLAY_TICKS = 60;
    private static final File SETTINGS_TEMPLATE_DIR = new File("config/" + Reference.MOD_ID + "/dump");

    private static final List<CyclicVariantButtonWidget.Variant> SORT_TYPE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang(LangHelpers.localize("gui.backpack.sort_by_name")),
            OKBGuiTextures.SMALL_A_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.sort_by_mod_id"), OKBGuiTextures.SMALL_M_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.sort_by_count"), OKBGuiTextures.SMALL_1_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.sort_by_ore_dict"), OKBGuiTextures.SMALL_O_ICON));

    public final EntityPlayer player;
    public final PanelSyncManager syncManager;
    public final UISettings settings;
    public final BackpackWrapper wrapper;
    public final TileEntity tile;

    public final BackpackSH backpackSyncHandler;
    public final PlayerMainInvWrapper playerInv;
    public final BackpackSlotSH[] backpackSlotSyncHandlers;
    public final UpgradeSlotSH[] upgradeSlotSyncHandlers;
    public final UpgradeSlotUpdateGroup[] upgradeSlotGroups;
    public final UpgradeSlotGroupWidget upgradeSlotGroupWidget;
    public final List<ItemSlot> upgradeSlotWidgets = new ArrayList<>();
    public final List<TabWidget> tabWidgets;
    public final ItemStack[] lastUpgradeStacks;

    public int rowSize;
    public int slotsHeight;
    public Row slotRow;
    public BackpackList backpackList;
    public Column backpackInvCol;
    public List<Column> slotWidgets;
    public BackpackSearchBarWidget searchBarWidget;

    public final IPanelHandler settingPanel;

    public boolean isMemorySettingTabOpened = false;
    public boolean shouldMemorizeRespectNBT = false;
    public boolean isSortingSettingTabOpened = false;
    public boolean isResetOpenedTabs = false;

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

    @Nullable
    public UpgradeSlotChangeResult activeError;
    public float activeErrorSetTick;

    private enum SettingsInputMode {
        NONE,
        SAVE_PRESET,
        EXPORT_FILE
    }

    public BackpackPanel(EntityPlayer player, TileEntity tile, PanelSyncManager syncManager, UISettings settings,
        BackpackWrapper wrapper, int width, Integer backpackSlotIndex) {
        super("backpack_gui");
        this.player = player;
        this.tile = tile;
        this.syncManager = syncManager;
        this.settings = settings;
        this.wrapper = wrapper;

        this.width(width);
        int calculated = (width - 14) / ItemSlot.SIZE;
        this.rowSize = Math.max(9, Math.min(12, calculated));

        this.playerInv = new PlayerMainInvWrapper(player.inventory);
        this.backpackSyncHandler = new BackpackSH(this.playerInv, this.wrapper, this);
        this.syncManager.syncValue("backpack_wrapper", this.backpackSyncHandler);

        this.backpackSlotSyncHandlers = new BackpackSlotSH[this.wrapper.getSlots()];
        for (int i = 0; i < this.wrapper.getSlots(); i++) {
            ModularBackpackSlot slot = new ModularBackpackSlot(this.wrapper, i);
            slot.slotGroup("backpack_inventory");
            BackpackSlotSH syncHandler = new BackpackSlotSH(slot, this.wrapper, this);
            this.syncManager.syncValue("backpack", i, syncHandler);
            this.backpackSlotSyncHandlers[i] = syncHandler;

            slot.changeListener((lastStack, currentStack, isClient, init) -> {
                if (isClient) {
                    searchBarWidget.research();
                }
            });
        }
        this.syncManager.registerSlotGroup(new SlotGroup("backpack_inventory", this.wrapper.getSlots(), 100, true));

        tabWidgets = new ArrayList<>();
        int upgradeSlots = wrapper.getUpgradeHandler()
            .getSlots();
        this.upgradeSlotGroupWidget = new UpgradeSlotGroupWidget(this, upgradeSlots);
        this.upgradeSlotSyncHandlers = new UpgradeSlotSH[upgradeSlots];
        this.upgradeSlotGroups = new UpgradeSlotUpdateGroup[upgradeSlots];
        this.lastUpgradeStacks = new ItemStack[upgradeSlots];
        for (int i = 0; i < upgradeSlots; i++) {
            int slotIndex = i;

            ModularUpgradeSlot slot = new ModularUpgradeSlot(this.wrapper, i);
            slot.slotGroup("upgrade_inventory");
            UpgradeSlotSH syncHandler = new UpgradeSlotSH(slot, this.wrapper, this);
            this.syncManager.syncValue("upgrades", i, syncHandler);
            this.upgradeSlotSyncHandlers[i] = syncHandler;
            this.upgradeSlotGroups[i] = new UpgradeSlotUpdateGroup(this, this.wrapper, i);

            slot.changeListener((stack, onlyAmountChanged, client, init) -> {
                if (!client) return;
                ItemStack last = lastUpgradeStacks[slotIndex];

                boolean itemChanged = !ItemStackHelpers.areStacksEqual(last, stack, true);
                boolean tabDirty = isTabDirty(slotIndex, syncHandler);

                if (!itemChanged && !tabDirty) return;
                lastUpgradeStacks[slotIndex] = stack == null ? null : stack.copy();

                activeError = null;

                updateSlotWidgets();
                updateUpgradeWidgets();
            });
        }
        this.syncManager.registerSlotGroup(new SlotGroup("upgrade_inventory", 1, 99, true));

        settingPanel = this.syncManager
            .syncedPanel("setting_panel", true, (syncManager1, syncHandler) -> new BackpackSettingPanel(this));

        this.settings.customContainer(() -> new BackPackContainer(wrapper, backpackSlotIndex));
        this.settings.customGui(() -> BackpackGuiContainer::new);

        syncManager.bindPlayerInventory(player);
        this.bindPlayerInventory();

        syncManager.onServerTick(() -> {
            if (tile != null) return;
            if (wrapper.tick(player)) {
                syncManager.getContainer()
                    .detectAndSendChanges();
            }
        });
    }

    @Override
    public void onInit() {
        super.onInit();
        updateListHeight();
        if (settingsButtonContainer != null) {
            getContext().getUISettings()
                .getRecipeViewerSettings()
                .addExclusionArea(settingsButtonContainer);
        }
    }

    @Override
    public void onResized() {
        super.onResized();
        updateListHeight();
    }

    private void updateListHeight() {
        int totalSlots = wrapper.getSlots();
        int rows = (totalSlots + rowSize - 1) / rowSize;

        int screenHeight = getScreen() != null ? getScreen().getScreenArea().height : 240;

        int slotSize = ItemSlot.SIZE;

        int maxRows = (screenHeight - 136) / slotSize;
        int visibleRows = Math.min(rows, maxRows);

        // set panel height
        height(visibleRows * slotSize + 118);

        // set list height
        slotsHeight = visibleRows * slotSize;
        backpackList.maxSize(slotsHeight);
        backpackList.scheduleResize();

        for (Column column : slotWidgets) {
            if (column != null) {
                column.height(slotsHeight);
                column.getChildren()
                    .forEach(IWidget::scheduleResize);
                column.scheduleResize();
            }
        }

        this.scheduleResize();
    }

    public void modifyPlayerSlot(PanelSyncManager syncManager, InventoryType inventoryType, int slotIndex,
        EntityPlayer player) {
        if (inventoryType == InventoryTypes.BAUBLES) return;
        ModularSlot slot = new LockedPlayerSlot(new PlayerInvWrapper(player.inventory), slotIndex)
            .slotGroup("player_inventory");
        syncManager.itemSlot("player", slotIndex, slot);
    }

    public void addSortingButtons() {

        ShiftButtonWidget sortButton = new ShiftButtonWidget(
            OKBGuiTextures.SOLID_DOWN_ARROW_ICON,
            OKBGuiTextures.SOLID_UP_ARROW_ICON).top(4)
                .right(21)
                .size(12)
                .setEnabledIf(w -> !settingPanel.isPanelOpen())
                .onMousePressed((button) -> {
                    if (button == 0) {
                        Interactable.playButtonClickSound();
                        boolean reverse = !Interactable.hasShiftDown();

                        BackpackInventoryHelpers.sortInventory(wrapper, reverse);

                        backpackSyncHandler.syncToServer(BackpackSH.getId(BackpackSHRegisters.UPDATE_SORT_INV), buf -> {
                            for (int i = 0; i < wrapper.getSlots(); i++) {
                                buf.writeItemStackToBuffer(wrapper.getStackInSlot(i));
                            }
                        });
                        return true;
                    }
                    return false;
                })
                .tooltipStatic(
                    (tooltip) -> tooltip.addLine(IKey.lang("gui.backpack.sort_inventory"))
                        .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        CyclicVariantButtonWidget sortTypeButton = new CyclicVariantButtonWidget(
            SORT_TYPE_VARIANTS,
            wrapper.getSortType()
                .ordinal(),
            0,
            12,
            (index) -> {

                SortType nextSortType = SortType.values()[index];

                wrapper.setSortType(nextSortType);

                backpackSyncHandler.syncToServer(
                    BackpackSH.getId(BackpackSHRegisters.UPDATE_SET_SORT_TYPE),
                    buf -> NetworkUtils.writeEnumValue(buf, nextSortType));

            }).setEnabledIf(cyclicVariantButtonWidget -> !settingPanel.isPanelOpen())
                .top(4)
                .right(7)
                .size(12);
        child(sortButton).child(sortTypeButton);
    }

    public void addTransferButtons() {
        ShiftButtonWidget transferToPlayerButton = new ShiftButtonWidget(
            OKBGuiTextures.DOT_DOWN_ARROW_ICON,
            OKBGuiTextures.SOLID_DOWN_ARROW_ICON).bottom(85)
                .right(21)
                .size(12)
                .setEnabledIf(shiftButtonWidget -> !settingPanel.isPanelOpen())
                .onMousePressed(mouseButton -> {
                    if (mouseButton == 0) {
                        boolean transferMatched = !Interactable.hasShiftDown();

                        Interactable.playButtonClickSound();
                        BackpackInventoryHelpers.transferBackpackToPlayerInventory(wrapper, playerInv, transferMatched);
                        backpackSyncHandler.syncToServer(
                            BackpackSH.getId(BackpackSHRegisters.UPDATE_TRANSFER_TO_PLAYER_INV),
                            buf -> buf.writeBoolean(transferMatched));
                        return true;
                    }
                    return false;
                })
                .tooltipAutoUpdate(true)
                .tooltipDynamic(tooltip -> {
                    if (Interactable.hasShiftDown()) {
                        tooltip.addLine(IKey.lang("gui.backpack.transfer_to_player_inv"));
                    } else {
                        tooltip.addLine(IKey.lang("gui.backpack.transfer_to_player_inv_matched_1"))
                            .addLine(
                                IKey.lang("gui.backpack.transfer_to_player_inv_matched_2")
                                    .style(IKey.GRAY));
                    }

                    tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                });

        ShiftButtonWidget transferToBackpackButton = new ShiftButtonWidget(
            OKBGuiTextures.DOT_UP_ARROW_ICON,
            OKBGuiTextures.SOLID_UP_ARROW_ICON).bottom(85)
                .right(7)
                .size(12)
                .setEnabledIf(shiftButtonWidget -> !settingPanel.isPanelOpen())
                .onMousePressed(mouseButton -> {
                    if (mouseButton == 0) {
                        boolean transferMatched = !Interactable.hasShiftDown();

                        Interactable.playButtonClickSound();
                        BackpackInventoryHelpers.transferPlayerInventoryToBackpack(wrapper, playerInv, transferMatched);
                        backpackSyncHandler.syncToServer(
                            BackpackSH.getId(BackpackSHRegisters.UPDATE_TRANSFER_TO_BACKPACK_INV),
                            buf -> buf.writeBoolean(transferMatched));
                        return true;
                    }
                    return false;
                })
                .tooltipAutoUpdate(true)
                .tooltipDynamic(tooltip -> {
                    if (Interactable.hasShiftDown()) {
                        tooltip.addLine(IKey.lang("gui.backpack.transfer_to_backpack_inv"));
                    } else {
                        tooltip.addLine(IKey.lang("gui.backpack.transfer_to_backpack_inv_matched_1"))
                            .addLine(
                                IKey.lang("gui.backpack.transfer_to_backpack_inv_matched_2")
                                    .style(IKey.GRAY));
                    }

                    tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                });

        ButtonWidget<?> sleepButton = new ButtonWidget<>().bottom(84)
            .right(35)
            .size(14)
            .overlay(OKBGuiTextures.SLEEPING_BAG)
            .setEnabledIf(shiftButtonWidget -> !settingPanel.isPanelOpen())
            .onMousePressed(mouseButton -> {
                if (mouseButton == 0) {
                    backpackSyncHandler.syncToServer(BackpackSH.getId(BackpackSHRegisters.DEPLOY_SLEEPING_BAG));
                    return true;
                }
                return false;
            })
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                tooltip.addLine(IKey.lang("gui.backpack.sleeping_bag"));
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });

        child(transferToPlayerButton).child(transferToBackpackButton)
            .child(sleepButton);
    }

    public void addSettingsTemplateButtons() {
        refreshAvailableSettingsFiles();

        settingsButtonContainer = new ParentWidget<>();
        settingsButtonContainer.bottom(11)
            .right(-21)
            .size(18, 90)
            .setEnabledIf(w -> settingPanel.isPanelOpen());

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
                } else if (isSelectedPresetUnnamed()) {
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
                if (selectedSettingsPresetIndex >= wrapper.getSettingsPresetCount()) {
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
                if (selectedSettingsPresetIndex >= wrapper.getSettingsPresetCount()) {
                    tooltip.addLine(IKey.lang("gui.backpack.settings_preset_delete_controls")
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
            .bottom(101)
            .left(0)
            .size(72, 16)
            .setEnabledIf(widget -> settingPanel.isPanelOpen() && activeSettingsInput != SettingsInputMode.NONE);

        settingsButtonContainer.child(saveButton)
            .child(loadButton)
            .child(deleteButton)
            .child(exportButton)
            .child(importButton);

        child(settingsButtonContainer).child(settingsInputField);
    }

    public void addMainWidget() {
        slotRow = (Row) new Row().coverChildren()
            .top(18)
            .left(5);

        rebuildInventorySlots();
        this.child(slotRow);
    }

    public void rebuildInventorySlots() {
        int savedScroll = 0;
        if (backpackList != null && backpackList.getScrollData() != null) {
            savedScroll = backpackList.getScrollData()
                .getScroll();
        }

        slotRow.removeAll();

        backpackList = new BackpackList(this, savedScroll).name("backpack_slots");
        addInventorySlots();
        slotRow.child(backpackList);

        slotWidgets = new ArrayList<>();
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            Column colWidget = new Column();
            colWidget.name("slot_widget_colum_" + i)
                .size(0);
            slotWidgets.add(colWidget);
            slotRow.child(colWidget);
        }
        if (searchBarWidget != null) {
            searchBarWidget.cacheOriginalOrder();
            searchBarWidget.research();
        }
    }

    public void addInventorySlots() {
        int usableRowSize = getUsableRowSize();
        backpackInvCol = (Column) new Column().coverChildren();

        for (int i = 0; i < wrapper.getStackHandler()
            .getVisualSize(); i++) {
            int col = i % usableRowSize;
            int row = i / usableRowSize;

            BackpackSlot slot = (BackpackSlot) new BackpackSlot(this, wrapper).syncHandler("backpack", i)
                .size(ItemSlot.SIZE)
                .name("slot_" + i)
                .left(col * ItemSlot.SIZE)
                .top(row * ItemSlot.SIZE);

            backpackInvCol.child(slot);
        }

        backpackList.child(backpackInvCol);
    }

    public int getActiveOverlayCount() {
        int count = 0;
        int maxOverlayColumns = Math.max(rowSize - 4, 0);

        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            ItemStack stack = wrapper.getUpgradeHandler()
                .getStackInSlot(i);
            if (stack == null) continue;

            Item item = stack.getItem();
            if (!(item instanceof IUpgradeItem upgrade)) continue;

            if (upgrade.hasSlotWidget()) {
                count++;
                if (count * 2 >= maxOverlayColumns) {
                    break;
                }
            }
        }

        return count;
    }

    public int getUsableRowSize() {
        int overlayColumns = getActiveOverlayCount() * 2;
        return rowSize - overlayColumns;
    }

    public void addSearchBar() {
        searchBarWidget = (BackpackSearchBarWidget) new BackpackSearchBarWidget(this).widthRel(0.75f)
            .height(10)
            .top(5)
            .left(5);

        searchBarWidget.setEnabledIf(tf -> !settingPanel.isPanelOpen());

        child(searchBarWidget);
    }

    public void addUpgradeSlots() {
        upgradeSlotGroupWidget.name("upgrade_inventory");
        upgradeSlotGroupWidget.resizer()
            .size(
                23,
                10 + wrapper.getUpgradeHandler()
                    .getSlots() * ItemSlot.SIZE)
            .left(-21);
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            UpgradeSlot itemSlot = (UpgradeSlot) new UpgradeSlot(this, i).syncHandler("upgrades", i)
                .pos(5, 5 + i * ItemSlot.SIZE)
                .name("slot_" + i);
            upgradeSlotWidgets.add(itemSlot);
            upgradeSlotGroupWidget.child(itemSlot);
        }
        this.child(upgradeSlotGroupWidget);
    }

    public void addUpgradeTabs() {
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            TabWidget tab = new TabWidget(i + 1).name("upgrade_tab_" + i);
            tab.setEnabled(false);
            tabWidgets.add(tab);
        }

        for (int i = tabWidgets.size() - 1; i >= 0; i--) {
            child(tabWidgets.get(i));
        }
    }

    public void addSettingTab() {
        child(new SettingTabWidget());
    }

    public void addTexts() {
        child(new TileWidget(wrapper.getDisplayName()).widthRel(0.8f));
        child(
            IKey.lang(this.player.inventory.getInventoryName())
                .asWidget()
                .left(8)
                .bottom(85));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!settingPanel.isPanelOpen() && activeSettingsInput != SettingsInputMode.NONE) {
            closeSettingsInput();
        }

        if (settingPanel.isPanelOpen() && saveButton != null && exportButton != null) {
            if (exportButton.isHovering()) {
                if (activeSettingsInput != SettingsInputMode.EXPORT_FILE) {
                    openSettingsInput(SettingsInputMode.EXPORT_FILE);
                    alignInputFieldToButton(exportButton);
                }
            } else if (activeSettingsInput != SettingsInputMode.NONE
                && (settingsInputField == null || !settingsInputField.isHovering())) {
                    closeSettingsInput();
                }
        }

        if (++settingsTemplateRefreshTicks >= 20) {
            settingsTemplateRefreshTicks = 0;
            refreshAvailableSettingsFiles();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void updateUpgradeWidgets() {
        int tabIndex = 0;
        Integer openedTabIndex = null;

        resetTabState();

        for (int slotIndex = 0; slotIndex < upgradeSlotWidgets.size(); slotIndex++) {
            ItemSlot slotWidget = upgradeSlotWidgets.get(slotIndex);
            if (slotWidget.getSlot() == null) continue;
            ItemStack stack = slotWidget.getSlot()
                .getStack();
            if (!(stack != null && stack.getItem() instanceof IUpgradeItem<?>item)) continue;
            if (!item.hasTab()) continue;

            IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
                .getWrapperInSlot(slotIndex);
            if (wrapper == null) continue;

            if (wrapper.isTabOpened()) {
                if (openedTabIndex != null) {
                    wrapper.setTabOpened(false);
                    upgradeSlotSyncHandlers[slotIndex].syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_UPGRADE_TAB_STATE),
                        buf -> buf.writeBoolean(false));
                    return;
                }
                openedTabIndex = slotIndex;
            }
        }

        for (int slotIndex = 0; slotIndex < wrapper.getUpgradeHandler()
            .getSlots(); slotIndex++) {
            ItemSlot slotWidget = upgradeSlotWidgets.get(slotIndex);
            if (slotWidget.getSlot() == null) continue;
            ItemStack stack = slotWidget.getSlot()
                .getStack();
            if (stack == null) continue;

            Item item = stack.getItem();
            if (!(item instanceof IUpgradeItem upgrade)) continue;
            if (!upgrade.hasTab()) continue;

            TabWidget tabWidget = tabWidgets.get(tabIndex);
            UpgradeSlotUpdateGroup upgradeSlotGroup = upgradeSlotGroups[slotIndex];

            IUpgradeWrapper wrapper = this.wrapper.upgradeHandler.getWrapperInSlot(slotIndex);
            if (wrapper == null) continue;

            tabWidget.setShowExpanded(wrapper.isTabOpened());
            tabWidget.setEnabled(true);
            tabWidget.setTabIcon(
                new ItemDrawable(stack).asIcon()
                    .size(18));
            tabWidget.tooltip(
                tooltip -> tooltip.clearText()
                    .addLine(IKey.str(item.getItemStackDisplayName(stack)))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

            upgrade.updateWidgetDelegates(wrapper, upgradeSlotGroup);
            ExpandedTabWidget widget = upgrade
                .getExpandedTabWidget(slotIndex, wrapper, stack, this, wrapper.getSettingLangKey());

            if (widget != null) {
                tabWidget.setExpandedWidget(widget);
            }

            if (tabWidget.getExpandedWidget() != null) {
                getContext().getUISettings()
                    .getRecipeViewerSettings()
                    .addExclusionArea(tabWidget.getExpandedWidget());
            }
            tabIndex++;
        }

        if (openedTabIndex != null) {
            TabWidget openedTab = tabWidgets.get(openedTabIndex);
            int covered = openedTab.getExpandedWidget() != null ? openedTab.getExpandedWidget()
                .getCoveredTabSize() : 0;

            int upperBound = Math.min(openedTabIndex + covered - 1, tabWidgets.size());

            for (int i = openedTabIndex + 1; i < upperBound; i++) {
                tabWidgets.get(i)
                    .setEnabled(false);
            }
        }

        resetOpenedTabsIfNotKeep();

        syncToggles();
        disableUnusedTabWidgets(tabIndex);
        this.scheduleResize();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void updateSlotWidgets() {
        rebuildInventorySlots();

        for (int slotIndex = 0; slotIndex < wrapper.getUpgradeHandler()
            .getSlots(); slotIndex++) {
            ItemSlot slotWidget = upgradeSlotWidgets.get(slotIndex);
            if (slotWidget.getSlot() == null) continue;

            ItemStack stack = slotWidget.getSlot()
                .getStack();
            Column column = slotWidgets.get(slotIndex);

            column.removeAll();

            if (stack == null || !(stack.getItem() instanceof IUpgradeItem upgrade)) {
                column.size(0);
                continue;
            }

            if (!upgrade.hasSlotWidget()) {
                column.size(0);
                continue;
            }

            IUpgradeWrapper wrapper = this.wrapper.upgradeHandler.getWrapperInSlot(slotIndex);
            if (wrapper == null) continue;

            column.size(36, slotsHeight);

            UpgradeSlotUpdateGroup upgradeSlotGroup = upgradeSlotGroups[slotIndex];
            Widget widget = upgrade.getSlotWidget(slotIndex, wrapper, stack, this, wrapper.getSettingLangKey());

            upgrade.updateSlotWidgetDelegates(wrapper, upgradeSlotGroup);

            if (widget != null) {
                column.child(widget);
            }
        }
    }

    private void resetTabState() {
        for (TabWidget tabWidget : tabWidgets) {
            if (tabWidget.getExpandedWidget() != null) {
                getContext().getUISettings()
                    .getRecipeViewerSettings()
                    .removeExclusionArea(tabWidget.getExpandedWidget());
                tabWidget.setExpandedWidget(null);
                tabWidget.child(null);
            }
        }
    }

    private void disableUnusedTabWidgets(int startTabIndex) {
        for (int i = startTabIndex; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            TabWidget tabWidget = tabWidgets.get(i);
            if (tabWidget != null) {
                tabWidget.setEnabled(false);
            }
        }
        this.scheduleResize();
    }

    public void disableAllTabWidgets() {
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            TabWidget tabWidget = tabWidgets.get(i);
            if (tabWidget != null) {
                tabWidget.setEnabled(false);
                tabWidget.setShowExpanded(false);
            }
        }
        this.scheduleResize();
    }

    private void syncToggles() {
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            UpgradeSlotGroupWidget.UpgradeToggleWidget toggleWidget = upgradeSlotGroupWidget.getToggleWidget(i);
            IToggleable wrapper = toggleWidget.getWrapper();

            if (wrapper != null) {
                toggleWidget.setEnabled(true);
                toggleWidget.setToggleEnabled(wrapper.isEnabled());
            } else {
                toggleWidget.setEnabled(false);
            }
        }
    }

    public void resetOpenedTabsIfNotKeep() {
        if (wrapper.isKeepTab()) {
            isResetOpenedTabs = false;
            return;
        }

        if (!isResetOpenedTabs) {
            for (int i = 0; i < upgradeSlotWidgets.size(); i++) {
                ItemSlot slotWidget = upgradeSlotWidgets.get(i);
                ItemStack stack = slotWidget.getSlot()
                    .getStack();
                if (stack == null || !(stack.getItem() instanceof IUpgradeItem<?>item) || !item.hasTab()) continue;

                IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
                    .getWrapperInSlot(i);
                if (wrapper != null && wrapper.isTabOpened()) {
                    wrapper.setTabOpened(false);
                    upgradeSlotSyncHandlers[i].syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_UPGRADE_TAB_STATE),
                        buf -> buf.writeBoolean(false));
                }
            }
            isResetOpenedTabs = true;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        if (!wrapper.isKeepSearchPhrase()) {
            clearSearchPhrase();
        }
    }

    public void clearSearchPhrase() {
        wrapper.setSearchPhrase("");
        if (searchBarWidget != null) {
            searchBarWidget.clearSearch();
        }
        backpackSyncHandler.syncToServer(
            BackpackSH.getId(BackpackSHRegisters.UPDATE_SEARCH_PHRASE),
            buffer -> buffer.writeStringToBuffer(""));
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
        int presetCount = Math.max(1, wrapper.getSettingsPresetCount() + 1);
        selectedSettingsPresetIndex = Math.max(0, Math.min(selectedSettingsPresetIndex, presetCount - 1));

        if (availableSettingsFiles.isEmpty()) {
            selectedImportFileIndex = 0;
        } else {
            selectedImportFileIndex = Math.floorMod(selectedImportFileIndex, availableSettingsFiles.size());
        }
    }

    private void cycleSettingsPreset(int delta) {
        int presetCount = Math.max(1, wrapper.getSettingsPresetCount() + 1);
        selectedSettingsPresetIndex = Math.floorMod(selectedSettingsPresetIndex + delta, presetCount);
    }

    private void cycleExistingPreset(int delta) {
        int presetCount = wrapper.getSettingsPresetCount();
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

    private boolean isSelectedPresetUnnamed() {
        if (selectedSettingsPresetIndex >= wrapper.getSettingsPresetCount()) {
            return true;
        }
        String name = wrapper.getSettingsPresetName(selectedSettingsPresetIndex);
        return name == null || name.isEmpty();
    }

    private void alignInputFieldToButton(ButtonWidget<?> button) {
        if (settingsInputField == null || button == null) return;
        int inputWidth = settingsInputField.getArea().width;
        int buttonAreaX = button.getArea().x;
        int leftX = buttonAreaX - 1 - inputWidth;
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
        String presetName = name.isEmpty() ? getSelectedSettingsPresetEditableName() : name;
        wrapper.saveSettingsPreset(selectedSettingsPresetIndex, presetName);
        backpackSyncHandler.syncToServer(BackpackSH.getId(BackpackSHRegisters.UPDATE_SAVE_SETTINGS_PRESET), buffer -> {
            buffer.writeInt(selectedSettingsPresetIndex);
            buffer.writeStringToBuffer(presetName == null ? "" : presetName);
        });
    }

    private void loadSelectedSettingsPreset() {
        if (selectedSettingsPresetIndex >= wrapper.getSettingsPresetCount()) {
            return;
        }

        if (wrapper.loadSettingsPreset(selectedSettingsPresetIndex)) {
            syncLocalPlayerSettingsFromWrapper();
            updateUpgradeWidgets();
            backpackSyncHandler.syncToServer(
                BackpackSH.getId(BackpackSHRegisters.UPDATE_LOAD_SETTINGS_PRESET),
                buffer -> buffer.writeInt(selectedSettingsPresetIndex));
        }
    }

    private void deleteSelectedSettingsPreset() {
        if (selectedSettingsPresetIndex >= wrapper.getSettingsPresetCount()) {
            return;
        }

        selectedSettingsPresetIndex = wrapper.deleteSettingsPreset(selectedSettingsPresetIndex);
        backpackSyncHandler.syncToServer(
            BackpackSH.getId(BackpackSHRegisters.UPDATE_DELETE_SETTINGS_PRESET),
            buffer -> buffer.writeInt(selectedSettingsPresetIndex));

        if (wrapper.getSettingsPresetCount() == 0) {
            saveCurrentSettingsPreset(LangHelpers.localize("gui.backpack.settings_preset_new_slot"));
        }

        normalizeSelectedIndexes();
    }

    private void exportCurrentSettingsToFile(String input) {
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
            backpackSyncHandler
                .syncToServer(BackpackSH.getId(BackpackSHRegisters.UPDATE_IMPORT_SETTINGS_PRESET), buffer -> {
                    buffer.writeStringToBuffer(presetName);
                    buffer.writeNBTTagCompoundToBuffer(template.serializeNBT());
                });
        } catch (IOException e) {
            // ignore invalid files and keep current state unchanged
        }
    }

    private void syncLocalPlayerSettingsFromWrapper() {
        if (!wrapper.isUsePlayerSettings()) {
            return;
        }

        BackpackProperty property = BackpackProperty.get(player);
        if (property == null) {
            return;
        }

        property.setLockBackpack(wrapper.isLockStorage());
        property.setKeepTab(wrapper.isKeepTab());
        property.setShiftClickIntoOpenTab(wrapper.isShiftClickIntoOpenTab());
        property.setKeepSearchPhrase(wrapper.isKeepSearchPhrase());
    }

    private String getSelectedSettingsPresetEditableName() {
        return selectedSettingsPresetIndex < wrapper.getSettingsPresetCount()
            ? wrapper.getSettingsPresetName(selectedSettingsPresetIndex)
            : "";
    }

    private String getSelectedSettingsPresetLabel() {
        String name = getSelectedSettingsPresetEditableName();
        if (selectedSettingsPresetIndex >= wrapper.getSettingsPresetCount()) {
            return LangHelpers.localize("gui.backpack.settings_preset_new_slot");
        }
        return name == null || name.isEmpty() ? LangHelpers.localize("gui.backpack.settings_preset_unnamed") : name;
    }

    private String getSuggestedExportName() {
        String name = getSelectedSettingsPresetEditableName();
        return name == null || name.trim()
            .isEmpty() ? "" : name.trim();
    }

    private String getSuggestedExportDisplayName() {
        String name = getSuggestedExportName();
        return name.isEmpty() ? LangHelpers.localize("gui.backpack.settings_preset_export_enter_name") : name;
    }

    private String getSelectedImportFileName() {
        if (availableSettingsFiles.isEmpty()) {
            return "";
        }
        return availableSettingsFiles.get(selectedImportFileIndex);
    }

    private String getUniqueImportedPresetName(String baseName) {
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

    public int getOpenCraftingUpgradeSlot() {
        for (int slotIndex = 0; slotIndex < wrapper.getUpgradeHandler()
            .getSlots(); slotIndex++) {
            ItemSlot slot = upgradeSlotWidgets.get(slotIndex);
            if (slot.getSlot() == null) continue;
            ItemStack stack = slot.getSlot()
                .getStack();
            if (stack == null) continue;
            Item item = stack.getItem();

            if (!(item instanceof IUpgradeItem<?> && ((IUpgradeItem<?>) item).hasTab())) {
                continue;
            }

            IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
                .getWrapperInSlot(slotIndex);
            if (wrapper == null) continue;

            if (wrapper instanceof CraftingUpgradeWrapper && wrapper.isTabOpened()) {
                return slotIndex;
            }
        }
        return -1;
    }

    public CraftingSlotInfo getCraftingInfo(int slotIndex) {
        return upgradeSlotGroups[slotIndex].get("crafting_info");
    }

    private boolean isTabDirty(int slotIndex, UpgradeSlotSH upgradeSlot) {
        IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
            .getWrapperInSlot(slotIndex);
        if (!(wrapper instanceof IDirtable dirtable)) return false;
        boolean isDirty = dirtable.isDirty();
        if (isDirty) {
            upgradeSlot
                .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_DIRTY), buf -> buf.writeBoolean(false));
        }
        return isDirty;
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        super.postDraw(context, transformed);
        LAYERED_TAB_TEXTURE.draw(
            context,
            resizer().getArea().width - 6,
            0,
            6,
            resizer().getArea().height,
            WidgetTheme.getDefault()
                .getTheme());
        renderErrorOverlay(context.getPartialTicks());
    }

    public boolean isSlotInConflict(int slotIndex) {
        updateActiveError(0f);
        if (activeError == null) return false;
        for (int s : activeError.getConflictSlots()) {
            if (s == slotIndex) return true;
        }
        return false;
    }

    public boolean isInventorySlotInConflict(int slotIndex) {
        updateActiveError(0f);
        if (activeError == null) return false;
        for (int s : activeError.getInventoryConflictSlots()) {
            if (s == slotIndex) return true;
        }
        return false;
    }

    private void updateActiveError(float partialTicks) {
        // pick up new error from any upgrade slot
        for (var widget : upgradeSlotWidgets) {
            if (widget instanceof UpgradeSlot upgradeSlot
                && upgradeSlot.getSlot() instanceof ModularUpgradeSlot modularSlot) {
                UpgradeSlotChangeResult result = modularSlot.getLastChangeResult();
                if (result != null && !result.isSuccessful()) {
                    if (result != activeError) {
                        activeError = result;
                        activeErrorSetTick = getCurrentTick(partialTicks);
                    }
                    modularSlot.setLastChangeResult(null);
                    return;
                }
            }
        }
        // check expiry
        if (activeError != null && getCurrentTick(partialTicks) - activeErrorSetTick >= ERROR_DISPLAY_TICKS) {
            activeError = null;
        }
    }

    private void renderErrorOverlay(float partialTicks) {
        updateActiveError(partialTicks);
        if (activeError == null || activeError.getErrorLangKey() == null) return;

        String errorText = LangHelpers.localize(activeError.getErrorLangKey(), activeError.getErrorArgs());
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int textWidth = font.getStringWidth(errorText);

        int panelWidth = resizer().getArea().width;
        int panelHeight = resizer().getArea().height;

        int padding = 4;
        int boxWidth = textWidth + padding * 2;
        int boxHeight = font.FONT_HEIGHT + padding * 2;
        int boxX = (panelWidth - boxWidth) / 2;
        int boxY = panelHeight - 90;

        GlStateManager.disableDepth();
        // border
        GuiDraw.drawRect(boxX, boxY, boxWidth, boxHeight, ERROR_BORDER_COLOR);
        // background
        GuiDraw.drawRect(boxX + 1, boxY + 1, boxWidth - 2, boxHeight - 2, ERROR_BACKGROUND_COLOR);
        // re-enable textures after drawRect for font rendering
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        // text
        font.drawStringWithShadow(errorText, boxX + padding, boxY + padding, ERROR_TEXT_COLOR);
        GlStateManager.enableDepth();
    }

    private float getCurrentTick(float partialTicks) {
        var mc = Minecraft.getMinecraft();
        return mc.theWorld != null ? mc.theWorld.getTotalWorldTime() + partialTicks : 0;
    }

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public TileEntity getTile() {
        return tile;
    }

    @Override
    public PanelSyncManager getSyncManager() {
        return syncManager;
    }

    @Override
    public UISettings getSettings() {
        return settings;
    }

    @Override
    public IStorageWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public IPanelHandler getSettingPanel() {
        return settingPanel;
    }

    @Override
    public boolean isMemorySettingTabOpened() {
        return isMemorySettingTabOpened;
    }

    @Override
    public void setMemorySettingTabOpened(boolean opened) {
        this.isMemorySettingTabOpened = opened;
    }

    @Override
    public boolean shouldMemorizeRespectNBT() {
        return shouldMemorizeRespectNBT;
    }

    @Override
    public void setShouldMemorizeRespectNBT(boolean enabled) {
        this.shouldMemorizeRespectNBT = enabled;
    }

    @Override
    public boolean isSortingSettingTabOpened() {
        return isSortingSettingTabOpened;
    }

    @Override
    public void setSortingSettingTabOpened(boolean opened) {
        this.isSortingSettingTabOpened = opened;
    }

    @Override
    public IStorageContainer<?> getContainer() {
        return (IStorageContainer<?>) syncManager.getContainer();
    }

    @Override
    public @NotNull BackpackPanel getStoragePanel() {
        return this;
    }

    @Override
    public SyncHandler getStorageSH() {
        return backpackSyncHandler;
    }

    @Override
    public ItemSlotSH[] getStorageSlotSH() {
        return backpackSlotSyncHandlers;
    }

    @Override
    public ItemSlotSH[] getUpgradedSlotSH() {
        return upgradeSlotSyncHandlers;
    }
}
