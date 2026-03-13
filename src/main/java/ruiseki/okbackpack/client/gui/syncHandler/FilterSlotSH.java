package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;

import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;

public class FilterSlotSH extends ItemSlotSH {

    public static final int SYNC_ITEM_SIMPLE = 102;

    public FilterSlotSH(ModularFilterSlot slot) {
        super(slot);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == SYNC_ITEM_SIMPLE) {
            ItemStack itemStack = NetworkUtils.readItemStack(buf);
            this.getSlot()
                .putStack(itemStack);
        }
        super.readOnServer(id, buf);
    }

    public void updateFromClient(ItemStack stack) {
        syncToServer(SYNC_ITEM_SIMPLE, buf -> { NetworkUtils.writeItemStack(buf, stack); });
    }
}
