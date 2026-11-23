package ruiseki.okbackpack.client.key;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;

import ruiseki.okcore.client.key.IKeyHandler;

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

        // BackpackEntityHelpers
        // .visitPlayerBackpacks(player, BackpackEntityHelpers.SearchOrder.BAUBLES_THEN_PLAYER, context -> {
        // if (upgradeSlot >= context.wrapper().upgradeSlots) return false;
        //
        // IUpgradeWrapper upgradeWrapper = context.wrapper()
        // .getUpgradeHandler()
        // .getWrapperInSlot(upgradeSlot);
        // if (upgradeWrapper instanceof IToggleable toggleable) {
        // toggleable.toggle();
        // BackpackEntityHelpers.persistBackpack(context);
        //
        // String upgradeName = EnumChatFormatting.getTextWithoutFormattingCodes(
        // upgradeWrapper.getUpgradeStack()
        // .getDisplayName());
        // String langKey = toggleable.isEnabled() ? "gui.okbackpack.status.upgrade_switched_on"
        // : "gui.okbackpack.status.upgrade_switched_off";
        // String message = LangHelpers.localize(langKey, upgradeName);
        // Minecraft.getMinecraft().ingameGUI.func_110326_a("\u00a7f" + message, true);
        //
        // OKBackpack._instance.getPacketHandler()
        // .sendToServer(
        // new PacketToggleUpgrade(context.slotIndex(), upgradeSlot, context.inventoryType()));
        // return true;
        // }
        // return false;
        // });
    }
}
