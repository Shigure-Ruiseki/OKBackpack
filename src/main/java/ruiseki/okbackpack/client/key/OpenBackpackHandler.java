package ruiseki.okbackpack.client.key;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import ruiseki.okcore.client.key.IKeyHandler;

public class OpenBackpackHandler implements IKeyHandler {

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        // BackpackGuiOpenHelpers.openFirstClient(player, BackpackEntityHelpers.SearchOrder.BAUBLES_THEN_PLAYER);
    }
}
