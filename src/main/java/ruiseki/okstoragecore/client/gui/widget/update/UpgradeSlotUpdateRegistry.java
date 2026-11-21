package ruiseki.okstoragecore.client.gui.widget.update;

import java.util.ArrayList;
import java.util.List;

public class UpgradeSlotUpdateRegistry {

    private static final List<IUpgradeSlotUpdateFactory> FACTORIES = new ArrayList<>();

    public static void register(IUpgradeSlotUpdateFactory factory) {
        FACTORIES.add(factory);
    }

    public static List<IUpgradeSlotUpdateFactory> getFactories() {
        return FACTORIES;
    }

}
