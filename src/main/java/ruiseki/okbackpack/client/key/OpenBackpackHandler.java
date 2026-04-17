package ruiseki.okbackpack.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.utils.Platform;

import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
import ruiseki.okcore.client.key.IKeyHandler;

public class OpenBackpackHandler implements IKeyHandler {

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        EntityPlayer player = Platform.getClientPlayer();

        InventoryTypes.BAUBLES.visitAll(player, (type, index, stack) -> {
            if (BackpackEntityHelper.isBackpackStack(stack, false)) {
                GuiFactories.playerInventory()
                    .openFromBaublesClient(index);
                return true;
            }
            return false;
        });

        for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
            int slot = player.inventory.getSizeInventory() - 1 - armorIndex;
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (BackpackEntityHelper.isBackpackStack(stack, false)) {

                GuiFactories.playerInventory()
                    .openFromPlayerInventoryClient(slot);
                return;
            }
        }
    }
}
