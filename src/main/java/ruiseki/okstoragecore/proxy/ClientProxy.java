package ruiseki.okstoragecore.proxy;

import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ClientProxyComponent;
import ruiseki.okstoragecore.OKStorageCore;

public class ClientProxy extends ClientProxyComponent {

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return OKStorageCore._instance;
    }
}
