package ruiseki.storagecore.client.gui.widget;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
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
import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
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
import ruiseki.storagecore.client.gui.sync.StorageSH;
import ruiseki.storagecore.client.gui.sync.StorageSHRegisters;
import ruiseki.storagecore.inventory.InventoryHandler;

public class StoragePanel extends ModularPanel {

    protected static final List<CyclicVariantButtonWidget.Variant> SORT_TYPE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang(LangHelpers.localize("gui.storage.sort_by_name")),
            OKBGuiTextures.SMALL_A_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.sort_by_mod_id"), OKBGuiTextures.SMALL_M_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.sort_by_count"), OKBGuiTextures.SMALL_1_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.sort_by_ore_dict"), OKBGuiTextures.SMALL_O_ICON));

    protected final EntityPlayer player;
    protected final PanelSyncManager syncManager;
    protected final UISettings settings;
    protected final IStorageWrapper wrapper;
    protected final TileEntity tile;

    protected StorageSH storageSH;
    protected PlayerMainInvWrapper playerInventory;

    protected int width;
    protected int rowSize;
    protected int inventoryHeight;
    protected StringValue searchValue;
    protected SearchNode searchNode;

    protected StorageList storageList;
    protected StorageSlotGrid<ItemSlot> storageSlotGrid;

    public StoragePanel(EntityPlayer player, TileEntity tile, PanelSyncManager syncManager, UISettings settings,
        IStorageWrapper wrapper, int width) {
        super("gui");

        this.player = player;
        this.tile = tile;
        this.syncManager = syncManager;
        this.settings = settings;
        this.wrapper = wrapper;

        this.width(this.width = width);
        int calculated = (width - 14) / ItemSlot.SIZE;
        this.rowSize = Math.clamp(calculated, 9, 12);

        this.getSyncManager()
            .bindPlayerInventory(player);
        this.bindPlayerInventory();

        this.constructPlayerInventory();

        this.constructStorageHandler();
        this.getSyncManager()
            .syncValue("storage_syncer", this.getStorageSH());

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
        int rows = (totalSlots + this.rowSize - 1) / this.rowSize;

        int screenHeight = getScreen() != null ? getScreen().getScreenArea().height : 240;

        int slotSize = ItemSlot.SIZE;

        int maxRows = (screenHeight - 136) / slotSize;
        int visibleRows = Math.min(rows, maxRows);

        // set panel height
        int calculatedHeight = visibleRows * slotSize + 118;
        this.height(Math.max(calculatedHeight, BackpackSettingPanel.getCompactMinimumPanelHeight()));

        // set list height
        this.inventoryHeight = visibleRows * slotSize;
        this.storageList.maxSize(this.inventoryHeight);
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
                .left(8)
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
                .child(addInventoryGrid()));
    }

    // Search/Sort
    protected ParentWidget<?> addToolBar() {
        this.searchValue = new StringValue("");
        return new ParentWidget<>().width(this.width - 22)
            .coverChildrenHeight()
            // Search
            .child(
                new TextFieldWidget().background(OKBGuiTextures.VANILLA_SEARCH_BACKGROUND)
                    .left(-2)
                    .top(2)
                    .value(this.searchValue)
                    .onUpdateListener(widget -> {
                        String search = this.searchValue.getStringValue();
                        this.searchNode = search.isEmpty() ? null : SearchParser.parse(search);
                        if (this.storageSlotGrid != null) this.storageSlotGrid.refreshGridLayout();
                    })
                    .widthRel(0.68f)
                    .height(10))
            .child(
                new ParentWidget<>().size(12)
                    .right(0)
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
                            .right(28)
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
                            }).right(14)
                                .size(12))
                    // Upgrade Index
                    .child(
                        new ShiftButtonWidget(OKBGuiTextures.DOT_UP_ARROW_ICON, OKBGuiTextures.DOT_DOWN_ARROW_ICON)
                            .onMousePressed(mouseButton -> {
                                // TODO: add change upgrade index
                                return true;
                            })
                            .size(12)
                            .right(0)));
    }

    // Inventory Grid
    protected IWidget addInventoryGrid() {
        InventoryHandler handler = this.getWrapper()
            .getInventoryHandler();
        this.syncManager.registerSlotGroup(new SlotGroup("storage_inventory", handler.getSlots(), 100, true));
        this.storageSlotGrid = StorageSlotGrid.builder()
            .matrix(this.rowSize, 6, 'I')
            .key(
                'I',
                index -> new ItemSlot().slot(new ModularItemSlot(handler, index))
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
}
