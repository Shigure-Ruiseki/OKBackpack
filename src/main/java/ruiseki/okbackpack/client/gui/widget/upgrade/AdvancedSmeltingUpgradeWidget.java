package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.slot.BigItemSlot;
import ruiseki.okbackpack.common.item.wrapper.AdvancedSmeltingUpgradeWrapperBase;

public class AdvancedSmeltingUpgradeWidget<T extends AdvancedSmeltingUpgradeWrapperBase>
    extends ExpandedUpgradeTabWidget<T> {

    private final T wrapper;

    public AdvancedSmeltingUpgradeWidget(int slotIndex, T wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, 8, stack, titleKey, 100);
        this.wrapper = wrapper;

        AdvancedFilterWidget filterWidget = new AdvancedFilterWidget(slotIndex, wrapper, "adv_common_filter").width(88)
            .coverChildrenHeight()
            .name("adv_filter_widget");

        SlotGroupWidget furnaceGroup = new SlotGroupWidget().coverChildren()
            .pos(8, 112);

        ItemSlot inputSlot = new ItemSlot().syncHandler("smelting_slot_" + slotIndex, 0)
            .pos(0, 0)
            .name("smelting_input_" + slotIndex);
        furnaceGroup.child(inputSlot);

        ItemSlot fuelSlot = new ItemSlot().syncHandler("smelting_slot_" + slotIndex, 1)
            .pos(0, 36)
            .name("smelting_fuel_" + slotIndex);
        furnaceGroup.child(fuelSlot);

        ProgressWidget flameProgress = new ProgressWidget().size(14, 14)
            .texture(OKBGuiTextures.FURNACE_FLAME_BACKGROUND, OKBGuiTextures.FURNACE_FLAME_FOREGROUND, 14)
            .direction(ProgressWidget.Direction.UP)
            .syncHandler("smelting_fuel_handler_" + slotIndex)
            .pos(2, 20);
        furnaceGroup.child(flameProgress);

        ProgressWidget arrowProgress = new ProgressWidget().size(24, 17)
            .texture(OKBGuiTextures.FURNACE_ARROW_BACKGROUND, OKBGuiTextures.FURNACE_ARROW_FOREGROUND, 24)
            .direction(ProgressWidget.Direction.RIGHT)
            .syncHandler("smelting_progress_handler_" + slotIndex)
            .pos(24, 18);
        furnaceGroup.child(arrowProgress);

        BigItemSlot outputSlot = (BigItemSlot) new BigItemSlot().syncHandler("smelting_slot_" + slotIndex, 2)
            .pos(56, 14)
            .name("smelting_output_" + slotIndex);
        furnaceGroup.child(outputSlot);

        SlotGroupWidget fuelFilterGroup = new SlotGroupWidget().coverChildren()
            .leftRel(0.5f)
            .pos(8, 169);

        for (int i = 0; i < 4; i++) {
            ItemSlot fuelFilterSlot = ItemSlot.create(true);
            fuelFilterSlot.name("fuel_filter_" + slotIndex)
                .syncHandler("fuel_filter_" + slotIndex, i)
                .pos(i * 18, 0);
            fuelFilterGroup.child(fuelFilterSlot);
        }

        Column column = (Column) new Column().pos(8, 28)
            .coverChildren()
            .childPadding(2)
            .child(filterWidget)
            .child(furnaceGroup)
            .child(fuelFilterGroup);

        child(column);
    }

    @Override
    protected T getWrapper() {
        return wrapper;
    }
}
