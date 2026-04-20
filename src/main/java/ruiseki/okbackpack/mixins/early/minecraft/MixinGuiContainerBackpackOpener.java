package ruiseki.okbackpack.mixins.early.minecraft;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ruiseki.okbackpack.ClientProxy;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.client.gui.interaction.BackpackGuiOpenHelpers;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainerBackpackOpener extends GuiScreen {

    @Shadow
    private Slot theSlot;

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void okbackpack$openHoveredBackpack(char typedChar, int keyCode, CallbackInfo ci) {
        KeyBinding keyBinding = ClientProxy.keyOpenBackpack;
        if (keyBinding == null || keyCode != keyBinding.getKeyCode()) {
            return;
        }

        if (mc == null || mc.thePlayer == null || mc.thePlayer.capabilities.isCreativeMode) {
            return;
        }

        Slot hoveredSlot = theSlot;
        if (hoveredSlot == null || !hoveredSlot.getHasStack()) {
            return;
        }

        if (!BackpackEntityHelpers.isBackpackStack(hoveredSlot.getStack(), false)) {
            return;
        }

        if (!BackpackGuiOpenHelpers.tryOpenClient(mc.thePlayer, hoveredSlot)) {
            return;
        }

        ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void okbackpack$openClickedBackpack(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton != 0 && mouseButton != 1) {
            return;
        }
        if (isShiftKeyDown() || mc == null || mc.thePlayer == null || mc.thePlayer.capabilities.isCreativeMode) {
            return;
        }
        if (!(mc.thePlayer.openContainer instanceof BackPackContainer)
            || mc.thePlayer.inventory.getItemStack() != null) {
            return;
        }

        if (!BackpackGuiOpenHelpers.tryOpenClient(mc.thePlayer, theSlot)) {
            return;
        }

        ci.cancel();
    }
}
