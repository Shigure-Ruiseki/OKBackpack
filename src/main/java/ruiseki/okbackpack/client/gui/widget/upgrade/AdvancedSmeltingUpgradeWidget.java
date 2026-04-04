package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.slot.BigItemSlot;
import ruiseki.okbackpack.client.gui.slot.FilterSlot;
import ruiseki.okbackpack.common.item.wrapper.AdvancedSmeltingUpgradeWrapperBase;

public class AdvancedSmeltingUpgradeWidget<T extends AdvancedSmeltingUpgradeWrapperBase>
    extends ExpandedUpgradeTabWidget<T> {

    private final T wrapper;

    public AdvancedSmeltingUpgradeWidget(int slotIndex, T wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, 8, stack, titleKey, 100);
        this.wrapper = wrapper;

        // Material filter (8 slots, 2 rows of 4) - same as AdvancedFilterWidget
        AdvancedFilterWidget filterWidget = new AdvancedFilterWidget(slotIndex, wrapper, "smelting_filter", 8).width(88)
            .coverChildrenHeight()
            .name("adv_filter_widget");

        // Furnace layout using SlotGroupWidget with absolute positioning
        SlotGroupWidget furnaceGroup = (SlotGroupWidget) new SlotGroupWidget().coverChildren();

        // Input slot (index 0)
        ItemSlot inputSlot = (ItemSlot) new ItemSlot().syncHandler("smelting_slot_" + slotIndex, 0)
            .pos(0, 0)
            .name("smelting_input_" + slotIndex);
        furnaceGroup.child(inputSlot);

        // Fuel slot (index 1)
        ItemSlot fuelSlot = (ItemSlot) new ItemSlot().syncHandler("smelting_slot_" + slotIndex, 1)
            .pos(0, 36)
            .name("smelting_fuel_" + slotIndex);
        furnaceGroup.child(fuelSlot);

        // Flame progress (fuel burn time remaining)
        ProgressWidget flameProgress = new ProgressWidget().size(14, 14)
            .texture(OKBGuiTextures.FURNACE_FLAME_BACKGROUND, OKBGuiTextures.FURNACE_FLAME_FOREGROUND, 14)
            .direction(ProgressWidget.Direction.UP)
            .progress(() -> {
                int total = wrapper.getFuelTotal();
                int current = wrapper.getFuelProgress();
                return total > 0 ? (float) current / total : 0f;
            })
            .pos(2, 20);
        furnaceGroup.child(flameProgress);

        // Arrow progress (smelting progress)
        ProgressWidget arrowProgress = new ProgressWidget().size(24, 17)
            .texture(OKBGuiTextures.FURNACE_ARROW_BACKGROUND, OKBGuiTextures.FURNACE_ARROW_FOREGROUND, 24)
            .direction(ProgressWidget.Direction.RIGHT)
            .progress(() -> {
                int total = wrapper.getSmeltTime();
                int current = wrapper.getSmeltProgress();
                return total > 0 ? (float) current / total : 0f;
            })
            .pos(24, 18);
        furnaceGroup.child(arrowProgress);

        // Output slot (index 2)
        BigItemSlot outputSlot = (BigItemSlot) new BigItemSlot().syncHandler("smelting_slot_" + slotIndex, 2)
            .pos(56, 14)
            .name("smelting_output_" + slotIndex);
        furnaceGroup.child(outputSlot);

        // Fuel filter (4 phantom slots in 1 row)
        SlotGroupWidget fuelFilterGroup = new SlotGroupWidget().coverChildren()
            .leftRel(0.5f);

        for (int i = 0; i < 4; i++) {
            FilterSlot fuelFilterSlot = new FilterSlot();
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
