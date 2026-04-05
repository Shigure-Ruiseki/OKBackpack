package ruiseki.okbackpack;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import ruiseki.okbackpack.client.audio.BackpackJukeboxSoundManager;
import ruiseki.okbackpack.client.key.OpenBackpackHandler;
import ruiseki.okbackpack.client.key.PickBlockHandler;
import ruiseki.okbackpack.common.event.ItemRenderEvent;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.nei.BackpackGuiOpener;
import ruiseki.okcore.client.key.IKeyRegistry;
import ruiseki.okcore.client.key.KeyRegistry;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ClientProxyComponent;

public class ClientProxy extends ClientProxyComponent {

    public static KeyBinding keyOpenBackpack;
    public static KeyBinding keyBackpackPickBlock;

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
        keyRegistry.addKeyHandler(keyOpenBackpack, new OpenBackpackHandler());

        keyBackpackPickBlock = KeyRegistry.newKeyBinding(getMod(), "pick_block", Keyboard.KEY_NONE);
        keyRegistry.addKeyHandler(keyBackpackPickBlock, new PickBlockHandler());

        if (Mods.NotEnoughItems.isLoaded()) {
            new BackpackGuiOpener(keyOpenBackpack);
        }
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();;
        MinecraftForge.EVENT_BUS.register(new ItemRenderEvent());
        MinecraftForge.EVENT_BUS.register(BackpackJukeboxSoundManager.getInstance());
    }
}
