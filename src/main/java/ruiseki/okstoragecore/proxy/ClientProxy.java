package ruiseki.okstoragecore.proxy;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ClientProxyComponent;
import ruiseki.okstoragecore.OKStorageCore;
import ruiseki.okstoragecore.upgrades.jukebox.StorageSoundHandler;

public class ClientProxy extends ClientProxyComponent {

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return OKStorageCore._instance;
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(StorageSoundHandler.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(StorageSoundHandler.INSTANCE);
    }
}
