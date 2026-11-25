package ruiseki.okbackpack.client.key;

import net.minecraft.client.settings.KeyBinding;
import ruiseki.okbackpack.client.gui.SelectBackpackGuiHolder;
import ruiseki.okcore.client.key.IKeyHandler;

public class OpenBackpackHandler implements IKeyHandler {

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        SelectBackpackGuiHolder.SELECT_BACKPACK.openClient();
    }
}
