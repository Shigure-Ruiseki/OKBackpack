package ruiseki.okbackpack.common.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.AdaptableUITexture;
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
import com.cleanroommc.modularui.utils.item.PlayerInvWrapper;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageContainer;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
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
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
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
import ruiseki.okbackpack.common.SortType;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.item.ItemUpgrade;
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
    public final BackpackSlotSH[] backpackSlotSyncHandlers;
    public final UpgradeSlotSH[] upgradeSlotSyncHandlers;
    public final UpgradeSlotUpdateGroup[] upgradeSlotGroups;
    public final UpgradeSlotGroupWidget upgradeSlotGroupWidget;
    public final List<ItemSlot> upgradeSlotWidgets = new ArrayList<>();
    public final List<TabWidget> tabWidgets;
    public final ItemStack[] lastUpgradeStacks;

    public int rowSize;
    public Column backpackInvCol;
    public BackpackList backpackList;
    public BackpackSearchBarWidget searchBarWidget;

    public final IPanelHandler settingPanel;

    public boolean isMemorySettingTabOpened = false;
    public boolean shouldMemorizeRespectNBT = false;
    public boolean isSortingSettingTabOpened = false;
    public boolean isResetOpenedTabs = false;

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

        this.backpackSyncHandler = new BackpackSH(new PlayerMainInvWrapper(player.inventory), this.wrapper, this);
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

                backpackSyncHandler.setSortType(nextSortType);

                backpackSyncHandler.syncToServer(
                    BackpackSH.UPDATE_SET_SORT_TYPE,
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

        for (int i = 0; i < wrapper.getSlots(); i++) {
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
            .size(
                23,
                10 + wrapper.getUpgradeHandler()
                    .getSlots() * ItemSlot.SIZE)
            .left(-21);
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            ItemSlot itemSlot = new ItemSlot().syncHandler("upgrades", i)
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
            if (!(stack != null && stack.getItem() instanceof ItemUpgrade<?>item)) continue;
            if (!item.hasTab()) continue;

            IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
                .getWrapperInSlot(slotIndex);
            if (wrapper == null) continue;

            if (wrapper.isTabOpened()) {
                if (openedTabIndex != null) {
                    wrapper.setTabOpened(false);
                    upgradeSlotSyncHandlers[slotIndex].syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_UPGRADE_TAB_STATE),
                        buf -> { buf.writeBoolean(false); });
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
            if (!(item instanceof ItemUpgrade upgrade) || !upgrade.hasTab()) continue;

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
        if (!wrapper.keepTab && !isResetOpenedTabs) {
            for (int i = 0; i < upgradeSlotWidgets.size(); i++) {
                ItemSlot slotWidget = upgradeSlotWidgets.get(i);
                ItemStack stack = slotWidget.getSlot()
                    .getStack();
                if (stack == null || !(stack.getItem() instanceof ItemUpgrade<?>item) || !item.hasTab()) continue;

                IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
                    .getWrapperInSlot(i);
                if (wrapper != null && wrapper.isTabOpened()) {
                    wrapper.setTabOpened(false);
                    upgradeSlotSyncHandlers[i].syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_UPGRADE_TAB_STATE),
                        buf -> { buf.writeBoolean(false); });
                }
            }
            isResetOpenedTabs = true;
        }
    }

    @Override
    public IStorageContainer<?> getContainer() {
        return (IStorageContainer<?>) syncManager.getContainer();
    }

    @Override
    public @NotNull BackpackPanel getPanel() {
        return this;
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

            if (!(item instanceof ItemUpgrade<?> && ((ItemUpgrade<?>) item).hasTab())) {
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
            upgradeSlot.syncToServer(
                UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_DIRTY),
                buf -> { buf.writeBoolean(false); });
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
    public boolean shouldMemorizeRespectNBT() {
        return shouldMemorizeRespectNBT;
    }

    @Override
    public boolean isSortingSettingTabOpened() {
        return isSortingSettingTabOpened;
    }
}
