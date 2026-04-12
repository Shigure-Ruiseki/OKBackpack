package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.slot.CustomBackgroundSlot;
import ruiseki.okbackpack.common.item.battery.BatteryUpgradeWrapper;
import ruiseki.okcore.client.OKCGuiTextures;

public class BatteryUpgradeWidget extends ExpandedUpgradeTabWidget<BatteryUpgradeWrapper> {

    private final BatteryUpgradeWrapper wrapper;

    public BatteryUpgradeWidget(int slotIndex, BatteryUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, 2, stack, panel, titleKey, 65);
        this.wrapper = wrapper;

        this.syncHandler("upgrades", slotIndex);

        // Input slot (items that provide energy to battery)
        ItemSlot inputSlot = new CustomBackgroundSlot(OKCGuiTextures.EMPTY_BATTERY_INPUT)
            .syncHandler("battery_slot_" + slotIndex, 0)
            .pos(6, 0)
            .name("battery_input_" + slotIndex);

        // Output slot (items that receive energy from battery)
        ItemSlot outputSlot = new CustomBackgroundSlot(OKCGuiTextures.EMPTY_BATTERY_OUTPUT)
            .syncHandler("battery_slot_" + slotIndex, 1)
            .pos(26, 0)
            .name("battery_output_" + slotIndex);

        Column column = (Column) new Column().pos(4, 32)
            .coverChildren()
            .childPadding(2)
            .child(inputSlot)
            .child(outputSlot);

        child(column);
    }

    @Override
    protected BatteryUpgradeWrapper getWrapper() {
        return wrapper;
    }
}
