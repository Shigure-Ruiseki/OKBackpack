package ruiseki.okbackpack.mixins.early.modularui2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.client.gui.interaction.BackpackGuiOpenHelpers;

@Mixin(ItemSlot.class)
public abstract class MixinItemSlotBackpackOpener {

    @Inject(method = "onMousePressed", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$openClickedBackpack(int mouseButton, CallbackInfoReturnable<Interactable.Result> cir) {
        if (mouseButton != 0 && mouseButton != 1) {
            return;
        }
        if (GuiScreen.isShiftKeyDown()) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.thePlayer.capabilities.isCreativeMode) {
            return;
        }
        if (!(minecraft.thePlayer.openContainer instanceof BackPackContainer)
            || minecraft.thePlayer.inventory.getItemStack() != null) {
            return;
        }

        ItemSlot self = (ItemSlot) (Object) this;
        if (!BackpackGuiOpenHelpers.tryOpenClient(minecraft.thePlayer, self.getSlot())) {
            return;
        }

        cir.setReturnValue(Interactable.Result.SUCCESS);
    }
}
