package ruiseki.okbackpack.api.widget;

import java.util.ArrayList;
import java.util.List;

public class UpgradeSlotGroupRegistry {

    private static final List<IUpgradeSlotGroupFactory> FACTORIES = new ArrayList<>();

    public static void register(IUpgradeSlotGroupFactory factory) {
        FACTORIES.add(factory);
    }

    public static List<IUpgradeSlotGroupFactory> getFactories() {
        return FACTORIES;
    }

}
