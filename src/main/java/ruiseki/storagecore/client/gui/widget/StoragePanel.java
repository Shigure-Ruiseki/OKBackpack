package ruiseki.storagecore.client.gui.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.ShiftButtonWidget;
import ruiseki.okbackpack.client.gui.widget.TileWidget;
import ruiseki.okbackpack.common.block.BackpackSettingPanel;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.inventory.SortBy;
import ruiseki.okcore.inventory.search.SearchNode;
import ruiseki.okcore.inventory.search.SearchParser;
import ruiseki.okcore.item.capability.wrapper.PlayerMainInvWrapper;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.container.StorageContainer;
import ruiseki.storagecore.client.gui.container.StorageGuiContainer;
import ruiseki.storagecore.client.gui.slot.ModularStorageSlot;
import ruiseki.storagecore.client.gui.slot.ModularUpgradeSlot;
import ruiseki.storagecore.client.gui.sync.StorageSH;
import ruiseki.storagecore.client.gui.sync.StorageSHRegisters;
import ruiseki.storagecore.client.gui.sync.StorageSlotSH;
import ruiseki.storagecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.storagecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.storagecore.helper.RenderHelpers;
import ruiseki.storagecore.helper.StorageInventoryHelpers;
import ruiseki.storagecore.inventory.InventoryHandler;
import ruiseki.storagecore.upgrades.IUpgradeItem;
import ruiseki.storagecore.upgrades.IUpgradeWrapper;
import ruiseki.storagecore.upgrades.UpgradeHandler;
import ruiseki.storagecore.upgrades.UpgradeSlotChangeResult;

public class StoragePanel extends ModularPanel {

    public static final AdaptableUITexture LAYERED_TAB_TEXTURE = (AdaptableUITexture) UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls")
        .imageSize(256, 256)
        .xy(132, 0, 124, 256)
        .adaptable(4)
        .tiled()
        .build();

    public static final int ERROR_DISPLAY_TICKS = 60;

