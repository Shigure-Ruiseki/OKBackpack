package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.upgrade.BackpackSHRegistry;
import ruiseki.okbackpack.common.block.BackpackWrapper;

public class BackpackSH extends SyncHandler {

    public final PlayerMainInvWrapper playerInv;
    public final BackpackWrapper wrapper;
    public final IStoragePanel<?> panel;

    public BackpackSH(PlayerMainInvWrapper playerInv, BackpackWrapper wrapper, IStoragePanel<?> panel) {
        this.playerInv = playerInv;
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (!BackpackSHRegistry.isServerEmpty()) {
            try {
                BackpackSHRegistry.handleServer(this, id, buf);
                wrapper.markDirty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (!BackpackSHRegistry.isClientEmpty()) {
            try {
                BackpackSHRegistry.handleClient(this, id, buf);
                wrapper.markDirty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int getId(String name) {
        return BackpackSHRegistry.getId(name);
    }
}
