package ruiseki.okstoragecore.proxy;

import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.CommonProxyComponent;
import ruiseki.okstoragecore.OKStorageCore;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKStorageCore._instance;
    }

}
