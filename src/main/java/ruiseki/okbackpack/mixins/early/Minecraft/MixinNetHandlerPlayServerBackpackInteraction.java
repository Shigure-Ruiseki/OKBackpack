package ruiseki.okbackpack.mixins.early.Minecraft;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ruiseki.okbackpack.client.gui.interaction.BackpackInventoryInteractionExecutor;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServerBackpackInteraction {

    @Shadow
    public EntityPlayerMP playerEntity;

    @Inject(method = "processClickWindow", at = @At("HEAD"), cancellable = true)
    private void okbackpack$handleInventoryInteractionPacket(C0EPacketClickWindow packetIn, CallbackInfo ci) {
        if (playerEntity == null || playerEntity.openContainer == null) {
            return;
        }

        if (playerEntity.openContainer.windowId != packetIn.func_149548_c()
            || !playerEntity.openContainer.isPlayerNotUsingContainer(playerEntity)) {
            return;
        }

        if (!BackpackInventoryInteractionExecutor.tryExecute(
            playerEntity.openContainer,
            playerEntity,
            packetIn.func_149544_d(),
            packetIn.func_149543_e(),
            packetIn.func_149542_h())) {
            return;
        }

        playerEntity.func_143004_u();
        playerEntity.playerNetServerHandler
            .sendPacket(new S32PacketConfirmTransaction(packetIn.func_149548_c(), packetIn.func_149547_f(), true));
        ci.cancel();
    }
}
