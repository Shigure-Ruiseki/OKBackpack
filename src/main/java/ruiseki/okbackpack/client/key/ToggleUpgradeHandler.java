package ruiseki.okbackpack.client.key;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.network.PacketToggleUpgrade;
import ruiseki.okcore.client.key.IKeyHandler;
import ruiseki.okcore.helper.LangHelpers;

public class ToggleUpgradeHandler implements IKeyHandler {

    private final int upgradeSlot;

    public ToggleUpgradeHandler(int upgradeSlot) {
        this.upgradeSlot = upgradeSlot;
    }

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) return;

        // Search baubles first
        if (tryToggle(BaublesApi.getBaubles(player), InventoryTypes.BAUBLES)) return;

        // Then search player inventory
        tryToggle(player.inventory, InventoryTypes.PLAYER);
    }

    private boolean tryToggle(IInventory inventory, InventoryType type) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack backpackItem)) continue;

            BackpackWrapper wrapper = new BackpackWrapper(stack, backpackItem);
            wrapper.readFromItem();

            if (upgradeSlot >= wrapper.upgradeSlots) continue;

            IUpgradeWrapper upgradeWrapper = wrapper.getUpgradeHandler()
                .getWrapperInSlot(upgradeSlot);
            if (upgradeWrapper instanceof IToggleable toggleable) {
                toggleable.toggle();
                wrapper.writeToItem();

                String upgradeName = EnumChatFormatting.getTextWithoutFormattingCodes(
                    upgradeWrapper.getUpgradeStack()
                        .getDisplayName());
                String langKey = toggleable.isEnabled() ? "gui.okbackpack.status.upgrade_switched_on"
                    : "gui.okbackpack.status.upgrade_switched_off";
                String message = LangHelpers.localize(langKey, upgradeName);
                Minecraft.getMinecraft().ingameGUI.func_110326_a("\u00a7f" + message, true);

                OKBackpack.instance.getPacketHandler()
                    .sendToServer(new PacketToggleUpgrade(i, upgradeSlot, type));
                return true;
            }
        }
        return false;
    }
}
