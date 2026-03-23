package ruiseki.okbackpack.common.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.item.PlayerInvWrapper;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import lombok.Getter;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.client.gui.container.BackpackGuiContainer;
import ruiseki.okbackpack.client.gui.slot.BackpackSlot;
import ruiseki.okbackpack.client.gui.slot.CraftingSlotInfo;
import ruiseki.okbackpack.client.gui.slot.LockedPlayerSlot;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeSlot;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.widget.AdvancedExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.AdvancedFeedingUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.AdvancedFilterUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.AdvancedMagnetUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.AdvancedVoidUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.BackpackList;
import ruiseki.okbackpack.client.gui.widget.BackpackSearchBarWidget;
import ruiseki.okbackpack.client.gui.widget.BasicExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.CraftingUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.FeedingUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.FilterUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.MagnetUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.SettingTabWidget;
import ruiseki.okbackpack.client.gui.widget.ShiftButtonWidget;
import ruiseki.okbackpack.client.gui.widget.TabWidget;
import ruiseki.okbackpack.client.gui.widget.TileWidget;
import ruiseki.okbackpack.client.gui.widget.UpgradeSlotGroupWidget;
import ruiseki.okbackpack.client.gui.widget.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.VoidUpgradeWidget;
import ruiseki.okbackpack.common.SortType;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.common.item.wrapper.AdvancedFeedingUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.AdvancedFilterUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.AdvancedMagnetUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.AdvancedUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.AdvancedVoidUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.BasicUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.CraftingUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.FeedingUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.FilterUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.IToggleable;
import ruiseki.okbackpack.common.item.wrapper.MagnetUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperFactory;
import ruiseki.okbackpack.common.item.wrapper.VoidUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class BackpackPanel extends ModularPanel {

    public static final AdaptableUITexture LAYERED_TAB_TEXTURE = (AdaptableUITexture) UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls")
        .imageSize(256, 256)
        .xy(132, 0, 124, 256)
        .adaptable(4)
        .tiled()
        .build();

    private static final List<CyclicVariantButtonWidget.Variant> SORT_TYPE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang(LangHelpers.localize("gui.backpack.sort_by_name")),
            OKBGuiTextures.SMALL_A_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.sort_by_mod_id"), OKBGuiTextures.SMALL_M_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.sort_by_count"), OKBGuiTextures.SMALL_1_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.sort_by_ore_dict"), OKBGuiTextures.SMALL_O_ICON));

    @Getter
    private final EntityPlayer player;
    @Getter
    private final PanelSyncManager syncManager;
    @Getter
    private final UISettings settings;
    @Getter
    private final BackpackWrapper wrapper;
    @Getter
    private final TileEntity tile;

    @Getter
    private final UpgradeSlotGroupWidget upgradeSlotGroupWidget;
    @Getter
    private final List<TabWidget> tabWidgets;
    private final List<ItemSlot> upgradeSlotWidgets = new ArrayList<>();

    @Getter
    private int rowSize;

    @Getter
    public final BackpackSH backpackSyncHandler;
    @Getter
    private final BackpackSlotSH[] backpackSlotSyncHandlers;
    private final UpgradeSlotSH[] upgradeSlotSyncHandlers;
    private final UpgradeSlotUpdateGroup[] upgradeSlotGroups;

    @Getter
    private final IPanelHandler settingPanel;
    @Getter
    private Column backpackInvCol;
    @Getter
    private BackpackList backpackList;
    private BackpackSearchBarWidget searchBarWidget;

    public boolean isMemorySettingTabOpened = false;
    public boolean shouldMemorizeRespectNBT = false;
    public boolean isSortingSettingTabOpened = false;
    public boolean isResetOpenedTabs = false;

    public BackpackPanel(EntityPlayer player, TileEntity tile, PanelSyncManager syncManager, UISettings settings,
        BackpackWrapper wrapper, int width, Integer slotIndex) {
        super("backpack_gui");
        this.player = player;
        this.tile = tile;
        this.syncManager = syncManager;
        this.settings = settings;
        this.wrapper = wrapper;

        this.width(width);
        int calculated = (width - 14) / ItemSlot.SIZE;
        this.rowSize = Math.max(9, Math.min(12, calculated));

        this.backpackSyncHandler = new BackpackSH(new PlayerMainInvWrapper(player.inventory), this.wrapper, this);
        this.syncManager.syncValue("backpack_wrapper", this.backpackSyncHandler);

        this.backpackSlotSyncHandlers = new BackpackSlotSH[this.wrapper.getBackpackSlots()];
        for (int i = 0; i < this.wrapper.getBackpackSlots(); i++) {
            ModularBackpackSlot slot = new ModularBackpackSlot(this.wrapper, i);
            slot.slotGroup("backpack_inventory");
            slot.changeListener((lastStack, currentStack, isClient, init) -> {
                if (isClient) {
                    searchBarWidget.research();
                    wrapper.syncToServer();
                }
            });
            BackpackSlotSH syncHandler = new BackpackSlotSH(slot, this.wrapper, this);
            this.syncManager.syncValue("backpack", i, syncHandler);
            this.backpackSlotSyncHandlers[i] = syncHandler;
        }
        this.syncManager
            .registerSlotGroup(new SlotGroup("backpack_inventory", this.wrapper.getBackpackSlots(), 100, true));

        tabWidgets = new ArrayList<>();
        this.upgradeSlotGroupWidget = new UpgradeSlotGroupWidget(this, this.wrapper.getUpgradeSlots());
        this.upgradeSlotSyncHandlers = new UpgradeSlotSH[this.wrapper.getUpgradeSlots()];
        this.upgradeSlotGroups = new UpgradeSlotUpdateGroup[this.wrapper.getUpgradeSlots()];
        for (int i = 0; i < this.wrapper.getUpgradeSlots(); i++) {
            ModularUpgradeSlot slot = new ModularUpgradeSlot(this.wrapper, i);
            slot.slotGroup("upgrade_inventory");
            UpgradeSlotSH syncHandler = new UpgradeSlotSH(slot, this.wrapper, this);
            slot.changeListener((lastStack, currentStack, isClient, init) -> {
                if (isClient) {
                    updateUpgradeWidgets();
                    wrapper.syncToServer();
                }
            });
            this.syncManager.syncValue("upgrades", i, syncHandler);
            this.upgradeSlotSyncHandlers[i] = syncHandler;
            this.upgradeSlotGroups[i] = new UpgradeSlotUpdateGroup(this, this.wrapper, i);
        }
        this.syncManager.registerSlotGroup(new SlotGroup("upgrade_inventory", 1, 99, true));

        settingPanel = this.syncManager
            .syncedPanel("setting_panel", true, (syncManager1, syncHandler) -> new BackpackSettingPanel(this));

        this.settings.customContainer(() -> new BackPackContainer(wrapper, slotIndex));
        this.settings.customGui(() -> BackpackGuiContainer::new);

        syncManager.bindPlayerInventory(player);
        this.bindPlayerInventory();
    }

    @Override
    public void onInit() {
        super.onInit();
        updateListHeight();
    }

    @Override
    public void onResized() {
        super.onResized();
        updateListHeight();
    }

    private void updateListHeight() {
        int totalSlots = wrapper.getBackpackSlots();
        int rows = (totalSlots + rowSize - 1) / rowSize;

        int screenHeight = getScreen() != null ? getScreen().getScreenArea().height : 240;

        int slotSize = ItemSlot.SIZE;

        int maxRows = (screenHeight - 136) / slotSize;
        int visibleRows = Math.min(rows, maxRows);

        // set panel height
        height(visibleRows * slotSize + 118);

        // set list height
        int backpackSlotsHeight = visibleRows * slotSize;
        backpackList.maxSize(backpackSlotsHeight);
        backpackList.scheduleResize();

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

                        backpackSyncHandler.syncToServer(BackpackSH.UPDATE_SORT_INV, buf -> {
                            for (int i = 0; i < wrapper.getBackpackSlots(); i++) {
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

                backpackSyncHandler.setSortType(nextSortType);

                backpackSyncHandler
                    .syncToServer(BackpackSH.UPDATE_SET_SORT_TYPE, buf -> buf.writeInt(nextSortType.ordinal()));

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
                        backpackSyncHandler.transferToPlayerInventory(transferMatched);
                        backpackSyncHandler.syncToServer(
                            BackpackSH.UPDATE_TRANSFER_TO_PLAYER_INV,
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
                        backpackSyncHandler.transferToBackpack(transferMatched);
                        backpackSyncHandler.syncToServer(
                            BackpackSH.UPDATE_TRANSFER_TO_BACKPACK_INV,
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
                    backpackSyncHandler.syncToServer(BackpackSH.DEPLOY_SLEEPING_BAG);
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

    public void addBackpackInventorySlots() {
        Row backpackInvRow = (Row) new Row().coverChildren()
            .alignX(0.5f)
            .top(18)
            .childPadding(4);

        backpackList = new BackpackList(this).name("backpack_slots");

        backpackInvCol = (Column) new Column().coverChildren();

        for (int i = 0; i < wrapper.getBackpackSlots(); i++) {
            int col = i % rowSize;
            int row = i / rowSize;

            BackpackSlot slot = (BackpackSlot) new BackpackSlot(this, wrapper).syncHandler("backpack", i)
                .size(ItemSlot.SIZE)
                .name("slot_" + i)
                .left(col * ItemSlot.SIZE)
                .top(row * ItemSlot.SIZE);

            backpackInvCol.child(slot);
        }

        backpackList.maxSizeRel(1f)
            .child(backpackInvCol);
        backpackInvRow.child(backpackList);

        this.child(backpackInvRow);
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
            .size(23, 10 + wrapper.getUpgradeSlots() * ItemSlot.SIZE)
            .left(-21);
        for (int i = 0; i < wrapper.getUpgradeSlots(); i++) {
            ItemSlot itemSlot = new ItemSlot().syncHandler("upgrades", i)
                .pos(5, 5 + i * ItemSlot.SIZE)
                .name("slot_" + i);
            upgradeSlotWidgets.add(itemSlot);
            upgradeSlotGroupWidget.child(itemSlot);
        }
        this.child(upgradeSlotGroupWidget);
    }

    public void addUpgradeTabs() {
        for (int i = 0; i < wrapper.getUpgradeSlots(); i++) {
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

    public void updateUpgradeWidgets() {
        int tabIndex = 0;
        Integer openedTabIndex = null;

        resetTabState();

        for (int slotIndex = 0; slotIndex < upgradeSlotWidgets.size(); slotIndex++) {
            ItemSlot slotWidget = upgradeSlotWidgets.get(slotIndex);
            ItemStack upgrade = slotWidget.getSlot()
                .getStack();
            if (!(upgrade != null && upgrade.getItem() instanceof ItemUpgrade<?>item)) {
                continue;
            }
            if (!item.hasTab()) {
                continue;
            }

            UpgradeWrapper wrapper = UpgradeWrapperFactory.createWrapper(upgrade);
            if (wrapper == null) {
                continue;
            }

            if (wrapper.isTabOpened()) {
                if (openedTabIndex != null) {
                    wrapper.setTabOpened(false);
                    upgradeSlotSyncHandlers[slotIndex]
                        .syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TAB_STATE, buf -> buf.writeBoolean(false));
                    return;
                }
                openedTabIndex = slotIndex;
            }
        }

        for (int slotIndex = 0; slotIndex < wrapper.getUpgradeSlots(); slotIndex++) {
            ItemSlot slotWidget = upgradeSlotWidgets.get(slotIndex);
            ItemStack stack = slotWidget.getSlot()
                .getStack();
            if (stack == null) {
                continue;
            }
            Item item = stack.getItem();

            if (!(item instanceof ItemUpgrade) || !((ItemUpgrade<?>) item).hasTab()) {
                continue;
            }

            TabWidget tabWidget = tabWidgets.get(tabIndex);
            UpgradeSlotUpdateGroup upgradeSlotGroup = upgradeSlotGroups[slotIndex];

            UpgradeWrapper wrapper = UpgradeWrapperFactory.createWrapper(stack);
            if (wrapper == null) {
                continue;
            }

            tabWidget.setShowExpanded(wrapper.isTabOpened());
            tabWidget.setEnabled(true);
            tabWidget.setTabIcon(
                new ItemDrawable(stack).asIcon()
                    .size(18));
            tabWidget.tooltip(
                tooltip -> tooltip.clearText()
                    .addLine(IKey.str(item.getItemStackDisplayName(stack)))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

            // spotless: off

            // Crafting
            if (wrapper instanceof CraftingUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateCraftingDelegate(upgrade);
                tabWidget.setExpandedWidget(new CraftingUpgradeWidget(slotIndex, upgrade, this));
            }

            // Feeding
            else if (wrapper instanceof AdvancedFeedingUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateAdvancedFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(new AdvancedFeedingUpgradeWidget(slotIndex, upgrade));
            } else if (wrapper instanceof FeedingUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(new FeedingUpgradeWidget(slotIndex, upgrade));
            }

            // Magnet
            else if (wrapper instanceof AdvancedMagnetUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateAdvancedFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(new AdvancedMagnetUpgradeWidget(slotIndex, upgrade));
            } else if (wrapper instanceof MagnetUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(new MagnetUpgradeWidget(slotIndex, upgrade));
            }

            // Filter
            else if (wrapper instanceof AdvancedFilterUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateAdvancedFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(new AdvancedFilterUpgradeWidget(slotIndex, upgrade));
            } else if (wrapper instanceof FilterUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(new FilterUpgradeWidget(slotIndex, upgrade));
            }

            // Void
            else if (wrapper instanceof AdvancedVoidUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateAdvancedFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(new AdvancedVoidUpgradeWidget(slotIndex, upgrade));
            } else if (wrapper instanceof VoidUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(new VoidUpgradeWidget(slotIndex, upgrade));
            }

            // Base
            else if (wrapper instanceof AdvancedUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateAdvancedFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(
                    new AdvancedExpandedTabWidget<>(slotIndex, upgrade, stack, upgrade.getSettingLangKey()));
            } else if (wrapper instanceof BasicUpgradeWrapper upgrade) {
                upgradeSlotGroup.updateFilterDelegate(upgrade);
                tabWidget.setExpandedWidget(
                    new BasicExpandedTabWidget<>(slotIndex, upgrade, stack, upgrade.getSettingLangKey()));
            }

            // spotless: on

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

            int upperBound = Math.min(openedTabIndex + covered, tabWidgets.size());

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

    private void resetTabState() {
        for (TabWidget tabWidget : tabWidgets) {
            if (tabWidget.getExpandedWidget() != null) {
                getContext().getUISettings()
                    .getRecipeViewerSettings()
                    .removeExclusionArea(tabWidget.getExpandedWidget());
            }
        }
    }

    private void disableUnusedTabWidgets(int startTabIndex) {
        for (int i = startTabIndex; i < wrapper.getUpgradeSlots(); i++) {
            TabWidget tabWidget = tabWidgets.get(i);
            if (tabWidget != null) {
                tabWidget.setEnabled(false);
            }
        }
        this.scheduleResize();
    }

    public void disableAllTabWidgets() {
        for (int i = 0; i < wrapper.getUpgradeSlots(); i++) {
            TabWidget tabWidget = tabWidgets.get(i);
            if (tabWidget != null) {
                tabWidget.setEnabled(false);
                tabWidget.setShowExpanded(false);
            }
        }
        this.scheduleResize();
    }

    private void syncToggles() {
        for (int i = 0; i < wrapper.getUpgradeSlots(); i++) {
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
        if (!wrapper.isKeepTab() && !isResetOpenedTabs) {
            for (int i = 0; i < upgradeSlotWidgets.size(); i++) {
                ItemSlot slotWidget = upgradeSlotWidgets.get(i);
                ItemStack stack = slotWidget.getSlot()
                    .getStack();
                if (stack == null || !(stack.getItem() instanceof ItemUpgrade<?>item) || !item.hasTab()) continue;

                UpgradeWrapper wrapper = UpgradeWrapperFactory.createWrapper(stack);
                if (wrapper != null && wrapper.isTabOpened()) {
                    wrapper.setTabOpened(false);
                    upgradeSlotSyncHandlers[i]
                        .syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TAB_STATE, buf -> buf.writeBoolean(false));
                }
            }
            isResetOpenedTabs = true;
        }
    }

    public BackPackContainer getBackpackContainer() {
        return (BackPackContainer) syncManager.getContainer();
    }

    public int getOpenCraftingUpgradeSlot() {
        for (int slotIndex = 0; slotIndex < wrapper.getUpgradeSlots(); slotIndex++) {
            ItemSlot slot = upgradeSlotWidgets.get(slotIndex);
            ItemStack stack = slot.getSlot()
                .getStack();
            Item item = stack.getItem();

            if (!(item instanceof ItemUpgrade<?> && ((ItemUpgrade<?>) item).hasTab())) {
                continue;
            }

            UpgradeWrapper wrapper = UpgradeWrapperFactory.createWrapper(stack);
            if (wrapper == null) continue;

            if (wrapper instanceof CraftingUpgradeWrapper && wrapper.isTabOpened()) {
                return slotIndex;
            }
        }
        return -1;
    }

    public CraftingSlotInfo getCraftingInfo(int slotIndex) {
        return upgradeSlotGroups[slotIndex].craftingInfo;
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
    }
}
