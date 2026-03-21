package ruiseki.okbackpack.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.RenderPlayerEvent;

import org.lwjgl.opengl.GL11;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ruiseki.okbackpack.client.renderer.RenderHelpers;
import ruiseki.okbackpack.client.renderer.player.IPlayerItemRender;
import ruiseki.okbackpack.client.renderer.player.PlayerRenderContext;
import ruiseki.okbackpack.compat.Mods;

public class ItemRenderEvent {

    public ItemRenderEvent() {}

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Specials.Post event) {

        if (event.entityLiving.getActivePotionEffect(Potion.invisibility) != null) {
            return;
        }

        EntityPlayer player = event.entityPlayer;
        InventoryPlayer inv = player.inventory;

        renderArmor(inv, event, RenderHelpers.RenderType.BODY);

        if (Mods.BaublesExpanded.isLoaded() || Mods.Baubles.isLoaded()) {
            InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);
            renderBauble(baubles, event, RenderHelpers.RenderType.BODY);
        }
    }

    @SubscribeEvent
    public void onRenderPre(RenderPlayerEvent.Specials.Pre event) {

        EntityPlayer player = event.entityPlayer;

        PlayerRenderContext context = collectRenderContext(player);

        event.renderCape = context.renderCape();
        event.renderHelmet = context.renderHelmet();
        event.renderItem = context.renderItem();
    }

    private PlayerRenderContext collectRenderContext(EntityPlayer player) {

        PlayerRenderContext context = new PlayerRenderContext();
        InventoryPlayer inv = player.inventory;

        // armor
        for (int armorIndex = 0; armorIndex < 4; armorIndex++) {

            int slot = player.inventory.getSizeInventory() - 1 - armorIndex;

            ItemStack stack = inv.getStackInSlot(slot);

            if (stack == null) continue;

            if (stack.getItem() instanceof IPlayerItemRender renderer) {
                renderer.collectContext(stack, player, context);
            }
        }

        // baubles
        if (Mods.BaublesExpanded.isLoaded() || Mods.Baubles.isLoaded()) {

            InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);

            for (int i = 0; i < baubles.getSizeInventory(); i++) {

                ItemStack stack = baubles.getStackInSlot(i);

                if (stack == null) continue;

                if (stack.getItem() instanceof IPlayerItemRender renderer) {
                    renderer.collectContext(stack, player, context);
                }
            }
        }

        return context;
    }

    private void renderBauble(InventoryBaubles inv, RenderPlayerEvent event, RenderHelpers.RenderType type) {

        EntityPlayer player = event.entityPlayer;

        for (int i = 0; i < inv.getSizeInventory(); i++) {

            ItemStack stack = inv.getStackInSlot(i);

            if (stack == null) continue;

            if (stack.getItem() instanceof IPlayerItemRender renderer) {

                GL11.glPushMatrix();
                GL11.glColor4f(1F, 1F, 1F, 1F);

                renderer.render(stack, player, event, type);

                GL11.glPopMatrix();
            }
        }
    }

    private void renderArmor(InventoryPlayer inv, RenderPlayerEvent event, RenderHelpers.RenderType type) {

        EntityPlayer player = event.entityPlayer;

        for (int armorIndex = 0; armorIndex < 4; armorIndex++) {

            int slot = player.inventory.getSizeInventory() - 1 - armorIndex;

            ItemStack stack = inv.getStackInSlot(slot);

            if (stack == null) continue;

            if (stack.getItem() instanceof IPlayerItemRender renderer) {

                GL11.glPushMatrix();
                GL11.glColor4f(1F, 1F, 1F, 1F);

                renderer.render(stack, player, event, type);

                GL11.glPopMatrix();
            }
        }
    }
}
