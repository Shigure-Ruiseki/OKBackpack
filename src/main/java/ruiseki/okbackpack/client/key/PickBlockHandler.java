package ruiseki.okbackpack.client.key;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import com.github.bsideup.jabel.Desugar;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.wrapper.IRefillUpgrade;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
import ruiseki.okbackpack.common.helpers.BackpackHandSwapHelper;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.common.network.PacketQuickDraw;
import ruiseki.okcore.client.key.IKeyHandler;
import ruiseki.okcore.helper.ItemStackHelpers;

public class PickBlockHandler implements IKeyHandler {

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null || player.capabilities.isCreativeMode) return;

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        Block block = mc.theWorld.getBlock(mop.blockX, mop.blockY, mop.blockZ);
        int meta = mc.theWorld.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
        Item item = Item.getItemFromBlock(block);
        if (item == null) return;

        ItemStack wanted = new ItemStack(item, 1, meta);
        if (playerHasItem(player, wanted)) return;

        ItemStack held = player.inventory.getStackInSlot(player.inventory.currentItem);
        if (held == null) {
            QuickDrawResult quickDraw = findQuickDrawResult(player, wanted);
            if (quickDraw != null && mc.theWorld.isRemote) {
                int currentSlot = player.inventory.currentItem;
                OKBackpack.instance.getPacketHandler()
                    .sendToServer(
                        new PacketBackpackNBT(
                            quickDraw.backpack.getSlotIndex(),
                            quickDraw.backpack.getWrapper()
                                .getBackpackNBT(),
                            quickDraw.backpack.getInventoryType()));
                OKBackpack.instance.getPacketHandler()
                    .sendToServer(new PacketQuickDraw(currentSlot, quickDraw.stack));
                return;
            }
        }

        if (!mc.theWorld.isRemote) return;

        RefillPickContext ctx = findRefillUpgrade(player);
        if (ctx == null) return;
        if (!BackpackHandSwapHelper
            .canReplaceHandWithBackpackItem(ctx.backpack.getWrapper(), wanted, wanted.getMaxStackSize(), held)) {
            return;
        }

        ItemStack extracted = ctx.backpack.getWrapper()
            .extractItem(wanted, wanted.getMaxStackSize(), false);
        if (extracted == null) return;

        if (held != null) {
            ctx.backpack.getWrapper()
                .insertItem(held, false);
        }

        int currentSlot = player.inventory.currentItem;
        OKBackpack.instance.getPacketHandler()
            .sendToServer(
                new PacketBackpackNBT(
                    ctx.backpack.getSlotIndex(),
                    ctx.backpack.getWrapper()
                        .getBackpackNBT(),
                    ctx.backpack.getInventoryType()));
        OKBackpack.instance.getPacketHandler()
            .sendToServer(new PacketQuickDraw(currentSlot, extracted));
    }

    private boolean playerHasItem(EntityClientPlayerMP player, ItemStack wanted) {
        IInventory baublesInventory = BaublesApi.getBaubles(player);
        if (baublesInventory != null) {
            for (int i = 0; i < baublesInventory.getSizeInventory(); i++) {
                ItemStack stack = baublesInventory.getStackInSlot(i);
                if (stack != null && ItemStackHelpers.areStacksEqual(stack, wanted)) {
                    return true;
                }
            }
        }

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && ItemStackHelpers.areStacksEqual(stack, wanted)) {
                return true;
            }
        }

        for (ItemStack stack : player.inventory.armorInventory) {
            if (stack != null && ItemStackHelpers.areStacksEqual(stack, wanted)) {
                return true;
            }
        }

        return false;
    }

    private QuickDrawResult findQuickDrawResult(EntityClientPlayerMP player, ItemStack wanted) {
        final QuickDrawResult[] result = new QuickDrawResult[1];
        BackpackEntityHelper
            .visitPlayerBackpacks(player, BackpackEntityHelper.SearchOrder.BAUBLES_THEN_PLAYER, context -> {
                ItemStack extracted = context.getWrapper()
                    .extractItem(wanted, wanted.getMaxStackSize(), false);
                if (extracted == null || extracted.stackSize <= 0) {
                    return false;
                }

                result[0] = new QuickDrawResult(context, extracted);
                return true;
            });
        return result[0];
    }

    private RefillPickContext findRefillUpgrade(EntityClientPlayerMP player) {
        final RefillPickContext[] result = new RefillPickContext[1];
        BackpackEntityHelper
            .visitPlayerBackpacks(player, BackpackEntityHelper.SearchOrder.BAUBLES_THEN_PLAYER, context -> {
                Map<Integer, IRefillUpgrade> upgrades = context.getWrapper()
                    .gatherCapabilityUpgrades(IRefillUpgrade.class);
                for (IRefillUpgrade upgrade : upgrades.values()) {
                    if (upgrade.supportsBlockPick() && upgrade.isEnabled()) {
                        result[0] = new RefillPickContext(context);
                        return true;
                    }
                }
                return false;
            });
        return result[0];
    }

    @Desugar
    public record QuickDrawResult(BackpackEntityHelper.BackpackContext backpack, ItemStack stack) {}

    @Desugar
    public record RefillPickContext(BackpackEntityHelper.BackpackContext backpack) {}
}
