package ruiseki.okbackpack.mixins.early.Minecraft;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ruiseki.okbackpack.client.gui.interaction.BackpackInventoryInteractionOverlay;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainerBackpackInteraction extends GuiScreen {

    @Shadow
    private Slot theSlot;

    @Inject(method = "func_146977_a", at = @At("TAIL"))
    private void okbackpack$renderInventoryInteractionOverlay(Slot slotIn, CallbackInfo ci) {
        if (mc == null || mc.thePlayer == null || fontRendererObj == null || slotIn == null) {
            return;
        }

        ItemStack cursorStack = mc.thePlayer.inventory.getItemStack();
        if (cursorStack == null || cursorStack.stackSize <= 0) {
            return;
        }

        BackpackInventoryInteractionOverlay.renderOverlay(fontRendererObj, mc.thePlayer, slotIn, cursorStack);
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void okbackpack$renderInventoryInteractionTooltip(int mouseX, int mouseY, float partialTicks,
        CallbackInfo ci) {
        if (mc == null || mc.thePlayer == null || fontRendererObj == null || theSlot == null) {
            return;
        }

        ItemStack cursorStack = mc.thePlayer.inventory.getItemStack();
        if (cursorStack == null || cursorStack.stackSize <= 0) {
            return;
        }

        List<String> lines = BackpackInventoryInteractionOverlay.buildTooltipLines(mc.thePlayer, theSlot, cursorStack);
        if (lines.isEmpty()) {
            return;
        }

        BackpackInventoryInteractionOverlay.renderTooltip(
            fontRendererObj,
            lines,
            mouseX,
            mouseY,
            (tooltipLines, x, y) -> this.func_146283_a(tooltipLines, x, y));
    }
}
