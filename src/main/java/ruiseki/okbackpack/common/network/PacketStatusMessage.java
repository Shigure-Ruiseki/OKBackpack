package ruiseki.okbackpack.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketStatusMessage extends PacketCodec {

    @CodecField
    public String translationKey;
    @CodecField
    public int count;

    public PacketStatusMessage() {}

    public PacketStatusMessage(String translationKey) {
        this.translationKey = translationKey;
        this.count = -1;
    }

    public PacketStatusMessage(String translationKey, int count) {
        this.translationKey = translationKey;
        this.count = count;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {
        ChatComponentTranslation component = count >= 0
            ? new ChatComponentTranslation(translationKey, "\u00a7f" + count)
            : new ChatComponentTranslation(translationKey);
        Minecraft.getMinecraft().ingameGUI.func_110326_a("\u00a7f" + component.getFormattedText(), true);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {}
}
