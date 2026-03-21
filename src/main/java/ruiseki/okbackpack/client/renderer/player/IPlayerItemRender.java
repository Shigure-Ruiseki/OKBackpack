package ruiseki.okbackpack.client.renderer.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.client.renderer.RenderHelpers.RenderType;

public interface IPlayerItemRender {

    @SideOnly(Side.CLIENT)
    default void collectContext(ItemStack stack, EntityPlayer player, PlayerRenderContext context) {}

    @SideOnly(Side.CLIENT)
    void render(ItemStack stack, EntityPlayer player, RenderPlayerEvent event, RenderType type);
}
