package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widget.Widget;
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
        super(slotIndex, 4, stack, titleKey, 70);
        this.wrapper = wrapper;

        this.syncHandler("upgrades", slotIndex);

        int topY = 25;
        int arrowY = topY + 18 + 3;
        int bottomY = arrowY + 8 + 3;
        int leftX = 8;
        int rightX = 29;

        // Input slot (containers that provide fluid to tank)
        ItemSlot inputSlot = new CustomBackgroundSlot(OKCGuiTextures.EMPTY_TANK_INPUT)
            .syncHandler("tank_slot_" + slotIndex, 0)
            .pos(leftX, topY)
            .name("tank_input_" + slotIndex);

        // Output slot (containers that receive fluid from tank)
        ItemSlot outputSlot = new CustomBackgroundSlot(OKCGuiTextures.EMPTY_TANK_OUTPUT)
            .syncHandler("tank_slot_" + slotIndex, 1)
            .pos(rightX, topY)
            .name("tank_output_" + slotIndex);

        // Down arrow indicators
        Widget<?> leftArrow = new Widget<>().size(15, 8)
            .pos(leftX + 1, arrowY)
            .background(OKBGuiTextures.TANK_SLOT_ARROW);

        Widget<?> rightArrow = new Widget<>().size(15, 8)
            .pos(rightX + 1, arrowY)
            .background(OKBGuiTextures.TANK_SLOT_ARROW);

        // Input result slot (emptied containers go here)
        ItemSlot inputResultSlot = new ItemSlot().syncHandler("tank_slot_" + slotIndex, 2)
            .pos(leftX, bottomY)
            .name("tank_input_result_" + slotIndex);

        // Output result slot (filled containers go here)
        ItemSlot outputResultSlot = new ItemSlot().syncHandler("tank_slot_" + slotIndex, 3)
            .pos(rightX, bottomY)
            .name("tank_output_result_" + slotIndex);

        child(inputSlot);
        child(outputSlot);
        child(leftArrow);
        child(rightArrow);
        child(inputResultSlot);
        child(outputResultSlot);

        height(bottomY + 18 + 8);
    }

    @Override
    protected TankUpgradeWrapper getWrapper() {
        return wrapper;
    }
}
