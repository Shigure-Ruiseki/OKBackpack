package ruiseki.okstoragecore.upgrades.cooking;

import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.slot.ResizableItemSlot;
import ruiseki.okstoragecore.client.gui.OSCTextures;

public class CookingLogicControl extends ParentWidget<CookingLogicControl> {

    protected final int slotIndex;

    public CookingLogicControl(int slotIndex) {
        this.slotIndex = slotIndex;
        this.coverChildren();
        this.child(this.addInputSlot());
        this.child(this.addOutputSlot());
        this.child(this.addFuelSlot());
        this.child(this.addCookProgress());
        this.child(this.addFuelProgress());
    }

    protected ProgressWidget addCookProgress() {
        return new ProgressWidget().size(24, 17)
            .texture(OSCTextures.FURNACE_ARROW_BACKGROUND, OSCTextures.FURNACE_ARROW_FOREGROUND, 24)
            .direction(ProgressWidget.Direction.RIGHT)
            .syncHandler("cooking_cook_progress_" + slotIndex)
            .pos(20, 18)
            .name("cooking_cook_progress_" + slotIndex);
    }

    protected ProgressWidget addFuelProgress() {
        return new ProgressWidget().size(14, 14)
            .texture(OKBGuiTextures.FURNACE_FLAME_BACKGROUND, OKBGuiTextures.FURNACE_FLAME_FOREGROUND, 14)
            .direction(ProgressWidget.Direction.UP)
            .syncHandler("cooking_burn_progress_" + slotIndex)
            .pos(2, 20)
            .name("cooking_burn_progress_" + slotIndex);
    }

    protected ItemSlot addInputSlot() {
        return new ItemSlot().syncHandler("cooking_slot_" + slotIndex, CookingLogic.COOK_INPUT_SLOT)
            .pos(0, 0)
            .name("cooking_input_" + slotIndex);
    }

    protected ItemSlot addOutputSlot() {
        return new ResizableItemSlot(26).syncHandler("cooking_slot_" + slotIndex, CookingLogic.COOK_OUTPUT_SLOT)
            .pos(46, 14)
            .name("cooking_output_" + slotIndex);
    }

    protected ItemSlot addFuelSlot() {
        return new ItemSlot().syncHandler("cooking_slot_" + slotIndex, CookingLogic.FUEL_SLOT)
            .pos(0, 36)
            .name("cooking_fuel_" + slotIndex);
    }
}
