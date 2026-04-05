package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.slot.BigItemSlot;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.ShiftButtonWidget;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.item.crafting.CraftingUpgradeWrapper;

public class CraftingUpgradeWidget extends ExpandedUpgradeTabWidget<CraftingUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> INTO_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.into_backpack"), OKBGuiTextures.INTO_BACKPACK),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.into_inventory"), OKBGuiTextures.INTO_INVENTORY));

    private static final List<CyclicVariantButtonWidget.Variant> USED_BACKPACK_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.used_backpack"), OKBGuiTextures.USED_BACKPACK),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.unused_backpack"),
            OKBGuiTextures.UNUSED_BACKPACK));

    private final CraftingUpgradeWrapper wrapper;
    private ItemSlot[] craftingMatrix;
    private ItemSlot craftingResult;

    public CraftingUpgradeWidget(int slotIndex, CraftingUpgradeWrapper wrapper, ItemStack stack, IStoragePanel panel,
        String titleKey) {
        super(slotIndex, 5, stack, titleKey, 90);
        this.wrapper = wrapper;

        this.syncHandler("upgrades", slotIndex);

        CyclicVariantButtonWidget craftingDesButton = new CyclicVariantButtonWidget(
            INTO_VARIANTS,
            wrapper.getCraftingDes()
                .ordinal(),
            index -> {
                wrapper.setCraftingDes(ICraftingUpgrade.CraftingDestination.values()[index]);
                updateWrapper();
            }).size(20, 20);

        CyclicVariantButtonWidget usedBackpackButton = new CyclicVariantButtonWidget(
            USED_BACKPACK_VARIANTS,
            wrapper.isUseBackpack() ? 0 : 1,
            index -> {
                wrapper.setUseBackpack(index == 0);
                updateWrapper();
            }).size(20, 20);

        Row buttonRow = (Row) new Row().height(20)
            .child(craftingDesButton)
            .child(usedBackpackButton);

        ShiftButtonWidget rotated = new ShiftButtonWidget(OKBGuiTextures.ROTATED_RIGHT, OKBGuiTextures.ROTATED_LEFT)
            .size(16)
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    boolean clockwise = !Interactable.hasShiftDown();

                    BackpackInventoryHelpers.rotated(wrapper.getStorage(), clockwise);
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_CRAFTING_R),
                        buf -> { buf.writeBoolean(clockwise); });
                    return true;
                }
                return false;
            });

        ShiftButtonWidget grid = new ShiftButtonWidget(OKBGuiTextures.BALANCE, OKBGuiTextures.SPREAD).size(16)
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    boolean balance = !Interactable.hasShiftDown();

                    if (balance) {
                        BackpackInventoryHelpers.balance(wrapper.getStorage());
                    } else {
                        BackpackInventoryHelpers.spread(wrapper.getStorage());
                    }
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_CRAFTING_G),
                        buf -> { buf.writeBoolean(balance); });
                    return true;
                }
                return false;
            });

        ButtonWidget<?> clear = new ButtonWidget<>().overlay(OKBGuiTextures.CLEAR)
            .size(16)
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();

                    BackpackInventoryHelpers.clear(
                        panel,
                        wrapper.getStorage(),
                        wrapper.getCraftingDes()
                            .ordinal());
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_CRAFTING_C),
                        buf -> {
                            buf.writeInt(
                                wrapper.getCraftingDes()
                                    .ordinal());
                        });
                    return true;
                }
                return false;
            });

        SlotGroupWidget craftingGroupsWidget = new SlotGroupWidget().name("crafting_matrix")
            .coverChildren();

        craftingMatrix = new ItemSlot[9];
        for (int i = 0; i < 9; i++) {
            ItemSlot itemSlot = new ItemSlot().syncHandler("crafting_slot_" + slotIndex, i)
                .pos(i % 3 * 18, i / 3 * 18)
                .name("crafting_slot_" + i);

            craftingGroupsWidget.child(itemSlot);
            craftingMatrix[i] = itemSlot;
        }

        craftingResult = new BigItemSlot().syncHandler("crafting_result_" + slotIndex, 0)
            .pos(18, 18 * 3 + 9)
            .name("crafting_result_" + slotIndex);
        craftingGroupsWidget.child(craftingResult);

        Row craftingRow = (Row) new Row().coverChildrenHeight()
            .childPadding(2);
        craftingRow.child(craftingGroupsWidget)
            .child(
                new Column().coverChildren()
                    .childPadding(2)
                    .top(1)
                    .child(rotated)
                    .child(grid)
                    .child(clear));

        Column column = (Column) new Column().pos(8, 28)
            .coverChildren()
            .childPadding(2)
            .child(buttonRow)
            .child(craftingRow);

        child(column);
    }

    @Override
    protected CraftingUpgradeWrapper getWrapper() {
        return wrapper;
    }

    public void updateWrapper() {
        this.getSyncHandler()
            .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_CRAFTING), buf -> {
                NetworkUtils.writeEnumValue(buf, wrapper.getCraftingDes());
                buf.writeBoolean(wrapper.isUseBackpack());
            });
    }

}