    protected static final List<CyclicVariantButtonWidget.Variant> SORT_TYPE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang(LangHelpers.localize("gui.storage.sort_by_name")),
            OKBGuiTextures.SMALL_A_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.sort_by_mod_id"), OKBGuiTextures.SMALL_M_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.sort_by_count"), OKBGuiTextures.SMALL_1_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.sort_by_ore_dict"), OKBGuiTextures.SMALL_O_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.sort_by_tag"), OKBGuiTextures.SMALL_T_ICON));

    protected final EntityPlayer player;
    protected final PanelSyncManager syncManager;
    protected final UISettings settings;
    protected final IStorageWrapper wrapper;
    protected final TileEntity tile;

    protected StorageSH storageSH;
    protected StorageSlotSH[] storageSlotSHs;
    protected UpgradeSlotSH[] upgradeSlotSHs;
    protected PlayerMainInvWrapper playerInventory;

    protected int width;
    protected int gridColumn;
    protected int inventoryHeight;
    protected StringValue searchValue;
    protected SearchNode searchNode;

    protected StorageList storageList;
    protected StorageSlotGrid<ItemSlot> storageSlotGrid;
    protected UpgradeSlotGroup upgradeSlotGroup;
    protected UpgradeSlotUpdate[] upgradeSlotUpdates;
    protected List<UpgradeSlot> upgradeSlots = new ArrayList<>();
    protected List<Tab> tabs = new ArrayList<>();
    protected int tabCount = 0;

    protected IPanelHandler settingPanel;

    private long errorResultExpirationTime = 0;
    @Nullable
    private UpgradeSlotChangeResult errorUpgradeSlotChangeResult;

    public StoragePanel(EntityPlayer player, TileEntity tile, PanelSyncManager syncManager, UISettings settings,
        IStorageWrapper wrapper, int width) {
        super("gui");

        this.player = player;
        this.tile = tile;
        this.syncManager = syncManager;
        this.settings = settings;
        this.wrapper = wrapper;

        this.width(this.width = width);
        this.gridColumn = this.getWrapper()
            .getNumberOfSlotColumns();

        this.getSyncManager()
            .bindPlayerInventory(player);
        this.bindPlayerInventory();

        this.constructPlayerInventory();

        this.constructStorageHandler();
        this.getSyncManager()
            .syncValue("storage_syncer", this.getStorageSH());

        this.constructInventorySH();
        this.constructUpgradeSH();

        this.constructSettings();
    }

    protected void constructPlayerInventory() {
        this.playerInventory = new PlayerMainInvWrapper(this.getPlayer().inventory);
    }

    protected void constructStorageHandler() {
        this.storageSH = new StorageSH(this.getPlayerInventory(), this.getWrapper(), this.getPanel());
    }

    protected void constructSettings() {
        this.getSettings()
            .customContainer(() -> new StorageContainer(this.getWrapper(), this.getPanel()));
        this.getSettings()
            .customGui(() -> (container, screen) -> new StorageGuiContainer(container, screen, this.getWrapper()));
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
        int totalSlots = this.getWrapper()
            .getInventoryHandler()
            .getSlots();
        int rows = (totalSlots + this.gridColumn - 1) / this.gridColumn;

        int screenHeight = getScreen() != null ? getScreen().getScreenArea().height : 240;

        int slotSize = ItemSlot.SIZE;

        int maxRows = (screenHeight - 136) / slotSize;
        int visibleRows = Math.min(rows, maxRows);

        // set panel height
        int calculatedHeight = visibleRows * slotSize + 120;
        this.height(Math.max(calculatedHeight, BackpackSettingPanel.getCompactMinimumPanelHeight()));

        // set list height
        this.inventoryHeight = visibleRows * slotSize;
        this.storageList.height(this.inventoryHeight);
        this.storageList.scheduleResize();

        this.scheduleResize();
    }

    // Storage Name / Player Inventory
    public void addDisplayName() {
        this.child(
            new TileWidget(
                this.getWrapper()
                    .getDisplayName()).widthRel(0.8f));
        this.child(
            IKey.lang(this.getPlayer().inventory.getInventoryName())
                .asWidget()
                .left(10)
                .bottom(85));
    }

    // Main Layout
    public void addMainLayout() {
        this.child(
            Flow.column()
                .coverChildren()
                .childPadding(2)
                .pos(10, 6)
                .child(addToolBar())
                .child(addInventoryGrid())
                .child(addTransferBar()));
    }

    // Search/Sort
    protected ParentWidget<?> addToolBar() {
        this.searchValue = new StringValue("");
        return Flow.row()
            // .setEnabledIf(w -> !settingPanel.isPanelOpen())
            .widthRel(0.98f)
            .coverChildrenHeight()
            .leftRel(0f)
            // Search
            .child(
                new TextFieldWidget().background(OKBGuiTextures.VANILLA_SEARCH_BACKGROUND)
                    .leftRel(0f)
                    .value(this.searchValue)
                    .onUpdateListener(widget -> {
                        String search = this.searchValue.getStringValue();
                        this.searchNode = search.isEmpty() ? null : SearchParser.parse(search);
                        if (this.storageSlotGrid != null) this.storageSlotGrid.refreshGridLayout();
                    })
                    .widthRel(0.675f)
                    .height(12))
            .child(
                Flow.row()
                    .coverChildren()
                    .childPadding(2)
                    .rightRel(0f)
                    // Sort
                    .child(
                        new ButtonWidget<>().overlay(OKBGuiTextures.SOLID_DOWN_ARROW_ICON)
                            .onMousePressed(button -> {
                                if (button == 0) {
                                    Interactable.playButtonClickSound();
                                    this.getStorageSH()
                                        .syncToServer(StorageSHRegisters.SORT);
                                    return true;
                                }
                                return false;
                            })
                            .size(12)
                            .tooltip(tooltip -> { tooltip.addLine(IKey.lang("gui.storage.sort")); }))
                    // Sort Type
                    .child(
                        new CyclicVariantButtonWidget(
                            SORT_TYPE_VARIANTS,
                            this.getWrapper()
                                .getSortBy()
                                .ordinal(),
                            0,
                            12,
                            index -> {
                                SortBy nextSortBy = SortBy.values()[index];
                                this.getWrapper()
                                    .setSortBy(nextSortBy);
                                this.getStorageSH()
                                    .syncToServer(
                                        StorageSHRegisters.SORT_BY,
                                        buf -> NetworkUtils.writeEnumValue(buf, nextSortBy));
                                Interactable.playButtonClickSound();
                            }).size(12))
                    // Upgrade Index
                    .child(
                        new ShiftButtonWidget(OKBGuiTextures.DOT_UP_ARROW_ICON, OKBGuiTextures.DOT_DOWN_ARROW_ICON)
                            .onMousePressed(button -> {
                                if (button == 0) {
                                    int tabCount = this.tabCount;
                                    if (tabCount <= 0) return false;

                                    int currentIndex = this.getWrapper()
                                        .getTabIndex()
                                        .orElse(0);

                                    int nextIndex = currentIndex + (Interactable.hasShiftDown() ? -1 : 1);

                                    nextIndex = Math.floorMod(nextIndex, tabCount);

                                    final int index = nextIndex;

                                    Interactable.playButtonClickSound();
                                    this.getWrapper()
                                        .setTabIndex(index);
                                    this.getStorageSH()
                                        .syncToServer(StorageSHRegisters.TAB_INDEX, buf -> buf.writeInt(index));

                                    updateUpgradeWidgets();
                                    return true;
                                }
                                return false;
                            })
                            .size(12)));
    }

    // Inventory Grid
    protected StorageList addInventoryGrid() {
        int inventorySlots = this.getWrapper()
            .getInventoryHandler()
            .getSlots();
        int columns = this.getWrapper()
            .getNumberOfSlotColumns();
        int rows = (int) Math.ceil((double) inventorySlots / columns);
        this.storageSlotGrid = StorageSlotGrid.builder()
            .matrix(columns, rows, 'I')
            .maxSlots(inventorySlots)
            .key(
                'I',
                i -> new ItemSlot().syncHandler(this.getStorageSlotSH(i))
                    .setEnabledIf(slot -> {
                        if (this.searchValue.getStringValue()
                            .isEmpty()) return true;
                        ItemStack stack = slot.getSlot()
                            .getStack();
                        if (ItemHelpers.isEmpty(stack)) return false;
                        ItemStackKey key = ItemStackKey.of(stack);
                        return this.searchNode.matches(key);
                    }))
            .slotGroup("storage_inventory")
            .build();
        return this.storageList = new StorageList(this, 0).child(this.storageSlotGrid);
    }

    protected void constructInventorySH() {
        InventoryHandler handler = this.getWrapper()
            .getInventoryHandler();
        this.storageSlotSHs = new StorageSlotSH[handler.getSlots()];
        for (int i = 0; i < handler.getSlots(); i++) {
            ModularStorageSlot slot = new ModularStorageSlot(handler, i, this.getWrapper(), this.getPanel());
            slot.slotGroup("storage_inventory");
            StorageSlotSH syncHandler = new StorageSlotSH(slot, this.getWrapper(), this);
            this.getSyncManager()
                .syncValue("storages", i, syncHandler);
            this.storageSlotSHs[i] = syncHandler;
        }
        this.getSyncManager()
            .registerSlotGroup(new SlotGroup("storage_inventory", handler.getSlots(), 100, true));
    }

    // Transfer Bar
    protected ParentWidget<?> addTransferBar() {
        Flow transferRow = Flow.row()
            // .setEnabledIf(w -> !settingPanel.isPanelOpen())
            .coverChildren()
            .reverseLayout()
            .childPadding(2)
            .rightRel(0.02f);

        // Player -> Storage
        transferRow.child(
            new ShiftButtonWidget(OKBGuiTextures.DOT_UP_ARROW_ICON, OKBGuiTextures.SOLID_UP_ARROW_ICON).size(12)
                .onMousePressed(mouseButton -> {
                    if (mouseButton == 0) {
                        boolean transferMatched = !Interactable.hasShiftDown();

                        Interactable.playButtonClickSound();
                        StorageInventoryHelpers
                            .transferPlayerToStorage(this.getWrapper(), this.getPlayerInventory(), transferMatched);
                        this.getStorageSH()
                            .syncToServer(
                                StorageSHRegisters.TRANSFER_TO_STORAGE,
                                buf -> buf.writeBoolean(transferMatched));
                        return true;
                    }
                    return false;
                })
                .tooltipAutoUpdate(true)
                .tooltipDynamic(tooltip -> {
                    if (Interactable.hasShiftDown()) {
                        tooltip.addLine(IKey.lang("gui.storage.transfer_to_backpack_inv"));
                    } else {
                        tooltip.addLine(IKey.lang("gui.storage.transfer_to_backpack_inv_matched_1"))
                            .addLine(
                                IKey.lang("gui.storage.transfer_to_backpack_inv_matched_2")
                                    .style(IKey.GRAY));
                    }

                    tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                }));

        // Storage -> Player
        transferRow.child(
            new ShiftButtonWidget(OKBGuiTextures.DOT_DOWN_ARROW_ICON, OKBGuiTextures.SOLID_DOWN_ARROW_ICON).size(12)
                .onMousePressed(mouseButton -> {
                    if (mouseButton == 0) {
                        boolean transferMatched = !Interactable.hasShiftDown();

                        Interactable.playButtonClickSound();
                        StorageInventoryHelpers
                            .transferStorageToPlayer(this.getWrapper(), this.getPlayerInventory(), transferMatched);
                        this.getStorageSH()
                            .syncToServer(
                                StorageSHRegisters.TRANSFER_TO_PLAYER,
                                buf -> buf.writeBoolean(transferMatched));
                        return true;
                    }
                    return false;
                })
                .tooltipAutoUpdate(true)
                .tooltipDynamic(tooltip -> {
                    if (Interactable.hasShiftDown()) {
                        tooltip.addLine(IKey.lang("gui.storage.transfer_to_player_inv"));
                    } else {
                        tooltip.addLine(IKey.lang("gui.storage.transfer_to_player_inv_matched_1"))
                            .addLine(
                                IKey.lang("gui.storage.transfer_to_player_inv_matched_2")
                                    .style(IKey.GRAY));
                    }

                    tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                }));

        return transferRow;
    }

    public void addUpgradeSlots() {
        this.upgradeSlotGroup = new UpgradeSlotGroup(this.getWrapper(), this.getPanel());
        this.upgradeSlotGroup.name("upgrade_inventory")
            .resizer()
            .size(
                23,
                10 + this.getWrapper()
                    .getUpgradeHandler()
                    .getSlots() * ItemSlot.SIZE)
            .left(-21);
        for (int i = 0; i < this.getWrapper()
            .getUpgradeHandler()
            .getSlots(); i++) {
            UpgradeSlot slot = new UpgradeSlot(this.getWrapper(), this.getPanel(), i);
            slot.syncHandler(this.getUpgradeSlotSH(i))
                .pos(5, 5 + i * ItemSlot.SIZE)
                .name("slot_" + i);
            this.upgradeSlotGroup.child(slot);
            this.upgradeSlots.add(slot);
        }
        this.child(this.upgradeSlotGroup);
    }

    protected void constructUpgradeSH() {
        UpgradeHandler handler = this.getWrapper()
            .getUpgradeHandler();
        int slotCount = handler.getSlots();
        this.upgradeSlotSHs = new UpgradeSlotSH[slotCount];
        this.upgradeSlotUpdates = new UpgradeSlotUpdate[slotCount];
        for (int i = 0; i < handler.getSlots(); i++) {
            final int slotIndex = i;
            ModularUpgradeSlot slot = new ModularUpgradeSlot(handler, slotIndex, this.getWrapper(), this.getPanel());
            slot.slotGroup("upgrade_inventory");
            slot.changeListener((newItem, onlyAmountChanged, client, init) -> {
                if (!client) return;
                updateUpgradeWidgets();
            });
            UpgradeSlotSH syncHandler = new UpgradeSlotSH(slot, this.getWrapper(), this);
            this.syncManager.syncValue("upgrades", slotIndex, syncHandler);
            this.upgradeSlotSHs[slotIndex] = syncHandler;
            this.upgradeSlotUpdates[i] = new UpgradeSlotUpdate(this.getWrapper(), this.getPanel(), i);
        }
        this.syncManager.registerSlotGroup(new SlotGroup("upgrade_inventory", 1, 99, true));
    }

    public void addUpgradeTabs() {
        int slotCount = this.getWrapper()
            .getUpgradeHandler()
            .getSlots();

        for (int i = 0; i < slotCount; i++) {
            Tab tab = new Tab(i + 1).name("upgrade_tab_" + i);

            tab.setEnabled(false);

            this.tabs.add(tab);
        }

        for (int i = this.tabs.size() - 1; i >= 0; i--) {
            this.child(this.tabs.get(i));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void updateUpgradeWidgets() {
        int tabCount = 0;
        resetTabState();
        UpgradeHandler handler = this.getWrapper()
            .getUpgradeHandler();
        int openedId = this.getWrapper()
            .getOpenTabId()
            .orElse(-1);

        for (int slotIndex = 0; slotIndex < handler.getSlots(); slotIndex++) {
            ItemStack stack = handler.getStackInSlot(slotIndex);
            IUpgradeWrapper upgradeWrapper = handler.getSlotWrapper(slotIndex);
            if (ItemHelpers.isEmpty(stack)) continue;

            Item item = stack.getItem();
            if (!(item instanceof IUpgradeItem upgrade)) continue;
            if (upgradeWrapper == null) continue;
            if (upgradeWrapper.hideSettingsTab()) continue;

            Tab tab = tabs.get(tabCount);
            UpgradeSlotUpdate group = upgradeSlotUpdates[slotIndex];

            boolean enabled = openedId == slotIndex;
            tab.setShowExpanded(enabled);
            tab.setTabIcon(
                new ItemDrawable(stack).asIcon()
                    .size(18));
            tab.tooltip(
                t -> t.clearText()
                    .addLine(IKey.str(item.getItemStackDisplayName(stack)))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

            upgrade.getType()
                .updateWidgetDelegates(upgradeWrapper, group);
            ExpandedTab expanded = upgrade.getType()
                .getExpandedTabWidget(
                    slotIndex,
                    upgradeWrapper,
                    stack,
                    this.getWrapper(),
                    this.getPanel(),
                    upgradeWrapper.getSettingLangKey());

            if (expanded != null) {
                tab.setExpandedWidget(expanded);
                getContext().getUISettings()
                    .getRecipeViewerSettings()
                    .addExclusionArea(expanded);
            }

            tabCount++;
        }

        this.tabCount = tabCount;
        int startIndex = 0;
        if (tabCount > 0) {
            startIndex = this.getWrapper()
                .getTabIndex()
                .orElse(0);
            startIndex = Math.floorMod(startIndex, tabCount);
            this.getWrapper()
                .setTabIndex(startIndex);
        }

        Integer openedRelIndex = null;
        int coveredSize = 0;

        for (int i = 0; i < tabCount; i++) {
            Tab tab = tabs.get(i);
            if (!tab.isShowExpanded()) continue;

            openedRelIndex = Math.floorMod(i - startIndex, tabCount);

            ExpandedTab expanded = tab.getExpandedWidget();
            if (expanded != null) {
                coveredSize = expanded.getCoveredTabSize();
            }
            break;
        }

        int maxVisible = getMaxVisibleTabs();

        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);

            if (i >= tabCount) {
                tab.setEnabled(false);
                continue;
            }

            int rel = Math.floorMod(i - startIndex, tabCount);
            boolean visible = rel < maxVisible;
            boolean enabled = visible;

            if (openedRelIndex != null) {
                int dist = rel - openedRelIndex;
                if (dist > 0 && dist < coveredSize) {
                    enabled = false;
                }
            }

            tab.setEnabled(enabled);

            if (visible) {
                tab.top((rel + 1) * 30);
            }
        }

        this.syncToggles();
        this.disableUnusedTabWidgets(tabCount);
        this.scheduleResize();
    }

    private int getMaxVisibleTabs() {
        int panelHeight = this.resizer()
            .getArea().height;
        return Math.max(1, (panelHeight / 30) - 1);
    }

    private void resetTabState() {
        for (Tab tab : tabs) {
            if (tab.getExpandedWidget() != null) {
                getContext().getUISettings()
                    .getRecipeViewerSettings()
                    .removeExclusionArea(tab.getExpandedWidget());
                tab.setExpandedWidget(null);
                tab.child(null);
            }
        }
    }

    private void syncToggles() {
        for (int i = 0; i < this.getWrapper()
            .getUpgradeHandler()
            .getSlots(); i++) {
            UpgradeSlotGroup.UpgradeToggleWidget widget = this.upgradeSlotGroup.getToggleWidget(i);
            IUpgradeWrapper wrapper = widget.getUpgradeWrapper();
            if (wrapper != null) {
                widget.setEnabled(wrapper.canBeDisabled());
                widget.setToggleEnabled(wrapper.isEnabled());
            } else {
                widget.setEnabled(false);
            }
        }
    }

    private void disableUnusedTabWidgets(int startTabIndex) {
        for (int i = startTabIndex; i < this.getWrapper()
            .getUpgradeHandler()
            .getSlots(); i++) {
            Tab tab = this.tabs.get(i);
            if (tab != null) {
                tab.setEnabled(false);
            }
        }
        this.scheduleResize();
    }

    public void addSettingPanel() {
        this.settingPanel = this.getSyncManager()
            .syncedPanel(
                "setting_panel",
                true,
                (syncManager1, syncHandler) -> new StorageSettingPanel(
                    this.getWrapper(),
                    this.getPanel(),
                    syncManager1,
                    syncHandler));
        this.child(new SettingTab());
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
        RenderHelpers.renderErrorOverlay(this.getPanel());
    }

    public void updateSlotChangeError(UpgradeSlotChangeResult result) {
        if (this.getPlayer().worldObj.isRemote && !result.isSuccessful()) {
            this.errorUpgradeSlotChangeResult = result;
            this.errorResultExpirationTime = this.getPlayer().worldObj.getWorldTime() + ERROR_DISPLAY_TICKS;
        }
    }

    public Optional<UpgradeSlotChangeResult> getErrorUpgradeSlotChangeResult() {
        if (this.errorUpgradeSlotChangeResult != null
            && this.getPlayer().worldObj.getWorldTime() >= this.errorResultExpirationTime) {
            this.errorResultExpirationTime = 0;
            this.errorUpgradeSlotChangeResult = null;
        }
        return Optional.ofNullable(this.errorUpgradeSlotChangeResult);
    }

    public PanelSyncManager getSyncManager() {
        return syncManager;
    }

    public UISettings getSettings() {
        return settings;
    }

    public IStorageWrapper getWrapper() {
        return wrapper;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public TileEntity getTile() {
        return tile;
    }

    @Override
    public @NotNull StoragePanel getPanel() {
        return this;
    }

    public StorageSH getStorageSH() {
        return storageSH;
    }

    public PlayerMainInvWrapper getPlayerInventory() {
        return playerInventory;
    }

    public StorageSlotSH[] getStorageSlotSHs() {
        return this.storageSlotSHs;
    }

    public StorageSlotSH getStorageSlotSH(int i) {
        return this.getStorageSlotSHs()[i];
    }

    public UpgradeSlotSH[] getUpgradeSlotSHs() {
        return this.upgradeSlotSHs;
    }

    public UpgradeSlotSH getUpgradeSlotSH(int i) {
        return this.getUpgradeSlotSHs()[i];
    }

    public IPanelHandler getSettingPanel() {
        return settingPanel;
    }
}
