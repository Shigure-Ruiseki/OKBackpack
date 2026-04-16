package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.slot.CustomBackgroundSlot;
import ruiseki.okbackpack.common.item.tank.TankUpgradeWrapper;
import ruiseki.okcore.client.OKCGuiTextures;

public class TankUpgradeWidget extends ExpandedUpgradeTabWidget<TankUpgradeWrapper> {

    private final TankUpgradeWrapper wrapper;

    public TankUpgradeWidget(int slotIndex, TankUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, 3, stack, panel, titleKey, 70);
        this.wrapper = wrapper;

        this.syncHandler("upgrades", slotIndex);

        // Input slot (containers that provide fluid to tank)
        ItemSlot inputSlot = new CustomBackgroundSlot(OKCGuiTextures.EMPTY_TANK_INPUT)
            .syncHandler("tank_slot_" + slotIndex, 0)
            .name("tank_input_" + slotIndex);

        // Output slot (containers that receive fluid from tank)
        ItemSlot outputSlot = new CustomBackgroundSlot(OKCGuiTextures.EMPTY_TANK_OUTPUT)
            .syncHandler("tank_slot_" + slotIndex, 1)
            .name("tank_output_" + slotIndex);

        Flow firstRow = Flow.row()
            .coverChildren()
            .childPadding(4)
            .child(inputSlot)
            .child(outputSlot);

        // Down arrow indicators
        Widget<?> leftArrow = new Widget<>().size(15, 8)
            .background(OKBGuiTextures.TANK_SLOT_ARROW);

        Widget<?> rightArrow = new Widget<>().size(15, 8)
            .background(OKBGuiTextures.TANK_SLOT_ARROW);

        Flow secondRow = Flow.row()
            .coverChildren()
            .childPadding(8)
            .child(leftArrow)
            .child(rightArrow);

        // Input result slot (emptied containers go here)
        ItemSlot inputResultSlot = new ItemSlot().syncHandler("tank_slot_" + slotIndex, 2)
            .name("tank_input_result_" + slotIndex);

        // Output result slot (filled containers go here)
        ItemSlot outputResultSlot = new ItemSlot().syncHandler("tank_slot_" + slotIndex, 3)
            .name("tank_output_result_" + slotIndex);

        Flow thirdRow = Flow.row()
            .coverChildren()
            .childPadding(4)
            .child(inputResultSlot)
            .child(outputResultSlot);

        Flow column = Flow.column()
            .pos(8, 32)
            .coverChildren()
            .childPadding(3)
            .child(firstRow)
            .child(secondRow)
            .child(thirdRow);

        child(column);
    }

    @Override
    protected TankUpgradeWrapper getWrapper() {
        return wrapper;
    }
}
