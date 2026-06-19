package ruiseki.okbackpack.mixins.early.modularui2;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.cleanroommc.modularui.widgets.slot.ItemSlot;

@Mixin(value = ItemSlot.class, remap = false)
public interface ItemSlotInvoker {

    @Invoker("renderSlotUnderlayNEI")
    void invokeRenderSlotUnderlayNEI(GuiContainer guiContainer, Slot slot);

    @Invoker("renderSlotOverlayNEI")
    void invokeRenderSlotOverlayNEI(GuiContainer guiContainer, Slot slot);
}
