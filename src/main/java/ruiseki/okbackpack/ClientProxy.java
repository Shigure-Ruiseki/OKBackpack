package ruiseki.okbackpack;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.nei.BackpackGuiOpener;
import ruiseki.okcore.client.key.IKeyHandler;
import ruiseki.okcore.client.key.IKeyRegistry;
import ruiseki.okcore.client.key.KeyRegistry;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ClientProxyComponent;

public class ClientProxy extends ClientProxyComponent {

    public static KeyBinding keyOpenBackpack;

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return OKBackpack.instance;
    }

    @Override
    public void registerKeyBindings(IKeyRegistry keyRegistry) {
        super.registerKeyBindings(keyRegistry);

        keyOpenBackpack = KeyRegistry.newKeyBinding(getMod(), "open_backpack", Keyboard.KEY_B);
        keyRegistry.addKeyHandler(keyOpenBackpack, new IKeyHandler() {

            @Override
            public void onKeyPressed(KeyBinding keyBinding) {}
        });
        if (Mods.NotEnoughItems.isLoaded()) {
            new BackpackGuiOpener(keyOpenBackpack);
        }
    }
}
