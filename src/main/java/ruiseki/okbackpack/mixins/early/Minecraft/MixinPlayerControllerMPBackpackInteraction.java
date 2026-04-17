package ruiseki.okbackpack.mixins.early.Minecraft;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ruiseki.okbackpack.client.gui.interaction.BackpackInventoryInteractionExecutor;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMPBackpackInteraction {

    @Shadow
    private NetHandlerPlayClient netClientHandler;

    @Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
    private void okbackpack$handleInventoryInteractionClick(int windowId, int slotId, int clickedButton, int mode,
        EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if (player == null || player.openContainer == null) {
            return;
        }

        if (!BackpackInventoryInteractionExecutor
            .tryExecute(player.openContainer, player, slotId, clickedButton, mode)) {
            return;
        }

        short transactionId = player.openContainer.getNextTransactionID(player.inventory);
        netClientHandler
            .addToSendQueue(new C0EPacketClickWindow(windowId, slotId, clickedButton, mode, null, transactionId));
        cir.setReturnValue(null);
    }
}
