package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.block.BackpackWrapper;

public class BackpackSlotSH extends ItemSlotSH {

    public static final int UPDATE_SET_MEMORY_STACK = 6;
    public static final int UPDATE_UNSET_MEMORY_STACK = 7;
    public static final int UPDATE_SET_SLOT_LOCK = 8;
    public static final int UPDATE_UNSET_SLOT_LOCK = 9;

    public final BackpackWrapper wrapper;
    public final BackpackPanel panel;

    public BackpackSlotSH(ModularSlot slot, BackpackWrapper wrapper, BackpackPanel panel) {
        super(slot);
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {

        switch (id) {
            case UPDATE_SET_MEMORY_STACK: {
                wrapper.setMemoryStack(getSlot().getSlotIndex(), buf.readBoolean());
                break;
            }

            case UPDATE_UNSET_MEMORY_STACK:
                wrapper.unsetMemoryStack(getSlot().getSlotIndex());
                break;

            case UPDATE_SET_SLOT_LOCK:
                wrapper.setSlotLocked(getSlot().getSlotIndex(), true);
                break;

            case UPDATE_UNSET_SLOT_LOCK:
                wrapper.setSlotLocked(getSlot().getSlotIndex(), false);
                break;

            default:
                super.readOnServer(id, buf);
                break;
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        super.readOnClient(id, buf);
        wrapper.syncToServer();
    }
}
