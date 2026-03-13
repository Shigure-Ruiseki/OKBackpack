package ruiseki.okbackpack.client.renderer;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A Bauble Item that implements this will be have hooks to render something on
 * the player while its equipped.
 * This class doesn't extend IBauble to make the API not depend on the Baubles
 * API, but the item in question still needs to implement IBauble.
 */
public interface IBaubleRender {

    /**
     * Called for the rendering of the bauble on the player. The player instance can be
     * acquired through the event parameter. Transformations are already applied for
     * the RenderType passed in. Make sure to check against the type parameter for
     * rendering. Will not be called if the item is a ICosmeticAttachable and
     * has a cosmetic bauble attached to it.
     */
    @SideOnly(Side.CLIENT)
    public void onPlayerBaubleRender(ItemStack stack, RenderPlayerEvent event, RenderHelpers.RenderType type);
}
