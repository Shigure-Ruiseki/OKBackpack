package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.api.IBackpackWrapper;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.upgrade.BackpackSlotSHRegistry;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;

public class BackpackSlotSH extends ItemSlotSH {

    public final IBackpackWrapper wrapper;
    public final IStoragePanel<?> panel;

    public BackpackSlotSH(ModularSlot slot, IBackpackWrapper wrapper, IStoragePanel<?> panel) {
        super(slot);
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (!BackpackSlotSHRegistry.isServerEmpty()) {
            try {
                BackpackSlotSHRegistry.handleServer(this, id, buf);
                wrapper.markDirty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        super.readOnServer(id, buf);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (!BackpackSlotSHRegistry.isClientEmpty()) {
            try {
                BackpackSlotSHRegistry.handleClient(this, id, buf);
                wrapper.markDirty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        super.readOnClient(id, buf);
    }

    public IUpgradeWrapper getWrapper() {
        return this.wrapper.getUpgradeHandler()
            .getWrapperInSlot(getSlot().getSlotIndex());
    }

    public static int getId(String name) {
        return BackpackSlotSHRegistry.getId(name);
    }
}
