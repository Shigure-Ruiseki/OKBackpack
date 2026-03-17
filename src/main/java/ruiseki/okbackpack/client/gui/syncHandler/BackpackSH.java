package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import ruiseki.okbackpack.common.SortType;
import ruiseki.okbackpack.common.block.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.block.BackpackWrapper;

public class BackpackSH extends SyncHandler {

    public static final int UPDATE_SET_SORT_TYPE = 0;
    public static final int UPDATE_SORT_INV = 1;
    public static final int UPDATE_TRANSFER_TO_BACKPACK_INV = 2;
    public static final int UPDATE_TRANSFER_TO_PLAYER_INV = 3;
    public static final int UPDATE_SETTING = 4;

    private final PlayerMainInvWrapper playerInv;
    private final BackpackWrapper wrapper;
    private final BackpackPanel panel;

    public BackpackSH(PlayerMainInvWrapper playerInv, BackpackWrapper wrapper, BackpackPanel panel) {
        this.playerInv = playerInv;
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {

    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        switch (id) {
            case UPDATE_SET_SORT_TYPE:
                setSortType(buf);
                break;

            case UPDATE_SORT_INV:
                sortInventory(buf);
                break;

            case UPDATE_TRANSFER_TO_BACKPACK_INV:
                transferToBackpack(buf);
                break;

            case UPDATE_TRANSFER_TO_PLAYER_INV:
                transferToPlayerInventory(buf);
                break;

            case UPDATE_SETTING:
                updateBackpack(buf);
                break;

            default:
                break;
        }
        wrapper.syncToServer();
    }

    public void setSortType(PacketBuffer buf) {
        SortType sortType = SortType.values()[buf.readInt()];
        setSortType(sortType);
    }

    public void setSortType(SortType sortType) {
        wrapper.setSortType(sortType);
    }

    public void sortInventory(PacketBuffer buf) throws IOException {
        int size = wrapper.getBackpackSlots();

        for (int i = 0; i < size; i++) {
            wrapper.getBackpackHandler()
                .setStackInSlot(i, buf.readItemStackFromBuffer());
        }
    }

    public void transferToBackpack(boolean transferMatched) {
        BackpackInventoryHelpers.transferPlayerInventoryToBackpack(wrapper, playerInv, transferMatched);
    }

    public void transferToBackpack(PacketBuffer buf) {
        boolean transferMatched = buf.readBoolean();
        BackpackInventoryHelpers.transferPlayerInventoryToBackpack(wrapper, playerInv, transferMatched);
    }

    public void transferToPlayerInventory(boolean transferMatched) {
        BackpackInventoryHelpers.transferBackpackToPlayerInventory(wrapper, playerInv, transferMatched);
    }

    public void transferToPlayerInventory(PacketBuffer buf) {
        boolean transferMatched = buf.readBoolean();
        BackpackInventoryHelpers.transferBackpackToPlayerInventory(wrapper, playerInv, transferMatched);
    }

    public void updateBackpack(PacketBuffer buf) throws IOException {
        boolean lock = buf.readBoolean();
        String uuid = buf.readStringFromBuffer(36);
        boolean tab = buf.readBoolean();
        wrapper.setLockBackpack(lock);
        wrapper.setUuid(uuid);
        wrapper.setKeepTab(tab);
    }
}
