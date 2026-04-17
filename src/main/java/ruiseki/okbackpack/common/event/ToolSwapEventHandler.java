package ruiseki.okbackpack.common.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade.ToolSwapMode;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade.WeaponSwapMode;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;

public class ToolSwapEventHandler {

    private static final int COOLDOWN_TICKS = 10;

    private final Map<Integer, PlayerSwapState> playerStates = new HashMap<>();

    public ToolSwapEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        if (event.entityPlayer.worldObj.isRemote) return;

        EntityPlayer player = event.entityPlayer;
        PlayerSwapState state = getState(player);
        long now = player.worldObj.getTotalWorldTime();
        if (now - state.lastToolSwapTick < COOLDOWN_TICKS) return;

        Block block = event.world.getBlock(event.x, event.y, event.z);
        int meta = event.world.getBlockMetadata(event.x, event.y, event.z);

        ItemStack currentHand = player.inventory.getStackInSlot(player.inventory.currentItem);
        int handItemId = getItemIdentity(currentHand);
        if (Block.getIdFromBlock(block) == state.lastBlockId && meta == state.lastBlockMeta
            && handItemId == state.lastToolHandId) {
            return;
        }

        state.lastToolSwapTick = now;
        state.lastBlockId = Block.getIdFromBlock(block);
        state.lastBlockMeta = meta;
        state.lastToolHandId = handItemId;

        trySwapTool(player, block, meta);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (event.entityPlayer.worldObj.isRemote) return;

        EntityPlayer player = event.entityPlayer;
        PlayerSwapState state = getState(player);
        long now = player.worldObj.getTotalWorldTime();
        if (now - state.lastWeaponSwapTick < COOLDOWN_TICKS) return;

        ItemStack currentHand = player.inventory.getStackInSlot(player.inventory.currentItem);
        int handItemId = getItemIdentity(currentHand);
        if (handItemId == state.lastWeaponHandId) {
            return;
        }

        state.lastWeaponSwapTick = now;
        state.lastWeaponHandId = handItemId;

