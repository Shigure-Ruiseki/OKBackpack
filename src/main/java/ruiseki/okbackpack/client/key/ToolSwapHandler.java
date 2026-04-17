package ruiseki.okbackpack.client.key;

import java.util.Collection;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import com.google.common.collect.Multimap;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade.ToolSwapMode;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade.WeaponSwapMode;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
import ruiseki.okbackpack.common.item.toolswapper.AdvancedToolSwapperUpgradeWrapper;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.common.network.PacketQuickDraw;
import ruiseki.okbackpack.common.network.PacketToolSwap;
import ruiseki.okcore.client.key.IKeyHandler;

public class ToolSwapHandler implements IKeyHandler {

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) return;

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null) return;

        boolean targetingBlock = mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
        boolean targetingEntity = mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;
        if (!targetingBlock && !targetingEntity) return;

        int currentSlot = player.inventory.currentItem;

        // Find the first backpack with an active advanced tool swapper upgrade
        UpgradeContext ctx = findActiveUpgrade(player);
        if (ctx == null) return;

        // Score the currently held item
        ItemStack currentHand = player.inventory.getStackInSlot(currentSlot);
        float currentScore = currentHand != null ? scoreItem(currentHand, player, mop, targetingBlock, ctx.upgrade)
            : -1;

        // Find the best tool across both the backpack and the player inventory
        ToolCandidate best = null;
        best = searchBackpack(ctx, player, mop, targetingBlock, best);
        best = searchPlayerInventory(player, mop, targetingBlock, ctx.upgrade, best, currentSlot);

        if (best == null || best.score <= Math.max(currentScore, 0)) return;
        if (!mc.theWorld.isRemote) return;

        switch (best.location) {
            case HOTBAR:
                // Target tool is already in hotbar — just switch selected slot
                player.inventory.currentItem = best.playerSlot;
                break;

            case PLAYER_INVENTORY:
                // Target tool is in main inventory — swap with current hand slot
                OKBackpack.instance.getPacketHandler()
                    .sendToServer(new PacketToolSwap(currentSlot, best.playerSlot));
                break;

            case BACKPACK:
                // Target tool is in backpack — extract tool, store hand item back
                ItemStack extracted = ctx.backpack.getWrapper()
                    .extractItem(best.backpackInternalSlot, best.stack.stackSize, false);
                if (extracted == null) break;
                if (currentHand != null) {
                    ctx.backpack.wrapper()
                        .insertItem(currentHand, false);
                }
                OKBackpack.instance.getPacketHandler()
                    .sendToServer(
                        new PacketBackpackNBT(
                            ctx.backpack.slotIndex(),
                            ctx.backpack.wrapper()
                                .getBackpackNBT(),
                            ctx.backpack.inventoryType()));
                OKBackpack.instance.getPacketHandler()
                    .sendToServer(new PacketQuickDraw(currentSlot, extracted));
                break;
        }
    }

    private UpgradeContext findActiveUpgrade(EntityClientPlayerMP player) {
        final UpgradeContext[] result = new UpgradeContext[1];
        BackpackEntityHelper
            .visitPlayerBackpacks(player, BackpackEntityHelper.SearchOrder.BAUBLES_THEN_PLAYER, context -> {
                Map<Integer, IToolSwapperUpgrade> upgrades = context.wrapper()
                    .gatherCapabilityUpgrades(IToolSwapperUpgrade.class);
                for (IToolSwapperUpgrade upgrade : upgrades.values()) {
                    if (upgrade instanceof AdvancedToolSwapperUpgradeWrapper adv && adv.isEnabled()) {
                        result[0] = new UpgradeContext(context, adv);
                        return true;
                    }
                }
                return false;
            });
        return result[0];
    }

    private ToolCandidate searchBackpack(UpgradeContext ctx, EntityClientPlayerMP player, MovingObjectPosition mop,
        boolean targetingBlock, ToolCandidate currentBest) {
        var wrapper = ctx.backpack.wrapper();
        for (int s = 0; s < wrapper.getSlots(); s++) {
            ItemStack stack = wrapper.getStackInSlot(s);
            if (stack == null) continue;
            if (!ctx.upgrade.checkFilter(stack)) continue;

            float score = scoreItem(stack, player, mop, targetingBlock, ctx.upgrade);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                currentBest = new ToolCandidate(stack, score, Location.BACKPACK, -1, s);
            }
        }
        return currentBest;
    }

    private ToolCandidate searchPlayerInventory(EntityClientPlayerMP player, MovingObjectPosition mop,
        boolean targetingBlock, AdvancedToolSwapperUpgradeWrapper upgrade, ToolCandidate currentBest, int skipSlot) {
        for (int s = 0; s < 36; s++) {
            if (s == skipSlot) continue;
            ItemStack stack = player.inventory.getStackInSlot(s);
            if (stack == null) continue;
            if (!upgrade.checkFilter(stack)) continue;

            float score = scoreItem(stack, player, mop, targetingBlock, upgrade);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                Location loc = s < 9 ? Location.HOTBAR : Location.PLAYER_INVENTORY;
                currentBest = new ToolCandidate(stack, score, loc, s, -1);
            }
        }
        return currentBest;
    }

    private float scoreItem(ItemStack stack, EntityClientPlayerMP player, MovingObjectPosition mop,
        boolean targetingBlock, AdvancedToolSwapperUpgradeWrapper upgrade) {
        if (targetingBlock) {
            ToolSwapMode mode = upgrade.getToolSwapMode();
            if (mode == ToolSwapMode.NO_SWAP_TOOL) return -1;
            if (mode == ToolSwapMode.ONLY_TOOL_SWAP_TOOL) {
                if (stack.getItem()
                    .getToolClasses(stack)
                    .isEmpty()) return -1;
            }
            Block block = player.worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
            int meta = player.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
            float speed = stack.getItem()
                .getDigSpeed(stack, block, meta);
            return speed > 1.0f ? speed : -1;
        } else {
            WeaponSwapMode mode = upgrade.getWeaponSwapMode();
            if (mode == WeaponSwapMode.NO_SWAP_WEAPON) return -1;
            float damage = getAttackDamage(stack);
            return damage > 0 ? damage : -1;
        }
    }

    @SuppressWarnings("unchecked")
    private static float getAttackDamage(ItemStack stack) {
        Multimap<String, AttributeModifier> attributes = stack.getAttributeModifiers();
        Collection<AttributeModifier> modifiers = attributes
            .get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
        float damage = 0f;
        for (AttributeModifier modifier : modifiers) {
            damage += (float) modifier.getAmount();
        }
        return damage;
    }

    private enum Location {
        HOTBAR,
        PLAYER_INVENTORY,
        BACKPACK
    }

    private static class UpgradeContext {

        final BackpackEntityHelper.BackpackContext backpack;
        final AdvancedToolSwapperUpgradeWrapper upgrade;

        UpgradeContext(BackpackEntityHelper.BackpackContext backpack, AdvancedToolSwapperUpgradeWrapper upgrade) {
            this.backpack = backpack;
            this.upgrade = upgrade;
        }
    }

    private static class ToolCandidate {

        final ItemStack stack;
        final float score;
        final Location location;
        final int playerSlot;
        final int backpackInternalSlot;

        ToolCandidate(ItemStack stack, float score, Location location, int playerSlot, int backpackInternalSlot) {
            this.stack = stack;
            this.score = score;
            this.location = location;
            this.playerSlot = playerSlot;
            this.backpackInternalSlot = backpackInternalSlot;
        }
    }
}
