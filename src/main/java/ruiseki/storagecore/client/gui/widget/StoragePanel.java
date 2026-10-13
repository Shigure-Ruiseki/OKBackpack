package ruiseki.storagecore.client.gui.widget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.TileWidget;
import ruiseki.okbackpack.common.block.BackpackSettingPanel;
import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.inventory.search.SearchNode;
import ruiseki.okcore.inventory.search.SearchParser;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.container.StorageContainer;
import ruiseki.storagecore.client.gui.container.StorageGuiContainer;
import ruiseki.storagecore.inventory.InventoryHandler;

public class StoragePanel extends ModularPanel {

    public final EntityPlayer player;
    public final PanelSyncManager syncManager;
    public final UISettings settings;
    public final IStorageWrapper wrapper;
    public final TileEntity tile;

    public int width;
    public int rowSize;
    public int inventoryHeight;
    public StringValue searchValue;
    public SearchNode searchNode;

    public StorageList storageList;
    public StorageSlotGrid<ItemSlot> storageSlotGrid;

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

        this.syncManager.bindPlayerInventory(player);
        this.bindPlayerInventory();

        this.constructSettings();
    }

    protected void constructSettings() {
        this.settings.customContainer(() -> new StorageContainer(wrapper));
        this.settings.customGui(() -> (container, screen) -> new StorageGuiContainer(container, screen, wrapper));
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
        int totalSlots = wrapper.getInventoryHandler()
            .getSlots();
        int rows = (totalSlots + rowSize - 1) / rowSize;

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
        this.child(new TileWidget(this.wrapper.getDisplayName()).widthRel(0.8f));
        this.child(
            IKey.lang(this.player.inventory.getInventoryName())
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
                    .value(this.searchValue)
                    .onUpdateListener(widget -> {
                        String search = this.searchValue.getStringValue();
                        this.searchNode = search.isEmpty() ? null : SearchParser.parse(search);
                        if (this.storageSlotGrid != null) this.storageSlotGrid.refreshGridLayout();
                    })
                    .widthRel(0.68f)
                    .height(10));
    }

    // Inventory Grid
    protected IWidget addInventoryGrid() {
        InventoryHandler handler = this.wrapper.getInventoryHandler();
        this.syncManager.registerSlotGroup(new SlotGroup("storage_inventory", handler.getSlots(), 100, true));
        this.storageSlotGrid = StorageSlotGrid.builder()
            .matrix(rowSize, 6, 'I')
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
}
