package ruiseki.okbackpack;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import ruiseki.okbackpack.client.audio.BackpackJukeboxSoundManager;
import ruiseki.okbackpack.client.key.OpenBackpackHandler;
import ruiseki.okbackpack.client.key.PickBlockHandler;
import ruiseki.okbackpack.client.key.ToggleUpgradeHandler;
import ruiseki.okbackpack.client.key.ToolSwapHandler;
import ruiseki.okbackpack.client.renderer.BackpackContentHandler;
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
    public static KeyBinding keyToolSwap;
    public static KeyBinding keyToggleUpgrade1;
    public static KeyBinding keyToggleUpgrade2;
    public static KeyBinding keyToggleUpgrade3;
    public static KeyBinding keyToggleUpgrade4;
    public static KeyBinding keyToggleUpgrade5;

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

        keyToolSwap = KeyRegistry.newKeyBinding(getMod(), "tool_swap", Keyboard.KEY_NONE);
        keyRegistry.addKeyHandler(keyToolSwap, new ToolSwapHandler());

        keyToggleUpgrade1 = KeyRegistry.newKeyBinding(getMod(), "toggle_upgrade_1", Keyboard.KEY_NONE);
        keyRegistry.addKeyHandler(keyToggleUpgrade1, new ToggleUpgradeHandler(0));

        keyToggleUpgrade2 = KeyRegistry.newKeyBinding(getMod(), "toggle_upgrade_2", Keyboard.KEY_NONE);
        keyRegistry.addKeyHandler(keyToggleUpgrade2, new ToggleUpgradeHandler(1));

        keyToggleUpgrade3 = KeyRegistry.newKeyBinding(getMod(), "toggle_upgrade_3", Keyboard.KEY_NONE);
        keyRegistry.addKeyHandler(keyToggleUpgrade3, new ToggleUpgradeHandler(2));

        keyToggleUpgrade4 = KeyRegistry.newKeyBinding(getMod(), "toggle_upgrade_4", Keyboard.KEY_NONE);
        keyRegistry.addKeyHandler(keyToggleUpgrade4, new ToggleUpgradeHandler(3));

        keyToggleUpgrade5 = KeyRegistry.newKeyBinding(getMod(), "toggle_upgrade_5", Keyboard.KEY_NONE);
        keyRegistry.addKeyHandler(keyToggleUpgrade5, new ToggleUpgradeHandler(4));

        if (Mods.NotEnoughItems.isLoaded()) {
            new BackpackGuiOpener(keyOpenBackpack);
        }
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();;
        MinecraftForge.EVENT_BUS.register(new ItemRenderEvent());
        MinecraftForge.EVENT_BUS.register(BackpackJukeboxSoundManager.getInstance());
        if (Mods.CodeChickenCore.isLoaded()) {
            MinecraftForge.EVENT_BUS.register(new BackpackContentHandler());
        }
    }
}
