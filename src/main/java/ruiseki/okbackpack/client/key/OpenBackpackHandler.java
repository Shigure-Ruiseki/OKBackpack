package ruiseki.okbackpack.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.utils.Platform;

import ruiseki.okbackpack.client.gui.interaction.BackpackGuiOpenHelper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
import ruiseki.okcore.client.key.IKeyHandler;

public class OpenBackpackHandler implements IKeyHandler {

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        EntityPlayer player = Platform.getClientPlayer();
        BackpackGuiOpenHelper.openFirstClient(player, BackpackEntityHelper.SearchOrder.BAUBLES_THEN_PLAYER);
    }
}
