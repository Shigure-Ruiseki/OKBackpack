package ruiseki.okbackpack.common.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.Multimap;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade.ToolSwapMode;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade.WeaponSwapMode;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;

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

        UpgradeContext ctx = findActiveUpgrade(BaublesApi.getBaubles(player), InventoryTypes.BAUBLES);
        if (ctx == null) {
            ctx = findActiveUpgrade(player.inventory, InventoryTypes.PLAYER);
        }
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

        UpgradeContext ctx = findActiveUpgrade(BaublesApi.getBaubles(player), InventoryTypes.BAUBLES);
        if (ctx == null) {
            ctx = findActiveUpgrade(player.inventory, InventoryTypes.PLAYER);
        }
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
                ItemStack extracted = ctx.wrapper.extractItem(best.backpackInternalSlot, best.stack.stackSize, false);
                if (extracted == null) break;
                if (currentHand != null) {
                    ctx.wrapper.insertItem(currentHand, false);
                }
                ctx.wrapper.writeToItem();
                player.inventory.setInventorySlotContents(currentSlot, extracted);
                break;
        }
    }

    private UpgradeContext findActiveUpgrade(IInventory inventory, InventoryType type) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack backpack)) continue;

            BackpackWrapper wrapper = new BackpackWrapper(stack, backpack);
            Map<Integer, IToolSwapperUpgrade> upgrades = wrapper.gatherCapabilityUpgrades(IToolSwapperUpgrade.class);

            for (IToolSwapperUpgrade upgrade : upgrades.values()) {
                if (upgrade instanceof IToggleable toggleable && !toggleable.isEnabled()) continue;
                return new UpgradeContext(wrapper, i, type, upgrade);
            }
        }
        return null;
    }

    /**
     * Raw tool score for current hand — no filter applied, only checks mode and dig speed.
     */
    private float rawToolScore(ItemStack stack, Block block, int meta, ToolSwapMode mode) {
        if (mode == ToolSwapMode.ONLY_TOOL_SWAP_TOOL) {
            if (stack.getItem()
                .getToolClasses(stack)
                .isEmpty()) return -1;
        }
        float speed = stack.getItem()
            .getDigSpeed(stack, block, meta);
        return speed > 1.0f ? speed : -1;
    }

    /**
     * Raw weapon score for current hand — no filter applied, only checks attack damage.
     */
    private float rawWeaponScore(ItemStack stack) {
        float damage = getAttackDamage(stack);
        return damage > 0 ? damage : -1;
    }

    /**
     * Candidate tool score — applies filter and mode checks.
     */
    private float scoreToolForBlock(ItemStack stack, Block block, int meta, UpgradeContext ctx) {
        ToolSwapMode mode = ctx.upgrade.getToolSwapMode();
        if (mode == ToolSwapMode.ONLY_TOOL_SWAP_TOOL) {
            if (stack.getItem()
                .getToolClasses(stack)
                .isEmpty()) return -1;
        }
        if (!passesFilter(stack, ctx.upgrade)) return -1;
        float speed = stack.getItem()
            .getDigSpeed(stack, block, meta);
        return speed > 1.0f ? speed : -1;
    }

    /**
     * Candidate weapon score — applies filter check.
     */
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
        BackpackWrapper wrapper = ctx.wrapper;
        for (int s = 0; s < wrapper.getSlots(); s++) {
            ItemStack stack = wrapper.getStackInSlot(s);
            if (stack == null) continue;

            float score = scoreToolForBlock(stack, block, meta, ctx);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                currentBest = new Candidate(stack, score, Location.BACKPACK, -1, s);
            }
        }
        return currentBest;
    }

    private Candidate searchPlayerInventoryForTool(EntityPlayer player, Block block, int meta, UpgradeContext ctx,
        Candidate currentBest, int skipSlot) {
        for (int s = 0; s < 36; s++) {
            if (s == skipSlot) continue;
            ItemStack stack = player.inventory.getStackInSlot(s);
            if (stack == null) continue;

            float score = scoreToolForBlock(stack, block, meta, ctx);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                Location loc = s < 9 ? Location.HOTBAR : Location.PLAYER_INVENTORY;
                currentBest = new Candidate(stack, score, loc, s, -1);
            }
        }
        return currentBest;
    }

    private Candidate searchBackpackForWeapon(UpgradeContext ctx, Candidate currentBest) {
        BackpackWrapper wrapper = ctx.wrapper;
        for (int s = 0; s < wrapper.getSlots(); s++) {
            ItemStack stack = wrapper.getStackInSlot(s);
            if (stack == null) continue;

            float score = scoreWeapon(stack, ctx);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                currentBest = new Candidate(stack, score, Location.BACKPACK, -1, s);
            }
        }
        return currentBest;
    }

    private Candidate searchPlayerInventoryForWeapon(EntityPlayer player, UpgradeContext ctx, Candidate currentBest,
        int skipSlot) {
        for (int s = 0; s < 36; s++) {
            if (s == skipSlot) continue;
            ItemStack stack = player.inventory.getStackInSlot(s);
            if (stack == null) continue;

            float score = scoreWeapon(stack, ctx);
            if (score > 0 && (currentBest == null || score > currentBest.score)) {
                Location loc = s < 9 ? Location.HOTBAR : Location.PLAYER_INVENTORY;
                currentBest = new Candidate(stack, score, loc, s, -1);
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
        return playerStates.computeIfAbsent(player.getEntityId(), k -> new PlayerSwapState());
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
    public record UpgradeContext(BackpackWrapper wrapper, int backpackSlot, InventoryType inventoryType,
        IToolSwapperUpgrade upgrade) {

    }

    @Desugar
    public record Candidate(ItemStack stack, float score, Location location, int playerSlot, int backpackInternalSlot) {

    }
}
