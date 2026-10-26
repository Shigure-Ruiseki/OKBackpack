package ruiseki.okstoragecore.proxy;

import net.minecraftforge.common.MinecraftForge;

import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.CommonProxyComponent;
import ruiseki.okstoragecore.OKStorageCore;
import ruiseki.okstoragecore.client.gui.tooltip.StorageContentHandler;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKStorageCore._instance;
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(StorageContentHandler.INSTANCE);
    }
}
