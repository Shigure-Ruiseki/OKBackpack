package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import cpw.mods.fml.relauncher.Side;

public class FilterSlotSH extends PhantomItemSlotSH {

    public FilterSlotSH(ModularSlot slot) {
        super(slot);
    }

    @Override
    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        if (mouseData.shift) {
            getSlot().putStack(null);
            return;
        }

        if (cursorStack != null) {
            if (!isItemValid(cursorStack)) return;

            ItemStack copy = cursorStack.copy();
            copy.stackSize = 1;
            getSlot().putStack(copy);
            return;
        }

        getSlot().putStack(null);
    }

    @Override
    protected void phantomScroll(MouseData mouseData) {

    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        super.readOnServer(id, buf);
        if (id == SYNC_CLICK) {
            phantomClick(MouseData.readPacket(buf));
        } else if (id == SYNC_ITEM_SIMPLE) {
            if (!isPhantom()) return;
            ItemStack itemStack = NetworkUtils.readItemStack(buf);
            int button = buf.readVarIntFromBuffer(); // TODO whats this 1.12
            phantomClick(new MouseData(Side.SERVER, button, false, false, false), itemStack);
        }
    }
}
