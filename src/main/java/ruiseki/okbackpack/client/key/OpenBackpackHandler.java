package ruiseki.okbackpack.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.utils.Platform;

import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.client.key.IKeyHandler;

public class OpenBackpackHandler implements IKeyHandler {

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        EntityPlayer player = Platform.getClientPlayer();

        if (Mods.Baubles.isLoaded()) {
            InventoryTypes.BAUBLES.visitAll(player, (type, index, stack) -> {
                if (stack != null && stack.getItem() instanceof BlockBackpack.ItemBackpack) {
                    GuiFactories.playerInventory()
                        .openFromBaublesClient(index);
                    return true;
                }
                return false;
            });
        }

        for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
            int slot = player.inventory.getSizeInventory() - 1 - armorIndex;
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() instanceof BlockBackpack.ItemBackpack) {

                GuiFactories.playerInventory()
                    .openFromPlayerInventoryClient(slot);
                return;
            }
        }
    }
}
