package ruiseki.okbackpack.client.gui.widget.updateGroup;

import java.util.HashMap;
import java.util.Map;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.api.widget.UpgradeSlotGroupRegistry;
import ruiseki.okbackpack.common.block.BackpackWrapper;

public class UpgradeSlotUpdateGroup {

    public final IStoragePanel<?> panel;
    public final IStorageWrapper wrapper;
    public final int slotIndex;
    public final PanelSyncManager syncManager;

    final Map<String, Object> components = new HashMap<>();

    public UpgradeSlotUpdateGroup(IStoragePanel<?> panel, BackpackWrapper wrapper, int slotIndex) {
        this.panel = panel;
        this.wrapper = wrapper;
        this.slotIndex = slotIndex;
        this.syncManager = panel.getSyncManager();

        for (IUpgradeSlotGroupFactory factory : UpgradeSlotGroupRegistry.getFactories()) {
            factory.build(this);
        }
    }

    public <T> void put(String key, T value) {
        components.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {

        Object obj = components.get(key);

        if (obj == null) {
            return null;
        }

        return (T) obj;
    }
}