        trySwapWeapon(player);
    }

    private void trySwapTool(EntityPlayer player, Block block, int meta) {
        int currentSlot = player.inventory.currentItem;
        ItemStack currentHand = player.inventory.getStackInSlot(currentSlot);
        PlayerSwapState state = getState(player);

        UpgradeContext ctx = findActiveUpgrade(player);
        if (ctx == null) {
            state.resetToolState();
            return;
        }

        ToolSwapMode mode = ctx.upgrade.getToolSwapMode();
        if (mode == ToolSwapMode.NO_SWAP_TOOL) {
            state.resetToolState();
            return;
        }

        float currentScore = currentHand != null ? rawToolScore(currentHand, block, meta, mode) : -1;

        Candidate best = null;
        best = searchBackpackForTool(ctx, block, meta, best);
        best = searchPlayerInventoryForTool(player, block, meta, ctx, best, currentSlot);
        if (best == null || best.score <= Math.max(currentScore, 0)) return;

        performSwap(player, ctx, best, currentSlot);
        state.lastToolHandId = getItemIdentity(best.stack);
    }

    private void trySwapWeapon(EntityPlayer player) {
        int currentSlot = player.inventory.currentItem;
        ItemStack currentHand = player.inventory.getStackInSlot(currentSlot);
        PlayerSwapState state = getState(player);

        UpgradeContext ctx = findActiveUpgrade(player);
        if (ctx == null) {
            state.resetWeaponState();
            return;
        }

        WeaponSwapMode mode = ctx.upgrade.getWeaponSwapMode();
        if (mode == WeaponSwapMode.NO_SWAP_WEAPON) {
            state.resetWeaponState();
            return;
        }

        float currentScore = currentHand != null ? rawWeaponScore(currentHand) : -1;

        Candidate best = null;
        best = searchBackpackForWeapon(ctx, best);
        best = searchPlayerInventoryForWeapon(player, ctx, best, currentSlot);
        if (best == null || best.score <= Math.max(currentScore, 0)) return;

        performSwap(player, ctx, best, currentSlot);
        state.lastWeaponHandId = getItemIdentity(best.stack);
    }

    private void performSwap(EntityPlayer player, UpgradeContext ctx, Candidate best, int currentSlot) {
        ItemStack currentHand = player.inventory.getStackInSlot(currentSlot);

        switch (best.location) {
            case HOTBAR:
                player.inventory.currentItem = best.playerSlot;
                if (player instanceof EntityPlayerMP mp) {
                    mp.playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(best.playerSlot));
                }
                break;
            case PLAYER_INVENTORY:
                player.inventory.setInventorySlotContents(currentSlot, best.stack);
                player.inventory.setInventorySlotContents(best.playerSlot, currentHand);
                break;
            case BACKPACK:
                ItemStack extracted = ctx.backpack.getWrapper()
                    .extractItem(best.backpackInternalSlot, best.stack.stackSize, false);
                if (extracted == null) break;
                if (currentHand != null) {
                    ctx.backpack.getWrapper()
                        .insertItem(currentHand, false);
                }
                BackpackEntityHelper.persistBackpack(ctx.backpack);
                player.inventory.setInventorySlotContents(currentSlot, extracted);
                break;
        }
    }

    private UpgradeContext findActiveUpgrade(EntityPlayer player) {
        final UpgradeContext[] result = new UpgradeContext[1];
        BackpackEntityHelper
            .visitPlayerBackpacks(player, BackpackEntityHelper.SearchOrder.BAUBLES_THEN_PLAYER, context -> {
                Map<Integer, IToolSwapperUpgrade> upgrades = context.getWrapper()
                    .gatherCapabilityUpgrades(IToolSwapperUpgrade.class);
                for (IToolSwapperUpgrade upgrade : upgrades.values()) {
                    if (upgrade instanceof IToggleable toggleable && !toggleable.isEnabled()) {
                        continue;
                    }

                    result[0] = new UpgradeContext(context, upgrade);
                    return true;
                }
                return false;
            });
        return result[0];
    }

    private float rawToolScore(ItemStack stack, Block block, int meta, ToolSwapMode mode) {
        if (mode == ToolSwapMode.ONLY_TOOL_SWAP_TOOL && stack.getItem()
            .getToolClasses(stack)
            .isEmpty()) {
            return -1;
        }

        float speed = stack.getItem()
            .getDigSpeed(stack, block, meta);
        return speed > 1.0f ? speed : -1;
    }

    private float rawWeaponScore(ItemStack stack) {
        float damage = getAttackDamage(stack);
        return damage > 0 ? damage : -1;
    }

    private float scoreToolForBlock(ItemStack stack, Block block, int meta, UpgradeContext ctx) {
        ToolSwapMode mode = ctx.upgrade.getToolSwapMode();
        if (mode == ToolSwapMode.ONLY_TOOL_SWAP_TOOL && stack.getItem()
            .getToolClasses(stack)
            .isEmpty()) {
            return -1;
        }
        if (!passesFilter(stack, ctx.upgrade)) return -1;

        float speed = stack.getItem()
            .getDigSpeed(stack, block, meta);
        return speed > 1.0f ? speed : -1;
    }

    private float scoreWeapon(ItemStack stack, UpgradeContext ctx) {
        if (!passesFilter(stack, ctx.upgrade)) return -1;

        float damage = getAttackDamage(stack);
        return damage > 0 ? damage : -1;
    }

    private boolean passesFilter(ItemStack stack, IToolSwapperUpgrade upgrade) {
        if (upgrade instanceof IBasicFilterable filterable) {
            return filterable.checkFilter(stack);
        }
        return true;
    }

    private Candidate searchBackpackForTool(UpgradeContext ctx, Block block, int meta, Candidate currentBest) {
        for (int slot = 0; slot < ctx.backpack.getWrapper()
            .getSlots(); slot++) {
            ItemStack stack = ctx.backpack.getWrapper()
                .getStackInSlot(slot);
            if (stack == null) continue;

            float score = scoreToolForBlock(stack, block, meta, ctx);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                currentBest = new Candidate(stack, score, Location.BACKPACK, -1, slot);
            }
        }
        return currentBest;
    }

    private Candidate searchPlayerInventoryForTool(EntityPlayer player, Block block, int meta, UpgradeContext ctx,
        Candidate currentBest, int skipSlot) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot == skipSlot) continue;

            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack == null) continue;

            float score = scoreToolForBlock(stack, block, meta, ctx);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                Location location = slot < 9 ? Location.HOTBAR : Location.PLAYER_INVENTORY;
                currentBest = new Candidate(stack, score, location, slot, -1);
            }
        }
        return currentBest;
    }

    private Candidate searchBackpackForWeapon(UpgradeContext ctx, Candidate currentBest) {
        for (int slot = 0; slot < ctx.backpack.getWrapper()
            .getSlots(); slot++) {
            ItemStack stack = ctx.backpack.getWrapper()
                .getStackInSlot(slot);
            if (stack == null) continue;

            float score = scoreWeapon(stack, ctx);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                currentBest = new Candidate(stack, score, Location.BACKPACK, -1, slot);
            }
        }
        return currentBest;
    }

    private Candidate searchPlayerInventoryForWeapon(EntityPlayer player, UpgradeContext ctx, Candidate currentBest,
        int skipSlot) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot == skipSlot) continue;

            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack == null) continue;

            float score = scoreWeapon(stack, ctx);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                Location location = slot < 9 ? Location.HOTBAR : Location.PLAYER_INVENTORY;
                currentBest = new Candidate(stack, score, location, slot, -1);
            }
        }
        return currentBest;
    }

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

    private static int getItemIdentity(ItemStack stack) {
        return stack == null ? -1 : Item.getIdFromItem(stack.getItem());
    }

    private PlayerSwapState getState(EntityPlayer player) {
        return playerStates.computeIfAbsent(player.getEntityId(), key -> new PlayerSwapState());
    }

    public static class PlayerSwapState {

        long lastToolSwapTick;
        long lastWeaponSwapTick;
        int lastBlockId = -1;
        int lastBlockMeta = -1;
        int lastToolHandId = -1;
        int lastWeaponHandId = -1;

        public void resetToolState() {
            lastToolSwapTick = 0;
            lastBlockId = -1;
            lastBlockMeta = -1;
            lastToolHandId = -1;
        }

        public void resetWeaponState() {
            lastWeaponSwapTick = 0;
            lastWeaponHandId = -1;
        }
    }

    public enum Location {
        HOTBAR,
        PLAYER_INVENTORY,
        BACKPACK
    }

    @Desugar
    public record UpgradeContext(BackpackEntityHelper.BackpackContext backpack, IToolSwapperUpgrade upgrade) {}

    @Desugar
    public record Candidate(ItemStack stack, float score, Location location, int playerSlot,
        int backpackInternalSlot) {}
}
