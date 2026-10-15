package ruiseki.storagecore.client.gui.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

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
import ruiseki.storagecore.helper.StorageInventoryHelpers;
import ruiseki.storagecore.inventory.InventoryHandler;
import ruiseki.storagecore.upgrades.UpgradeHandler;

public class StoragePanel extends ModularPanel {

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
    protected List<ItemSlot> upgradeSlots = new ArrayList<>();

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
            .customContainer(() -> new StorageContainer(this.getWrapper()));
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
                            }).size(12))
                    // Upgrade Index
                    .child(
                        new ShiftButtonWidget(OKBGuiTextures.DOT_UP_ARROW_ICON, OKBGuiTextures.DOT_DOWN_ARROW_ICON)
                            .onMousePressed(mouseButton -> {
                                // TODO: add change upgrade index
                                return true;
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
            this.upgradeSlots.add(slot);
            this.upgradeSlotGroup.child(slot);
        }
        this.child(this.upgradeSlotGroup);
    }

    protected void constructUpgradeSH() {
        UpgradeHandler handler = this.getWrapper()
            .getUpgradeHandler();
        this.upgradeSlotSHs = new UpgradeSlotSH[handler.getSlots()];
        for (int i = 0; i < handler.getSlots(); i++) {
            ModularUpgradeSlot slot = new ModularUpgradeSlot(handler, i, this.getWrapper(), this.getPanel());
            slot.slotGroup("upgrade_inventory");
            UpgradeSlotSH syncHandler = new UpgradeSlotSH(slot, this.wrapper, this);
            this.syncManager.syncValue("upgrades", i, syncHandler);
            this.upgradeSlotSHs[i] = syncHandler;
        }
        this.syncManager.registerSlotGroup(new SlotGroup("upgrade_inventory", 1, 99, true));
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
}
