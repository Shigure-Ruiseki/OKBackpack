package ruiseki.okbackpack.compat.tic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.github.bsideup.jabel.Desugar;

import ruiseki.okbackpack.client.gui.interaction.BackpackGuiOpenHelpers;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.common.init.OKBackpackBlocks;
import ruiseki.okcore.helper.BaublesHelpers;
import tconstruct.client.tabs.AbstractTab;

public class OKBackpackInventoryTab extends AbstractTab {

    private static final ResourceLocation TAB_TEXTURE = new ResourceLocation(
        "textures/gui/container/creative_inventory/tabs.png");

    private final RenderItem tabItemRenderer = new RenderItem();

    private EntityClientPlayerMP cachedPlayer;
    private int cachedTick = -1;
    private TargetBackpack cachedTarget;

    public OKBackpackInventoryTab() {
        super(0, 0, 0, new ItemStack(OKBackpackBlocks.BACKPACK_BASE.get()));
    }

    @Override
    public void onTabClicked() {
        TargetBackpack target = getTargetBackpack(true);
        if (target != null) {
            BackpackGuiOpenHelpers.openClient(target.inventoryType(), target.slotIndex());
        }
    }

    @Override
    public boolean shouldAddToList() {
        return getTargetBackpack() != null;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        TargetBackpack target = getTargetBackpack();
        if (target != null) {
            drawButton(minecraft, target.stack(), true);
        }
    }

    @Override
    protected void drawButton(Minecraft minecraft, ItemStack tabIcon, boolean enableItemIconDepthTest) {
        if (!visible || tabIcon == null) {
            return;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int textureY = enabled ? 2 : 32;
        int tabHeight = enabled ? 25 : 32;
        int textureX = id == 2 ? 0 : 1;
        int tabY = yPosition + (enabled ? 3 : 0);

        minecraft.getTextureManager()
            .bindTexture(TAB_TEXTURE);
        drawTexturedModalRect(xPosition, tabY, textureX * 28, textureY, 28, tabHeight);

        RenderHelper.enableGUIStandardItemLighting();
        zLevel = 100.0F;
        tabItemRenderer.zLevel = 100.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        if (enableItemIconDepthTest) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        tabItemRenderer.renderItemAndEffectIntoGUI(
            minecraft.fontRenderer,
            minecraft.getTextureManager(),
            tabIcon,
            xPosition + 6,
            yPosition + 8);
        tabItemRenderer.renderItemOverlayIntoGUI(
            minecraft.fontRenderer,
            minecraft.getTextureManager(),
            tabIcon,
            xPosition + 6,
            yPosition + 8);
        if (enableItemIconDepthTest) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        tabItemRenderer.zLevel = 0.0F;
        zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
    }

    private TargetBackpack getTargetBackpack() {
        return getTargetBackpack(false);
    }

    private TargetBackpack getTargetBackpack(boolean refresh) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player == null || player.capabilities.isCreativeMode) {
            clearTargetCache();
            return null;
        }

        if (refresh || cachedPlayer != player || cachedTick != player.ticksExisted) {
            cachedPlayer = player;
            cachedTick = player.ticksExisted;
            cachedTarget = findTargetBackpack(player);
        }
        return cachedTarget;
    }

    private TargetBackpack findTargetBackpack(EntityClientPlayerMP player) {
        TargetBackpack baublesTarget = findTargetBackpack(BaublesHelpers.getBaubles(player), InventoryTypes.BAUBLES);
        if (baublesTarget != null) {
            return baublesTarget;
        }
        return findTargetBackpack(player.inventory, InventoryTypes.PLAYER);
    }

    private TargetBackpack findTargetBackpack(IInventory inventory, InventoryType inventoryType) {
        if (inventory == null) {
            return null;
        }

        for (int slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++) {
            ItemStack stack = inventory.getStackInSlot(slotIndex);
            if (BackpackEntityHelpers.isBackpackStack(stack, false)) {
                return new TargetBackpack(stack, inventoryType, slotIndex);
            }
        }
        return null;
    }

    private void clearTargetCache() {
        cachedPlayer = null;
        cachedTick = -1;
        cachedTarget = null;
    }

    @Desugar
    private record TargetBackpack(ItemStack stack, InventoryType inventoryType, int slotIndex) {}
}
